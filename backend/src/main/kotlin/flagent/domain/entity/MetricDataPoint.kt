package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * MetricDataPoint - represents a single metric data point for a flag evaluation
 * 
 * Used for anomaly detection and smart rollouts
 * Domain entity - no framework dependencies
 */
@Serializable
data class MetricDataPoint(
    val id: Long = 0,
    val flagId: Int,
    val flagKey: String,
    val segmentId: Int?,
    val variantId: Int?,
    val variantKey: String?,
    val metricType: MetricType,
    val metricValue: Double,
    val timestamp: Long,
    val entityId: String?,
    val tenantId: String? = null
) {
    /**
     * MetricType - type of metric collected
     */
    enum class MetricType {
        SUCCESS_RATE,      // Success rate (0.0 - 1.0)
        ERROR_RATE,        // Error rate (0.0 - 1.0)
        CONVERSION_RATE,   // Conversion rate (0.0 - 1.0)
        LATENCY_MS,        // Latency in milliseconds
        RESPONSE_TIME_MS,  // Response time in milliseconds
        CUSTOM             // Custom metric
    }
    
    /**
     * Check if metric is critical (indicates potential issue)
     */
    fun isCritical(threshold: Double): Boolean {
        return when (metricType) {
            MetricType.ERROR_RATE -> metricValue > threshold
            MetricType.SUCCESS_RATE -> metricValue < threshold
            MetricType.LATENCY_MS, MetricType.RESPONSE_TIME_MS -> metricValue > threshold
            MetricType.CONVERSION_RATE -> metricValue < threshold
            MetricType.CUSTOM -> false // Custom metrics need custom logic
        }
    }
}

/**
 * MetricAggregation - aggregated metrics for a time window
 */
@Serializable
data class MetricAggregation(
    val flagId: Int,
    val flagKey: String,
    val variantId: Int?,
    val variantKey: String?,
    val metricType: MetricDataPoint.MetricType,
    val avgValue: Double,
    val minValue: Double,
    val maxValue: Double,
    val stdDev: Double,
    val count: Int,
    val windowStartMs: Long,
    val windowEndMs: Long,
    val tenantId: String? = null
) {
    /**
     * Calculate Z-score for a given value
     * Used for anomaly detection
     */
    fun zScore(value: Double): Double {
        return if (stdDev > 0) {
            (value - avgValue) / stdDev
        } else {
            0.0
        }
    }
    
    /**
     * Check if value is anomalous (using Z-score)
     * Default threshold: 3 standard deviations
     */
    fun isAnomaly(value: Double, threshold: Double = 3.0): Boolean {
        return kotlin.math.abs(zScore(value)) > threshold
    }
}

/**
 * Minimal metric record for global overview aggregation (time series, top flags).
 */
data class MetricOverviewRecord(
    val timestamp: Long,
    val flagId: Int,
    val flagKey: String
)
