package flagent.application

import flagent.api.EnterpriseConfigurator
import flagent.config.AppConfig
import flagent.repository.Database
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
import flagent.route.configureAnalyticsEventsRoutes
import flagent.route.configureCoreMetricsRoutes
import flagent.route.configureCrashRoutes
import flagent.route.configureDistributionRoutes
import flagent.route.configureEvaluationRoutes
import flagent.route.configureExportRoutes
import flagent.route.configureImportRoutes
import flagent.route.configureFlagEntityTypeRoutes
import flagent.route.configureFlagRoutes
import flagent.route.configureFlagSnapshotRoutes
import flagent.route.configureHealthRoutes
import flagent.route.configureInfoRoutes
import flagent.route.configureProfilingRoutes
import flagent.route.configureSegmentRoutes
import flagent.route.configureTagRoutes
import flagent.route.configureVariantRoutes
import flagent.route.configureWebhookRoutes
import flagent.route.integration.configureIntegrationWebhookRoutes
import flagent.mcp.configureMcpRoutes
import flagent.middleware.configureSSE
import flagent.middleware.configureRealtimeEventBus
import flagent.route.RealtimeEventBus
import flagent.route.realtimeRoutes
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
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.util.ServiceLoader

private val logger = KotlinLogging.logger {}

private fun Application.configurePlugins() {
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
            AppConfig.corsAllowedHeaders.forEach { allowHeader(it) }
            AppConfig.corsAllowedMethods.forEach { method ->
                try { allowMethod(io.ktor.http.HttpMethod.parse(method)) } catch (_: Exception) {}
            }
            val origins = AppConfig.corsAllowedOrigins
            val useWildcard = origins.any { it == "*" } && !AppConfig.corsAllowCredentials
            if (useWildcard) anyHost()
            else {
                origins.filter { it != "*" }.forEach { origin ->
                    allowHost(origin.removePrefix("http://").removePrefix("https://"))
                }
                if (origins.any { it == "*" }) {
                    listOf("localhost:8080", "localhost:8081", "localhost:18000",
                        "127.0.0.1:8080", "127.0.0.1:8081", "127.0.0.1:18000").forEach { allowHost(it) }
                }
            }
            AppConfig.corsExposedHeaders.forEach { exposeHeader(it) }
        }
    }
    install(DefaultHeaders)
    configureSSE()
    configureCompression()
    configureLogging()
    configureSentry()
    configureNewRelic()
    configureJWTAuth()
    configureBasicAuth()
    configureHeaderAuth()
    configureCookieAuth()
    configurePrometheusMetrics()
    configureStatsDMetrics()
    configureErrorHandling()
}

