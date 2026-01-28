package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class MetricDataPointRequest(
    val flagId: Int? = null,
    val flagKey: String? = null,
    val segmentId: Int? = null,
    val variantId: Int? = null,
    val variantKey: String? = null,
    val metricType: MetricType,
    val metricValue: Double,
    val timestamp: Long? = null,
    val entityId: String? = null,
    val tenantId: String? = null
)

@Serializable
data class MetricDataPointResponse(
    val id: Int,
    val flagId: Int,
    val segmentId: Int?,
    val variantId: Int?,
    val metricType: MetricType,
    val metricValue: Double,
    val timestamp: Long,
    val entityId: String?,
    val tenantId: String?
)

@Serializable
enum class MetricType {
    SUCCESS_RATE,
    ERROR_RATE,
    LATENCY,
    CONVERSION_RATE,
    CUSTOM
}

@Serializable
data class MetricAggregationResponse(
    val flagId: Int,
    val metricType: MetricType,
    val count: Int,
    val sum: Double,
    val avg: Double,
    val min: Double,
    val max: Double,
    val windowStart: Long,
    val windowEnd: Long
)

/** Global metrics overview: time series + top flags */
@Serializable
data class GlobalMetricsOverviewResponse(
    val totalCount: Long,
    val timeSeries: List<TimeBucketCountResponse>,
    val topFlags: List<FlagMetricCountResponse>
)

@Serializable
data class TimeBucketCountResponse(
    val bucketStartMs: Long,
    val count: Int
)

@Serializable
data class FlagMetricCountResponse(
    val flagId: Int,
    val flagKey: String,
    val count: Int
)
