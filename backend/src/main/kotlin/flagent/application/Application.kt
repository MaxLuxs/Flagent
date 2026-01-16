package flagent.application

import flagent.cache.impl.EvalCache
import flagent.cache.impl.createEvalCacheFetcher
import flagent.config.AppConfig
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.middleware.configureErrorHandling
import flagent.middleware.configureCompression
import flagent.middleware.configureLogging
import flagent.middleware.configureJWTAuth
import flagent.middleware.configureBasicAuth
import flagent.middleware.configureHeaderAuth
import flagent.middleware.configureCookieAuth
import flagent.middleware.configurePrometheusMetrics
import flagent.middleware.configureStatsDMetrics
import flagent.middleware.configureSentry
import flagent.middleware.configureNewRelic
import flagent.route.configureConstraintRoutes
import flagent.route.configureDistributionRoutes
import flagent.route.configureDocumentationRoutes
import flagent.route.configureEvaluationRoutes
import flagent.route.configureExportRoutes
import flagent.route.configureFlagEntityTypeRoutes
import flagent.route.configureFlagRoutes
import flagent.route.configureFlagSnapshotRoutes
import flagent.route.configureHealthRoutes
import flagent.route.configureInfoRoutes
import flagent.route.configureProfilingRoutes
import flagent.route.configureSegmentRoutes
import flagent.route.configureTagRoutes
import flagent.route.configureVariantRoutes
import flagent.recorder.DataRecordingService
import flagent.service.ConstraintService
import flagent.service.DistributionService
import flagent.service.EvaluationService
import flagent.service.FlagEntityTypeService
import flagent.service.FlagService
import flagent.service.FlagSnapshotService
import flagent.service.SegmentService
import flagent.service.TagService
import flagent.service.VariantService
import flagent.service.ExportService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, host = AppConfig.host, port = AppConfig.port) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    logger.info { "Starting Flagent server on ${AppConfig.host}:${AppConfig.port}" }
    
    // Initialize database
    try {
        Database.init()
        logger.info { "Database initialized successfully" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to initialize database" }
        throw e
    }
    
    // Configure plugins
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
    
    install(CORS) {
        if (AppConfig.corsEnabled) {
            allowCredentials = AppConfig.corsAllowCredentials
            maxAgeInSeconds = AppConfig.corsMaxAge.toLong()
            
            // Configure allowed headers
            AppConfig.corsAllowedHeaders.forEach { header ->
                allowHeader(header)
            }
            
            // Configure allowed methods
            AppConfig.corsAllowedMethods.forEach { method ->
                try {
                    allowMethod(io.ktor.http.HttpMethod.parse(method))
                } catch (e: Exception) {
                    // Skip invalid methods
                }
            }
            
            // Configure allowed origins
            AppConfig.corsAllowedOrigins.forEach { origin ->
                if (origin == "*") {
                    anyHost()
                } else {
                    allowHost(origin)
                }
            }
            
            // Configure exposed headers
            AppConfig.corsExposedHeaders.forEach { header ->
                exposeHeader(header)
            }
        }
    }
    
    install(DefaultHeaders)
    
    // Configure compression
    configureCompression()
    
    // Configure logging
    configureLogging()
    
    // Configure Sentry (before error handling to catch errors)
    configureSentry()
    
    // Configure New Relic (requires Java Agent for full functionality)
    configureNewRelic()
    
    // Configure authentication
    configureJWTAuth()
    configureBasicAuth()
    configureHeaderAuth()
    configureCookieAuth()
    
    // Configure metrics
    configurePrometheusMetrics()
    configureStatsDMetrics()
    
    // Configure error handling and recovery (combined in configureErrorHandling)
    configureErrorHandling()
    
    // Initialize repositories
    val flagRepository = FlagRepository()
    val segmentRepository = SegmentRepository()
    val variantRepository = VariantRepository()
    val constraintRepository = ConstraintRepository()
    val distributionRepository = DistributionRepository()
    val tagRepository = TagRepository()
    val flagSnapshotRepository = FlagSnapshotRepository()
    val flagEntityTypeRepository = FlagEntityTypeRepository()
    
    // Initialize cache with appropriate fetcher
    val evalCache = if (AppConfig.evalOnlyMode && AppConfig.dbDriver in listOf("json_file", "json_http")) {
        // Use fetcher for json_file/json_http
        val fetcher = createEvalCacheFetcher(flagRepository)
        EvalCache(fetcher = fetcher)
    } else {
        // Use repository for database
        EvalCache(flagRepository = flagRepository)
    }
    evalCache.start()
    logger.info { "EvalCache started with driver: ${AppConfig.dbDriver}" }
    
    // Initialize data recording service
    val dataRecordingService = try {
        DataRecordingService().also {
            logger.info { "DataRecordingService initialized with type: ${AppConfig.recorderType}" }
        }
    } catch (e: Exception) {
        logger.error(e) { "Failed to initialize DataRecordingService, continuing without recording" }
        null
    }
    
    // Initialize services
    val evaluationService = EvaluationService(evalCache, dataRecordingService)
    val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
    val flagEntityTypeService = FlagEntityTypeService(flagEntityTypeRepository)
    val segmentService = SegmentService(segmentRepository, flagSnapshotService)
    val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
    val distributionService = DistributionService(distributionRepository, flagRepository, flagSnapshotService)
    val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
    val flagService = FlagService(
        flagRepository, 
        flagSnapshotService,
        segmentService,
        variantService,
        distributionService,
        flagEntityTypeService
    )
    val tagService = TagService(tagRepository, flagRepository, flagSnapshotService)
    val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)
    
    // Configure routes
    routing {
        val routeConfig: Routing.() -> Unit = {
            configureHealthRoutes()
            configureInfoRoutes()
            configureDocumentationRoutes()
            configureEvaluationRoutes(evaluationService)
            
            // Profiling routes (if enabled)
            configureProfilingRoutes()
            
            // In EvalOnlyMode, only register health and evaluation routes
            if (!AppConfig.evalOnlyMode) {
                configureFlagRoutes(flagService)
                configureSegmentRoutes(segmentService)
                configureConstraintRoutes(constraintService)
                configureDistributionRoutes(distributionService)
                configureVariantRoutes(variantService)
                configureTagRoutes(tagService)
                configureFlagSnapshotRoutes(flagSnapshotService)
                configureFlagEntityTypeRoutes(flagEntityTypeService)
                configureExportRoutes(evalCache, exportService)
            } else {
                logger.info { "Running in EvalOnlyMode - CRUD and Export routes are disabled" }
            }
        }
        
        // Apply WebPrefix if configured
        val routingInstance = this
        if (AppConfig.webPrefix.isNotEmpty()) {
            route(AppConfig.webPrefix) {
                routingInstance.routeConfig()
            }
            // Static file serving for frontend (after API routes)
            routingInstance.configureStaticFiles()
        } else {
            routeConfig()
            
            // Static file serving for frontend (after API routes, without prefix)
            configureStaticFiles()
        }
    }
    
    // Shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        evalCache.stop()
        dataRecordingService?.stop()
        Database.close()
    }
}

/**
 * Configure static file serving for frontend
 * Maps to negroni.Static with Prefix: Config.WebPrefix, IndexFile: "index.html"
 */
private fun Routing.configureStaticFiles() {
    // Try multiple possible locations for frontend static files
    val frontendDir = run {
        val currentDir = File(System.getProperty("user.dir"))
        val possiblePaths = listOf(
            File(currentDir, "flagent/frontend/build/dist/js/developmentExecutable"), // From project root
            File(currentDir.parentFile, "frontend/build/dist/js/developmentExecutable"), // From backend directory
            File(currentDir.parentFile?.parentFile, "flagent/frontend/build/dist/js/developmentExecutable"), // From workspace root
            File("./flagent/frontend/build/dist/js/developmentExecutable") // Relative path
        )
        possiblePaths.firstOrNull { it.exists() && it.isDirectory }
    }
    
    if (frontendDir != null && frontendDir.exists()) {
        // Serve static files with fallback to index.html for SPA routing
        staticFiles("/", frontendDir) {
            default("index.html")
        }
        logger.info { "Static files serving enabled from: ${frontendDir.absolutePath}" }
    } else {
        logger.warn { "Frontend static files directory not found, static file serving disabled" }
    }
}
