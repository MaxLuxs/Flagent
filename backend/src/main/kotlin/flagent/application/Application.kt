package flagent.application

import flagent.api.EnterpriseBackendContext
import flagent.api.EnterpriseConfigurator
import flagent.cache.impl.EvalCache
import flagent.cache.impl.createEvalCacheFetcher
import flagent.config.AppConfig
import flagent.repository.Database
import flagent.repository.impl.ConstraintRepository
import flagent.repository.impl.DistributionRepository
import flagent.repository.impl.FlagEntityTypeRepository
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.FlagSnapshotRepository
import flagent.repository.impl.SegmentRepository
import flagent.repository.impl.TagRepository
import flagent.repository.impl.VariantRepository
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
import flagent.route.configureAuthRoutes
import flagent.route.configureConstraintRoutes
import flagent.route.configureDistributionRoutes
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
import flagent.middleware.configureSSE
import flagent.middleware.configureRealtimeEventBus
import flagent.recorder.DataRecordingService
import flagent.route.realtimeRoutes
import flagent.service.ConstraintService
import flagent.service.DistributionService
import flagent.service.EvaluationService
import flagent.service.adapter.SharedFlagEvaluatorAdapter
import flagent.domain.usecase.EvaluateFlagUseCase
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
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.util.ServiceLoader

private val logger = KotlinLogging.logger {}

fun main() {
    embeddedServer(
        factory = Netty,
        configure = {
            connector {
                host = AppConfig.host
                port = AppConfig.port
            }
        }
    ) {
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
    
    // Configure SSE for real-time updates
    configureSSE()
    
    // Configure compression
    configureCompression()
    
    // Configure logging
    configureLogging()
    
    // Configure Sentry (before error handling to catch errors)
    configureSentry()
    
    // Configure New Relic (requires Java Agent for full functionality)
    configureNewRelic()
    
    // Configure authentication (SSO JWT configured by enterprise when present)
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
    
    // Initialize evaluation: shared evaluator as single source of truth
    val sharedFlagEvaluatorAdapter = SharedFlagEvaluatorAdapter()
    val evaluateFlagUseCase = EvaluateFlagUseCase(sharedFlagEvaluatorAdapter)
    val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase, dataRecordingService)
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
    
    // Configure realtime event bus for SSE
    val eventBus = configureRealtimeEventBus()
    
    // TODO: Integrate eventBus with services for automatic event publishing
    // This will enable automatic SSE notifications on flag CRUD operations
    // Example implementation needed in FlagService, SegmentService, VariantService
    val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)
    
    val enterpriseConfigurator = ServiceLoader.load(EnterpriseConfigurator::class.java).toList().firstOrNull() ?: DefaultEnterpriseConfigurator()
    EnterprisePresence.enterpriseEnabled = enterpriseConfigurator !is DefaultEnterpriseConfigurator

    // Invoke enterprise configurator (migrations; when enterprise present it configures middleware and routes)
    val coreDeps = CoreDependenciesImpl(segmentService, flagRepository, evalCache)
    val backendContext = EnterpriseBackendContextImpl(coreDeps)
    enterpriseConfigurator.configure(this, backendContext)
    
    // Configure routes
    routing {
        val routeConfig: Routing.() -> Unit = {
            configureHealthRoutes()
            configureAuthRoutes()
            configureInfoRoutes()
            configureEvaluationRoutes(evaluationService)
            
            // Real-time updates via SSE
            realtimeRoutes(flagService, eventBus)
            
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
                
                // Tenant, billing, SSO, AI rollouts: registered by enterprise when present
                enterpriseConfigurator.configureRoutes(this, backendContext)
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
        
        // New Ktor 3.4.0 OpenAPI and Swagger UI plugins
        // Serve OpenAPI specification at /openapi
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        
        // Serve Swagger UI at /docs
        swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml")
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
