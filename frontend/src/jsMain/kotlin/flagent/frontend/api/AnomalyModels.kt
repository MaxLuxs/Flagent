package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class AnomalyDetectionConfigRequest(
    val flagId: Int,
    val enabled: Boolean? = null,
    val zScoreThreshold: Double? = null,
    val errorRateThreshold: Double? = null,
    val successRateThreshold: Double? = null,
    val latencyThresholdMs: Long? = null,
    val conversionRateThreshold: Double? = null,
    val minSampleSize: Int? = null,
    val windowSizeMs: Long? = null,
    val autoKillSwitch: Boolean? = null,
    val autoRollback: Boolean? = null
)

@Serializable
data class AnomalyDetectionConfig(
    val id: Int,
    val flagId: Int,
    val enabled: Boolean,
    val zScoreThreshold: Double,
    val errorRateThreshold: Double,
    val successRateThreshold: Double,
    val latencyThresholdMs: Long,
    val conversionRateThreshold: Double,
    val minSampleSize: Int,
    val windowSizeMs: Long,
    val autoKillSwitch: Boolean,
    val autoRollback: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class AnomalyAlertResponse(
    val id: Int,
    val flagId: Int,
    val metricType: String,
    val severity: AlertSeverity,
    val message: String,
    val actualValue: Double,
    val expectedValue: Double?,
    val threshold: Double?,
    val resolved: Boolean,
    val resolvedAt: Long?,
    val createdAt: Long
)

@Serializable
enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
