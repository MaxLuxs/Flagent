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
    LATENCY_MS,
    RESPONSE_TIME_MS,
    CONVERSION_RATE,
    CRASH_RATE,
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

/** Global metrics overview: time series + top flags (matches backend GET /api/v1/metrics/overview) */
@Serializable
data class GlobalMetricsOverviewResponse(
    val totalEvaluations: Long,
    val uniqueFlags: Int,
    val topFlags: List<TopFlagEntryResponse>,
    val timeSeries: List<TimeSeriesEntryResponse>
)

@Serializable
data class TopFlagEntryResponse(
    val flagId: Int,
    val flagKey: String,
    val evaluationCount: Long
)

@Serializable
data class TimeSeriesEntryResponse(
    val timestamp: Long,
    val count: Long
)

/** Per-flag evaluation stats from Core (OSS) - API evaluation count only. */
@Serializable
data class FlagEvaluationStatsResponse(
    val flagId: Int,
    val evaluationCount: Long,
    val timeSeries: List<TimeSeriesEntryResponse>
)

/** @deprecated Use TimeSeriesEntryResponse */
@Serializable
data class TimeBucketCountResponse(
    val bucketStartMs: Long,
    val count: Int
)

/** @deprecated Use TopFlagEntryResponse */
@Serializable
data class FlagMetricCountResponse(
    val flagId: Int,
    val flagKey: String,
    val count: Int
)

/** Per-variant conversion stats for A/B experiment insights */
@Serializable
data class VariantConversionStatsResponse(
    val variantId: Int,
    val variantKey: String? = null,
    val sampleSize: Int,
    val conversions: Int,
    val conversionRate: Double,
    val confidenceIntervalLow: Double,
    val confidenceIntervalHigh: Double
)

/** A/B experiment insights: conversion by variant, significance, recommendation */
@Serializable
data class ExperimentInsightsResponse(
    val flagId: Int,
    val flagKey: String,
    val variantStats: List<VariantConversionStatsResponse>,
    val winnerVariantId: Int? = null,
    val pValue: Double? = null,
    val isSignificant: Boolean,
    val recommendation: String,
    val windowStartMs: Long,
    val windowEndMs: Long
)
