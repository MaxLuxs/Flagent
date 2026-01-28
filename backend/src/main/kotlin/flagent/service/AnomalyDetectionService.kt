package flagent.service

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig
import flagent.domain.entity.MetricDataPoint
import flagent.domain.repository.IAnomalyAlertRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IMetricsRepository
import flagent.domain.usecase.DetectAnomalyUseCase

/**
 * AnomalyDetectionService - detects anomalies and manages alerts
 * 
 * Service layer - orchestrates anomaly detection and actions
 */
class AnomalyDetectionService(
    private val anomalyAlertRepository: IAnomalyAlertRepository,
    private val metricsRepository: IMetricsRepository,
    private val flagRepository: IFlagRepository,
    private val slackNotificationService: SlackNotificationService? = null,
    private val detectAnomalyUseCase: DetectAnomalyUseCase = DetectAnomalyUseCase()
) {
    /**
     * Run anomaly detection for a specific flag
     */
    suspend fun detectAnomaliesForFlag(
        flagId: Int,
        tenantId: String? = null
    ): DetectionReport {
        // Get config
        val config = anomalyAlertRepository.findConfigByFlagId(flagId, tenantId)
            ?: return DetectionReport(
                flagId = flagId,
                anomaliesDetected = emptyList(),
                skipped = true,
                skipReason = "No anomaly detection config found"
            )
        
        val now = System.currentTimeMillis()
        val windowStart = now - config.windowSizeMs
        
        val anomalies = mutableListOf<AnomalyAlert>()
        
        // Check each metric type
        for (metricType in listOf(
            MetricDataPoint.MetricType.SUCCESS_RATE,
            MetricDataPoint.MetricType.ERROR_RATE,
            MetricDataPoint.MetricType.LATENCY_MS,
            MetricDataPoint.MetricType.CONVERSION_RATE
        )) {
            val aggregation = metricsRepository.getAggregation(
                flagId = flagId,
                metricType = metricType,
                windowStartMs = windowStart,
                windowEndMs = now,
                tenantId = tenantId
            ) ?: continue
            
            val recentMetrics = metricsRepository.findByFlagId(
                flagId = flagId,
                startTime = windowStart,
                endTime = now,
                metricType = metricType,
                tenantId = tenantId
            )
            
            val result = detectAnomalyUseCase.invoke(config, aggregation, recentMetrics)
            
            if (result.isAnomaly && result.alert != null) {
                val savedAlert = anomalyAlertRepository.saveAlert(result.alert)
                anomalies.add(savedAlert)
                
                // Send Slack notification for critical/high severity alerts
                slackNotificationService?.sendAnomalyAlert(savedAlert)
                
                // Take action if needed
                if (savedAlert.requiresAction()) {
                    takeAction(savedAlert, config)
                }
            }
        }
        
        return DetectionReport(
            flagId = flagId,
            anomaliesDetected = anomalies,
            skipped = false
        )
    }
    
    /**
     * Run anomaly detection for all enabled configs
     */
    suspend fun detectAnomaliesForAll(tenantId: String? = null): List<DetectionReport> {
        val configs = anomalyAlertRepository.findEnabledConfigs(tenantId)
        
        return configs.map { config ->
            detectAnomaliesForFlag(config.flagId, tenantId)
        }
    }
    
    /**
     * Take action on anomaly alert
     */
    private suspend fun takeAction(alert: AnomalyAlert, config: AnomalyDetectionConfig) {
        val action = alert.recommendedAction()
        
        when (action) {
            AnomalyAlert.ActionTaken.FLAG_DISABLED -> {
                if (config.autoKillSwitch) {
                    // Disable flag
                    flagRepository.findById(alert.flagId)?.let { flag ->
                        flagRepository.update(flag.copy(enabled = false))
                    }
                }
            }
            AnomalyAlert.ActionTaken.ROLLOUT_DECREASED,
            AnomalyAlert.ActionTaken.ROLLOUT_PAUSED -> {
                // Handled by SmartRolloutService
            }
            else -> {
                // Just alert, no automatic action
            }
        }
        
        // Update alert with action taken
        anomalyAlertRepository.updateAlert(
            alert.copy(
                actionTaken = action,
                actionTakenAt = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Get alerts for a flag
     */
    suspend fun getAlertsForFlag(
        flagId: Int,
        resolved: Boolean? = null,
        severity: AnomalyAlert.Severity? = null,
        limit: Int = 100,
        tenantId: String? = null
    ): List<AnomalyAlert> {
        return anomalyAlertRepository.findAlertsByFlagId(
            flagId = flagId,
            resolved = resolved,
            severity = severity,
            limit = limit,
            tenantId = tenantId
        )
    }
    
    /**
     * Get unresolved alerts
     */
    suspend fun getUnresolvedAlerts(
        flagId: Int? = null,
        tenantId: String? = null
    ): List<AnomalyAlert> {
        return anomalyAlertRepository.findUnresolvedAlerts(flagId, tenantId)
    }
    
    /**
     * Mark alert as resolved
     */
    suspend fun resolveAlert(alertId: Int, tenantId: String? = null): AnomalyAlert? {
        return anomalyAlertRepository.markResolved(alertId, tenantId)
    }
    
    /**
     * Create or update anomaly detection config
     */
    suspend fun saveConfig(config: AnomalyDetectionConfig): AnomalyDetectionConfig {
        val existing = anomalyAlertRepository.findConfigByFlagId(config.flagId, config.tenantId)
        
        return if (existing != null) {
            anomalyAlertRepository.updateConfig(config)
        } else {
            anomalyAlertRepository.saveConfig(config)
        }
    }
    
    /**
     * Get config for a flag
     */
    suspend fun getConfig(flagId: Int, tenantId: String? = null): AnomalyDetectionConfig? {
        return anomalyAlertRepository.findConfigByFlagId(flagId, tenantId)
    }
    
    /**
     * Delete config
     */
    suspend fun deleteConfig(flagId: Int, tenantId: String? = null): Boolean {
        return anomalyAlertRepository.deleteConfig(flagId, tenantId)
    }
    
    /**
     * Clean up old alerts
     */
    suspend fun cleanupOldAlerts(olderThanMs: Long, tenantId: String? = null): Int {
        return anomalyAlertRepository.deleteOlderThan(olderThanMs, tenantId)
    }
}

/**
 * Detection report for a flag
 */
data class DetectionReport(
    val flagId: Int,
    val anomaliesDetected: List<AnomalyAlert>,
    val skipped: Boolean = false,
    val skipReason: String? = null
) {
    val anomalyCount: Int get() = anomaliesDetected.size
    val hasAnomalies: Boolean get() = anomaliesDetected.isNotEmpty()
    val criticalAnomalies: List<AnomalyAlert> get() = anomaliesDetected.filter { it.severity == AnomalyAlert.Severity.CRITICAL }
}
