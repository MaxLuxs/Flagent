package flagent.route

import flagent.domain.entity.SmartRolloutConfig
import flagent.service.SmartRolloutService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Smart Rollout routes - API for smart/automatic rollouts
 */
fun Routing.configureSmartRolloutRoutes(smartRolloutService: SmartRolloutService) {
    route("/api/v1/smart-rollout") {
        // Create smart rollout config
        post {
            val request = call.receive<SmartRolloutConfigRequest>()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val config = SmartRolloutConfig(
                flagId = request.flagId,
                segmentId = request.segmentId,
                enabled = request.enabled ?: true,
                targetRolloutPercent = request.targetRolloutPercent ?: 100,
                currentRolloutPercent = request.currentRolloutPercent ?: 0,
                incrementPercent = request.incrementPercent ?: 10,
                incrementIntervalMs = request.incrementIntervalMs ?: 3600_000,
                successRateThreshold = request.successRateThreshold ?: 0.95,
                errorRateThreshold = request.errorRateThreshold ?: 0.05,
                conversionRateThreshold = request.conversionRateThreshold,
                minSampleSize = request.minSampleSize ?: 100,
                autoRollback = request.autoRollback ?: true,
                rollbackOnAnomaly = request.rollbackOnAnomaly ?: true,
                pauseOnAnomaly = request.pauseOnAnomaly ?: true,
                notifyOnIncrement = request.notifyOnIncrement ?: true,
                tenantId = tenantId
            )
            
            val created = smartRolloutService.createConfig(config)
            call.respond(HttpStatusCode.Created, mapConfigToResponse(created))
        }
        
        // Get config by ID
        get("/{configId}") {
            val configId = call.parameters["configId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid config ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val config = smartRolloutService.getConfigById(configId, tenantId)
            
            if (config != null) {
                call.respond(mapConfigToResponse(config))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Config not found"))
            }
        }
        
        // Update config
        put("/{configId}") {
            val configId = call.parameters["configId"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid config ID"))
            val request = call.receive<SmartRolloutConfigRequest>()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val existing = smartRolloutService.getConfigById(configId, tenantId)
                ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Config not found"))
            
            val updated = existing.copy(
                enabled = request.enabled ?: existing.enabled,
                targetRolloutPercent = request.targetRolloutPercent ?: existing.targetRolloutPercent,
                incrementPercent = request.incrementPercent ?: existing.incrementPercent,
                incrementIntervalMs = request.incrementIntervalMs ?: existing.incrementIntervalMs,
                successRateThreshold = request.successRateThreshold ?: existing.successRateThreshold,
                errorRateThreshold = request.errorRateThreshold ?: existing.errorRateThreshold,
                conversionRateThreshold = request.conversionRateThreshold ?: existing.conversionRateThreshold,
                minSampleSize = request.minSampleSize ?: existing.minSampleSize,
                autoRollback = request.autoRollback ?: existing.autoRollback,
                rollbackOnAnomaly = request.rollbackOnAnomaly ?: existing.rollbackOnAnomaly,
                pauseOnAnomaly = request.pauseOnAnomaly ?: existing.pauseOnAnomaly,
                notifyOnIncrement = request.notifyOnIncrement ?: existing.notifyOnIncrement
            )
            
            val savedConfig = smartRolloutService.updateConfig(updated)
            call.respond(mapConfigToResponse(savedConfig))
        }
        
        // Delete config
        delete("/{configId}") {
            val configId = call.parameters["configId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid config ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val deleted = smartRolloutService.deleteConfig(configId, tenantId)
            
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Config not found"))
            }
        }
        
        // Get configs for a flag
        get("/flag/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val configs = smartRolloutService.getConfigsByFlagId(flagId, tenantId)
            call.respond(configs.map { mapConfigToResponse(it) })
        }
        
        // Execute rollout for a config
        post("/{configId}/execute") {
            val configId = call.parameters["configId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid config ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val result = smartRolloutService.executeRollout(configId, hasAnomaly = false, tenantId)
            
            if (result.success) {
                call.respond(mapOf(
                    "success" to true,
                    "decision" to result.decision,
                    "config" to result.updatedConfig?.let { mapConfigToResponse(it) },
                    "metrics" to result.metricsSummary
                ))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to result.error))
            }
        }
        
        // Get rollout history for a config
        get("/{configId}/history") {
            val configId = call.parameters["configId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid config ID"))
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val history = smartRolloutService.getHistory(configId, limit, tenantId)
            call.respond(history)
        }
        
        // Get rollout history for a flag
        get("/flag/{flagId}/history") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val history = smartRolloutService.getHistoryByFlagId(flagId, limit, tenantId)
            call.respond(history)
        }
    }
}

/**
 * Request model for smart rollout config
 */
@Serializable
data class SmartRolloutConfigRequest(
    val flagId: Int,
    val segmentId: Int,
    val enabled: Boolean? = null,
    val targetRolloutPercent: Int? = null,
    val currentRolloutPercent: Int? = null,
    val incrementPercent: Int? = null,
    val incrementIntervalMs: Long? = null,
    val successRateThreshold: Double? = null,
    val errorRateThreshold: Double? = null,
    val conversionRateThreshold: Double? = null,
    val minSampleSize: Int? = null,
    val autoRollback: Boolean? = null,
    val rollbackOnAnomaly: Boolean? = null,
    val pauseOnAnomaly: Boolean? = null,
    val notifyOnIncrement: Boolean? = null
)

/**
 * Response model for smart rollout config
 */
@Serializable
data class SmartRolloutConfigResponse(
    val id: Int,
    val flagId: Int,
    val segmentId: Int,
    val enabled: Boolean,
    val targetRolloutPercent: Int,
    val currentRolloutPercent: Int,
    val incrementPercent: Int,
    val incrementIntervalMs: Long,
    val successRateThreshold: Double,
    val errorRateThreshold: Double,
    val conversionRateThreshold: Double?,
    val minSampleSize: Int,
    val autoRollback: Boolean,
    val rollbackOnAnomaly: Boolean,
    val pauseOnAnomaly: Boolean,
    val notifyOnIncrement: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastIncrementAt: Long?,
    val completedAt: Long?,
    val status: SmartRolloutConfig.RolloutStatus,
    val tenantId: String?
)

/**
 * Map SmartRolloutConfig to response
 */
private fun mapConfigToResponse(config: SmartRolloutConfig): SmartRolloutConfigResponse {
    return SmartRolloutConfigResponse(
        id = config.id,
        flagId = config.flagId,
        segmentId = config.segmentId,
        enabled = config.enabled,
        targetRolloutPercent = config.targetRolloutPercent,
        currentRolloutPercent = config.currentRolloutPercent,
        incrementPercent = config.incrementPercent,
        incrementIntervalMs = config.incrementIntervalMs,
        successRateThreshold = config.successRateThreshold,
        errorRateThreshold = config.errorRateThreshold,
        conversionRateThreshold = config.conversionRateThreshold,
        minSampleSize = config.minSampleSize,
        autoRollback = config.autoRollback,
        rollbackOnAnomaly = config.rollbackOnAnomaly,
        pauseOnAnomaly = config.pauseOnAnomaly,
        notifyOnIncrement = config.notifyOnIncrement,
        createdAt = config.createdAt,
        updatedAt = config.updatedAt,
        lastIncrementAt = config.lastIncrementAt,
        completedAt = config.completedAt,
        status = config.status,
        tenantId = config.tenantId
    )
}
