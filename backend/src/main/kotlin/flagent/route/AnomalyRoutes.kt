package flagent.route

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig
import flagent.service.AnomalyDetectionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Anomaly routes - API for anomaly detection and alerts
 */
fun Routing.configureAnomalyRoutes(anomalyDetectionService: AnomalyDetectionService) {
    route("/api/v1/anomaly") {
        // ===== Anomaly Detection Config =====
        
        // Create or update anomaly detection config
        post("/config") {
            val request = call.receive<AnomalyDetectionConfigRequest>()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val config = AnomalyDetectionConfig(
                flagId = request.flagId,
                enabled = request.enabled ?: true,
                zScoreThreshold = request.zScoreThreshold ?: 3.0,
                errorRateThreshold = request.errorRateThreshold ?: 0.1,
                successRateThreshold = request.successRateThreshold ?: 0.8,
                latencyThresholdMs = request.latencyThresholdMs ?: 1000.0,
                conversionRateThreshold = request.conversionRateThreshold ?: 0.05,
                minSampleSize = request.minSampleSize ?: 100,
                windowSizeMs = request.windowSizeMs ?: 300_000,
                autoKillSwitch = request.autoKillSwitch ?: false,
                autoRollback = request.autoRollback ?: false,
                tenantId = tenantId
            )
            
            val saved = anomalyDetectionService.saveConfig(config)
            call.respond(HttpStatusCode.Created, saved)
        }
        
        // Get anomaly detection config for a flag
        get("/config/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val config = anomalyDetectionService.getConfig(flagId, tenantId)
            
            if (config != null) {
                call.respond(config)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Config not found"))
            }
        }
        
        // Delete anomaly detection config
        delete("/config/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val deleted = anomalyDetectionService.deleteConfig(flagId, tenantId)
            
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Config not found"))
            }
        }
        
        // ===== Anomaly Detection Execution =====
        
        // Run anomaly detection for a specific flag
        post("/detect/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val report = anomalyDetectionService.detectAnomaliesForFlag(flagId, tenantId)
            
            call.respond(mapOf(
                "flag_id" to report.flagId,
                "anomalies_detected" to report.anomalyCount,
                "has_anomalies" to report.hasAnomalies,
                "critical_anomalies" to report.criticalAnomalies.size,
                "skipped" to report.skipped,
                "skip_reason" to report.skipReason,
                "alerts" to report.anomaliesDetected.map { mapAlertToResponse(it) }
            ))
        }
        
        // Run anomaly detection for all enabled configs
        post("/detect-all") {
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val reports = anomalyDetectionService.detectAnomaliesForAll(tenantId)
            
            call.respond(mapOf(
                "total_flags" to reports.size,
                "flags_with_anomalies" to reports.count { it.hasAnomalies },
                "total_anomalies" to reports.sumOf { it.anomalyCount },
                "reports" to reports.map { report ->
                    mapOf(
                        "flag_id" to report.flagId,
                        "anomalies" to report.anomalyCount,
                        "critical" to report.criticalAnomalies.size
                    )
                }
            ))
        }
        
        // ===== Anomaly Alerts =====
        
        // Get alerts for a flag
        get("/alerts/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            val resolved = call.request.queryParameters["resolved"]?.toBoolean()
            val severityStr = call.request.queryParameters["severity"]
            val severity = severityStr?.let {
                try {
                    AnomalyAlert.Severity.valueOf(it)
                } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid severity"))
                }
            }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val alerts = anomalyDetectionService.getAlertsForFlag(
                flagId = flagId,
                resolved = resolved,
                severity = severity,
                limit = limit,
                tenantId = tenantId
            )
            
            call.respond(alerts.map { mapAlertToResponse(it) })
        }
        
        // Get unresolved alerts (all flags or specific flag)
        get("/alerts/unresolved") {
            val flagId = call.request.queryParameters["flag_id"]?.toIntOrNull()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val alerts = anomalyDetectionService.getUnresolvedAlerts(flagId, tenantId)
            
            call.respond(alerts.map { mapAlertToResponse(it) })
        }
        
        // Mark alert as resolved
        post("/alerts/{alertId}/resolve") {
            val alertId = call.parameters["alertId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid alert ID"))
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val alert = anomalyDetectionService.resolveAlert(alertId, tenantId)
            
            if (alert != null) {
                call.respond(mapAlertToResponse(alert))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Alert not found"))
            }
        }
    }
}

/**
 * Request model for anomaly detection config
 */
@Serializable
data class AnomalyDetectionConfigRequest(
    val flagId: Int,
    val enabled: Boolean? = null,
    val zScoreThreshold: Double? = null,
    val errorRateThreshold: Double? = null,
    val successRateThreshold: Double? = null,
    val latencyThresholdMs: Double? = null,
    val conversionRateThreshold: Double? = null,
    val minSampleSize: Int? = null,
    val windowSizeMs: Long? = null,
    val autoKillSwitch: Boolean? = null,
    val autoRollback: Boolean? = null
)

/**
 * Response model for anomaly alert
 */
@Serializable
data class AnomalyAlertResponse(
    val id: Int,
    val flagId: Int,
    val flagKey: String,
    val variantId: Int?,
    val variantKey: String?,
    val anomalyType: AnomalyAlert.AnomalyType,
    val severity: AnomalyAlert.Severity,
    val detectedAt: Long,
    val metricType: flagent.domain.entity.MetricDataPoint.MetricType,
    val metricValue: Double,
    val expectedValue: Double,
    val zScore: Double,
    val message: String,
    val actionTaken: AnomalyAlert.ActionTaken?,
    val actionTakenAt: Long?,
    val resolved: Boolean,
    val resolvedAt: Long?,
    val tenantId: String?
)

/**
 * Map AnomalyAlert to response
 */
private fun mapAlertToResponse(alert: AnomalyAlert): AnomalyAlertResponse {
    return AnomalyAlertResponse(
        id = alert.id,
        flagId = alert.flagId,
        flagKey = alert.flagKey,
        variantId = alert.variantId,
        variantKey = alert.variantKey,
        anomalyType = alert.anomalyType,
        severity = alert.severity,
        detectedAt = alert.detectedAt,
        metricType = alert.metricType,
        metricValue = alert.metricValue,
        expectedValue = alert.expectedValue,
        zScore = alert.zScore,
        message = alert.message,
        actionTaken = alert.actionTaken,
        actionTakenAt = alert.actionTakenAt,
        resolved = alert.resolved,
        resolvedAt = alert.resolvedAt,
        tenantId = alert.tenantId
    )
}
