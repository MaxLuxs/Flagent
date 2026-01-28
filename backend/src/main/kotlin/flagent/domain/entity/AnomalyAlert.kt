package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * AnomalyAlert - represents an anomaly detection alert for a flag
 * 
 * Triggers automatic actions like kill switch or rollback
 * Domain entity - no framework dependencies
 */
@Serializable
data class AnomalyAlert(
    val id: Int = 0,
    val flagId: Int,
    val flagKey: String,
    val variantId: Int?,
    val variantKey: String?,
    val anomalyType: AnomalyType,
    val severity: Severity,
    val detectedAt: Long,
    val metricType: MetricDataPoint.MetricType,
    val metricValue: Double,
    val expectedValue: Double,
    val zScore: Double,
    val message: String,
    val actionTaken: ActionTaken? = null,
    val actionTakenAt: Long? = null,
    val resolved: Boolean = false,
    val resolvedAt: Long? = null,
    val tenantId: String? = null
) {
    /**
     * AnomalyType - type of anomaly detected
     */
    enum class AnomalyType {
        HIGH_ERROR_RATE,        // Error rate spike
        LOW_SUCCESS_RATE,       // Success rate drop
        HIGH_LATENCY,           // Latency spike
        LOW_CONVERSION_RATE,    // Conversion rate drop
        STATISTICAL_OUTLIER,    // Statistical outlier (Z-score)
        THRESHOLD_EXCEEDED      // Threshold exceeded
    }
    
    /**
     * Severity - severity level of anomaly
     */
    enum class Severity {
        LOW,      // Minor issue, monitor
        MEDIUM,   // Significant issue, alert
        HIGH,     // Critical issue, take action
        CRITICAL  // Emergency, immediate action
    }
    
    /**
     * ActionTaken - action taken in response to anomaly
     */
    enum class ActionTaken {
        NONE,              // No action taken
        ALERT_SENT,        // Alert sent to team
        ROLLOUT_PAUSED,    // Rollout paused
        ROLLOUT_DECREASED, // Rollout percentage decreased
        FLAG_DISABLED,     // Flag disabled (kill switch)
        VARIANT_DISABLED   // Variant disabled
    }
    
    /**
     * Check if action is required based on severity
     */
    fun requiresAction(): Boolean {
        return severity == Severity.HIGH || severity == Severity.CRITICAL
    }
    
    /**
     * Get recommended action based on anomaly type and severity
     */
    fun recommendedAction(): ActionTaken {
        return when (severity) {
            Severity.LOW -> ActionTaken.ALERT_SENT
            Severity.MEDIUM -> ActionTaken.ROLLOUT_PAUSED
            Severity.HIGH -> ActionTaken.ROLLOUT_DECREASED
            Severity.CRITICAL -> when (anomalyType) {
                AnomalyType.HIGH_ERROR_RATE, AnomalyType.LOW_SUCCESS_RATE -> ActionTaken.FLAG_DISABLED
                else -> ActionTaken.ROLLOUT_PAUSED
            }
        }
    }
}

/**
 * AnomalyDetectionConfig - configuration for anomaly detection
 */
@Serializable
data class AnomalyDetectionConfig(
    val flagId: Int,
    val enabled: Boolean = true,
    val zScoreThreshold: Double = 3.0,           // Z-score threshold for statistical outliers
    val errorRateThreshold: Double = 0.1,        // 10% error rate threshold
    val successRateThreshold: Double = 0.8,      // 80% success rate threshold
    val latencyThresholdMs: Double = 1000.0,     // 1 second latency threshold
    val conversionRateThreshold: Double = 0.05,  // 5% conversion rate threshold
    val minSampleSize: Int = 100,                // Minimum sample size for detection
    val windowSizeMs: Long = 300_000,            // 5 minute window
    val autoKillSwitch: Boolean = false,         // Auto disable flag on critical anomaly
    val autoRollback: Boolean = false,           // Auto rollback on anomaly
    val tenantId: String? = null
)
