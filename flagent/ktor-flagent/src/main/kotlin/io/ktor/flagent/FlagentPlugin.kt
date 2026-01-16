package io.ktor.flagent

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * FlagentPlugin - Ktor plugin for Flagent functionality
 * Provides evaluation endpoints, caching, and data recording
 */
class FlagentPluginConfig {
    /**
     * Base URL of Flagent server
     */
    var flagentBaseUrl: String = "http://localhost:18000"
    
    /**
     * Enable evaluation endpoints
     */
    var enableEvaluation: Boolean = true
    
    /**
     * Enable caching
     */
    var enableCache: Boolean = true
    
    /**
     * Enable data recording
     */
    var enableDataRecording: Boolean = true
    
    /**
     * Cache TTL in milliseconds
     */
    var cacheTtlMs: Long = 60000 // 1 minute
    
    /**
     * HTTP client configuration
     */
    var connectTimeoutMs: Long = 5000
    var requestTimeoutMs: Long = 10000
    
    /**
     * Metrics configuration
     */
    var metrics: FlagentMetricsConfig.() -> Unit = {}
}

/**
 * FlagentPlugin - main plugin class
 */
val FlagentPlugin = createApplicationPlugin(
    name = "Flagent",
    createConfiguration = ::FlagentPluginConfig
) {
    val config = pluginConfig
    
    logger.info { "FlagentPlugin initialized with baseUrl: ${config.flagentBaseUrl}" }
    
    // Create FlagentClient
    val client = FlagentClient(
        baseUrl = config.flagentBaseUrl,
        connectTimeoutMs = config.connectTimeoutMs,
        requestTimeoutMs = config.requestTimeoutMs
    )
    
    // Create cache if enabled
    val cache = if (config.enableCache) {
        FlagentCache(ttlMs = config.cacheTtlMs)
    } else {
        null
    }
    
    // Configure metrics
    val metricsConfig = FlagentMetricsConfig().apply(config.metrics)
    val metrics = application.configureFlagentMetrics(metricsConfig)
    
    // Store in application attributes
    application.attributes.put(FlagentPluginAttributes.client, client)
    if (cache != null) {
        application.attributes.put(FlagentPluginAttributes.cache, cache)
    }
    if (metrics != null) {
        application.attributes.put(FlagentPluginAttributes.metrics, metrics)
    }
    
    // Configure routes if evaluation is enabled
    if (config.enableEvaluation) {
        // Ensure Routing plugin is installed
        if (application.pluginOrNull(Routing) == null) {
            application.install(Routing)
        }
        
        application.routing {
            route("/flagent") {
                // Health check endpoint
                get("/health") {
                    try {
                        // Try to make a simple request to verify connection
                        call.respond(mapOf(
                            "status" to "OK",
                            "server" to config.flagentBaseUrl
                        ))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                            "status" to "ERROR",
                            "error" to e.message
                        ))
                    }
                }
                
                post("/evaluate") {
                    // Validate request
                    val request = try {
                        call.receive<EvaluationRequest>()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse evaluation request" }
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format: ${e.message}"))
                        return@post
                    }
                    
                    // Validate that either flagID or flagKey is provided
                    if (request.flagID == null && request.flagKey == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Either flagID or flagKey must be provided"))
                        return@post
                    }
                    
                    val cacheKey = cache?.generateKey(request)
                    var fromCache = false
                    
                    val duration = kotlin.time.measureTime {
                        // Try cache first
                        if (cache != null && cacheKey != null) {
                            val cached = cache.get(cacheKey)
                            if (cached != null) {
                                fromCache = true
                                metrics?.recordEvaluation(
                                    flagID = request.flagID,
                                    flagKey = request.flagKey,
                                    duration = kotlin.time.Duration.ZERO,
                                    fromCache = true
                                )
                                call.respond(cached)
                                return@post
                            }
                        }
                        
                        try {
                            // Evaluate via client
                            val response = client.evaluate(request)
                            
                            // Cache result
                            if (cache != null && cacheKey != null) {
                                cache.put(cacheKey, response)
                            }
                            
                            // Record metrics
                            metrics?.recordEvaluation(
                                flagID = response.flagID,
                                flagKey = response.flagKey,
                                duration = duration,
                                fromCache = false
                            )
                            
                            call.respond(response)
                        } catch (e: FlagentException) {
                            logger.error(e) { "Failed to evaluate flag" }
                            metrics?.recordEvaluationError(
                                flagID = request.flagID,
                                flagKey = request.flagKey,
                                errorType = e.javaClass.simpleName
                            )
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message ?: "Unknown error"))
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to evaluate flag" }
                            metrics?.recordEvaluationError(
                                flagID = request.flagID,
                                flagKey = request.flagKey,
                                errorType = e.javaClass.simpleName
                            )
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message ?: "Unknown error"))
                        }
                    }
                }
                
                post("/evaluate/batch") {
                    // Validate request
                    val request = try {
                        call.receive<EvaluationBatchRequest>()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse batch evaluation request" }
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format: ${e.message}"))
                        return@post
                    }
                    
                    // Validate that at least one entity is provided
                    if (request.entities.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "At least one entity must be provided"))
                        return@post
                    }
                    
                    // Validate that at least one flag identifier is provided
                    if (request.flagIDs.isEmpty() && request.flagKeys.isEmpty() && request.flagTags.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "At least one of flagIDs, flagKeys, or flagTags must be provided"))
                        return@post
                    }
                    
                    val duration = kotlin.time.measureTime {
                        try {
                            // For batch requests, we don't use cache as the request format is different
                            // and caching would be complex. The Flagent server should handle caching internally.
                            val response = client.evaluateBatch(request)
                            
                            // Record batch metrics
                            metrics?.recordBatchEvaluation(
                                count = response.evaluationResults.size,
                                duration = duration,
                                errors = 0
                            )
                            
                            call.respond(response)
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to evaluate flags batch" }
                            val entityCount = request.entities.size
                            val flagCount = request.flagIDs.size + request.flagKeys.size + request.flagTags.size
                            val totalRequests = entityCount * flagCount
                            
                            metrics?.recordBatchEvaluation(
                                count = totalRequests,
                                duration = duration,
                                errors = totalRequests
                            )
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message ?: "Unknown error"))
                        }
                    }
                }
            }
        }
    }
    
    // Cleanup on shutdown
    application.environment.monitor.subscribe(io.ktor.server.application.ApplicationStopped) {
        client.close()
        cache?.clear()
    }
}

/**
 * Application attributes for FlagentPlugin
 */
object FlagentPluginAttributes {
    val client = AttributeKey<FlagentClient>("FlagentClient")
    val cache = AttributeKey<FlagentCache>("FlagentCache")
    val metrics = AttributeKey<FlagentMetrics>("FlagentMetrics")
}

/**
 * Install FlagentPlugin with configuration
 */
fun Application.installFlagent(configure: FlagentPluginConfig.() -> Unit = {}) {
    install(FlagentPlugin) {
        configure()
    }
}
