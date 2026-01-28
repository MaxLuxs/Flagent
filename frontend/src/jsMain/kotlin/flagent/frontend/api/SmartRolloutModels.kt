package flagent.frontend.api

import kotlinx.serialization.Serializable

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
    val conversionRateThreshold: Double,
    val minSampleSize: Int,
    val autoRollback: Boolean,
    val rollbackOnAnomaly: Boolean,
    val pauseOnAnomaly: Boolean,
    val notifyOnIncrement: Boolean,
    val lastIncrementAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RolloutHistoryEntry(
    val id: Int,
    val configId: Int,
    val decision: String,
    val oldPercent: Int,
    val newPercent: Int,
    val metrics: Map<String, Double>?,
    val timestamp: Long
)