fun main() {
    try {
        embeddedServer(
            factory = Netty,
            configure = {
                connector {
                    host = AppConfig.host
                    port = AppConfig.port
                }
                workerGroupSize = AppConfig.workerPoolSize
                callGroupSize = AppConfig.workerPoolSize
            }
        ) {
            module()
        }.start(wait = true)
    } catch (e: Exception) {
        throw e
    }
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
    
    configurePlugins()
    val eventBus = configureRealtimeEventBus()

    val repos = createRepositories()
    val cacheAndSync = createCacheAndSync(repos.flagRepository)
    logger.info { "EvalCache started with driver: ${AppConfig.dbDriver}" }
    if (cacheAndSync.firebaseRcSyncService != null) {
        logger.info { "Firebase RC sync started for project ${AppConfig.firebaseRcProjectId}" }
    }

    val recordingAndMetrics = createRecordingAndMetrics(repos.evaluationEventRepository, repos.analyticsEventRepository)
    if (recordingAndMetrics.dataRecordingService != null) {
        logger.info { "DataRecordingService initialized with type: ${AppConfig.recorderType}" }
    }
    if (recordingAndMetrics.firebaseAnalyticsReporter != null) {
        logger.info { "Firebase Analytics reporter enabled" }
    }
    if (recordingAndMetrics.evaluationEventRecorder != null) {
        logger.info { "EvaluationEventRecorder initialized for core metrics" }
    }

    val services = createServices(repos, cacheAndSync, recordingAndMetrics, eventBus)

    val enterpriseConfigurator = ServiceLoader.load(EnterpriseConfigurator::class.java).toList().firstOrNull() ?: DefaultEnterpriseConfigurator()
    EnterprisePresence.enterpriseEnabled = enterpriseConfigurator !is DefaultEnterpriseConfigurator

    val coreDeps = CoreDependenciesImpl(services.segmentService, repos.flagRepository, cacheAndSync.evalCache)
    val backendContext = EnterpriseBackendContextImpl(coreDeps)
    enterpriseConfigurator.configure(this, backendContext)

    routing {
        val routeConfig: Routing.() -> Unit = {
            configureHealthRoutes()
            configureAuthRoutes()
            configureInfoRoutes()
            configureEvaluationRoutes(services.evaluationService)

            realtimeRoutes(services.flagService, eventBus)
            
            // Profiling routes (if enabled)
            configureProfilingRoutes()
            
            // In EvalOnlyMode, only register health and evaluation routes
            if (!AppConfig.evalOnlyMode) {
                configureFlagRoutes(services.flagService)
                configureSegmentRoutes(services.segmentService)
                configureConstraintRoutes(services.constraintService)
                configureDistributionRoutes(services.distributionService)
                configureVariantRoutes(services.variantService)
                configureTagRoutes(services.tagService)
                configureFlagSnapshotRoutes(services.flagSnapshotService)
                configureFlagEntityTypeRoutes(services.flagEntityTypeService)
                configureExportRoutes(cacheAndSync.evalCache, services.exportService)
                configureImportRoutes(services.importService)
                configureWebhookRoutes(services.webhookService)
                configureIntegrationWebhookRoutes(services.flagService)

                if (!EnterprisePresence.enterpriseEnabled && services.coreMetricsService != null) {
                    configureCoreMetricsRoutes(services.coreMetricsService, services.flagService)
                }

                if (!EnterprisePresence.enterpriseEnabled) {
                    configureCrashRoutes(services.crashReportService)
                }

                configureAnalyticsEventsRoutes(services.analyticsEventsService)

                // Tenant, billing, SSO, AI rollouts: registered by enterprise when present
                enterpriseConfigurator.configureRoutes(this, backendContext)
            } else {
                logger.info { "Running in EvalOnlyMode - CRUD and Export routes are disabled" }
            }

            // MCP server (Model Context Protocol for AI assistants)
            if (AppConfig.mcpEnabled) {
                configureMcpRoutes(AppConfig.mcpPath, services.evaluationService, cacheAndSync.evalCache)
            }

            // Catch-all for unmatched /api paths: return 404 JSON instead of falling through to staticFiles (index.html)
            route("/api") {
                route("{...}") {
                    get {
                        call.respondText("""{"error":"Not found"}""", io.ktor.http.ContentType.Application.Json, io.ktor.http.HttpStatusCode.NotFound)
                    }
                    post {
                        call.respondText("""{"error":"Not found"}""", io.ktor.http.ContentType.Application.Json, io.ktor.http.HttpStatusCode.NotFound)
                    }
                    put {
                        call.respondText("""{"error":"Not found"}""", io.ktor.http.ContentType.Application.Json, io.ktor.http.HttpStatusCode.NotFound)
                    }
                    delete {
                        call.respondText("""{"error":"Not found"}""", io.ktor.http.ContentType.Application.Json, io.ktor.http.HttpStatusCode.NotFound)
                    }
                    patch {
                        call.respondText("""{"error":"Not found"}""", io.ktor.http.ContentType.Application.Json, io.ktor.http.HttpStatusCode.NotFound)
                    }
                }
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
    
    environment.monitor.subscribe(ApplicationStopped) {
        cacheAndSync.evalCache.stop()
        cacheAndSync.firebaseRcSyncService?.stop()
        recordingAndMetrics.firebaseAnalyticsReporter?.close()
        recordingAndMetrics.dataRecordingService?.stop()
        recordingAndMetrics.evaluationEventRecorder?.stop()
        recordingAndMetrics.evaluationEventsCleanupJob?.stop()
        recordingAndMetrics.analyticsEventsCleanupJob?.stop()
        Database.close()
    }
}

/**
 * Static file serving for frontend
 */
private fun Routing.configureStaticFiles() {
    val currentDir = File(System.getProperty("user.dir"))
    val explicitPaths = listOfNotNull(
        AppConfig.frontendStaticDir?.let { File(it) },
        AppConfig.staticDir?.let { File(it) }
    )
    for (dir in explicitPaths) {
        if (dir.exists() && dir.isDirectory) {
            staticFiles("/", dir) { default("index.html") }
            logger.info { "Static files from config: ${dir.absolutePath}" }
            return
        }
    }

    val fallbackPaths = listOf(
        File(currentDir, "frontend/build/kotlin-webpack/js/productionExecutable"),
        File(currentDir, "frontend/build/kotlin-webpack/js/developmentExecutable"),
        File(currentDir, "frontend/build/dist/js/productionExecutable"),
        File(currentDir.parentFile, "frontend/build/kotlin-webpack/js/productionExecutable"),
        File(currentDir.parentFile, "frontend/build/kotlin-webpack/js/developmentExecutable")
    )
    val frontendDir = fallbackPaths.firstOrNull { it.exists() && it.isDirectory }

    if (frontendDir != null) {
        staticFiles("/", frontendDir) { default("index.html") }
        logger.info { "Static files serving enabled from: ${frontendDir.absolutePath}" }
    } else {
        logger.warn { "Frontend static files directory not found, static file serving disabled" }
    }
}
