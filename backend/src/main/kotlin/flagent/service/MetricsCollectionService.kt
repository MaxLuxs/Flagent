package flagent.service

import flagent.domain.entity.MetricDataPoint
import flagent.domain.repository.IMetricsRepository
import flagent.domain.usecase.CollectMetricUseCase
import kotlinx.serialization.Serializable

/**
 * MetricsCollectionService - collects and stores metrics for flags
 * 
 * Service layer - orchestrates use cases and repositories
 */
class MetricsCollectionService(
    private val metricsRepository: IMetricsRepository,
    private val collectMetricUseCase: CollectMetricUseCase = CollectMetricUseCase()
) {
    /**
     * Collect a single metric
     */
    suspend fun collectMetric(metric: MetricDataPoint): Result<MetricDataPoint> {
        val result = collectMetricUseCase.invoke(metric)
        
        return if (result.success && result.metric != null) {
            try {
                val saved = metricsRepository.save(result.metric)
                Result.success(saved)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.error ?: "Unknown error"))
        }
    }
    
    /**
     * Collect multiple metrics in batch
     */
    suspend fun collectMetricsBatch(metrics: List<MetricDataPoint>): BatchCollectionResult {
        val validationResult = collectMetricUseCase.invokeBatch(metrics)
        
        return try {
            val saved = if (validationResult.successful.isNotEmpty()) {
                metricsRepository.saveBatch(validationResult.successful)
            } else {
                emptyList()
            }
            
            BatchCollectionResult(
                successful = saved,
                failed = validationResult.failed,
                totalCount = metrics.size
            )
        } catch (e: Exception) {
            BatchCollectionResult(
                successful = emptyList(),
                failed = validationResult.failed + listOf("Database error: ${e.message}"),
                totalCount = metrics.size
            )
        }
    }
    
    /**
     * Get metrics for a flag within time range
     */
    suspend fun getMetricsByFlagId(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): List<MetricDataPoint> {
        return metricsRepository.findByFlagId(
            flagId = flagId,
            startTime = startTime,
            endTime = endTime,
            metricType = metricType,
            variantId = variantId,
            tenantId = tenantId
        )
    }
    
    /**
     * Get metrics for a flag by key within time range
     */
    suspend fun getMetricsByFlagKey(
        flagKey: String,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): List<MetricDataPoint> {
        return metricsRepository.findByFlagKey(
            flagKey = flagKey,
            startTime = startTime,
            endTime = endTime,
            metricType = metricType,
            variantId = variantId,
            tenantId = tenantId
        )
    }
    
    /**
     * Get aggregated metrics for a flag
     */
    suspend fun getAggregation(
        flagId: Int,
        metricType: MetricDataPoint.MetricType,
        windowStartMs: Long,
        windowEndMs: Long,
        variantId: Int? = null,
        tenantId: String? = null
    ) = metricsRepository.getAggregation(
        flagId = flagId,
        metricType = metricType,
        windowStartMs = windowStartMs,
        windowEndMs = windowEndMs,
        variantId = variantId,
        tenantId = tenantId
    )
    
    /**
     * Clean up old metrics
     */
    suspend fun cleanupOldMetrics(olderThanMs: Long, tenantId: String? = null): Int {
        return metricsRepository.deleteOlderThan(olderThanMs, tenantId)
    }
    
    /**
     * Count metrics for a flag
     */
    suspend fun countMetrics(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): Int {
        return metricsRepository.countByFlagId(
            flagId = flagId,
            startTime = startTime,
            endTime = endTime,
            metricType = metricType,
            variantId = variantId,
            tenantId = tenantId
        )
    }

    /**
     * Get global metrics overview: time series (count per bucket) and top flags by count.
     * @param bucketMs time bucket size in milliseconds (e.g. 3600_000 for 1 hour)
     * @param topFlagsLimit max number of top flags to return
     */
    suspend fun getGlobalOverview(
        startTime: Long,
        endTime: Long,
        bucketMs: Long,
        topFlagsLimit: Int = 10,
        tenantId: String? = null
    ): GlobalMetricsOverview {
        val records = metricsRepository.findMetricsInTimeRange(startTime, endTime, tenantId)
        val totalCount = records.size

        val timeSeries = records
            .groupBy { (it.timestamp / bucketMs) * bucketMs }
            .map { (bucketStart, list) -> TimeBucketCount(bucketStart, list.size) }
            .sortedBy { it.bucketStartMs }

        val topFlags = records
            .groupBy { it.flagId to it.flagKey }
            .map { (key, list) -> FlagMetricCount(flagId = key.first, flagKey = key.second, count = list.size) }
            .sortedByDescending { it.count }
            .take(topFlagsLimit)

        return GlobalMetricsOverview(
            totalCount = totalCount.toLong(),
            timeSeries = timeSeries,
            topFlags = topFlags
        )
    }
}

/**
 * Time bucket with count for global overview.
 */
@Serializable
data class TimeBucketCount(
    val bucketStartMs: Long,
    val count: Int
)

/**
 * Flag metric count for top flags list.
 */
@Serializable
data class FlagMetricCount(
    val flagId: Int,
    val flagKey: String,
    val count: Int
)

/**
 * Global metrics overview response.
 */
@Serializable
data class GlobalMetricsOverview(
    val totalCount: Long,
    val timeSeries: List<TimeBucketCount>,
    val topFlags: List<FlagMetricCount>
)

/**
 * Result of batch metrics collection
 */
data class BatchCollectionResult(
    val successful: List<MetricDataPoint>,
    val failed: List<String>,
    val totalCount: Int
) {
    val successCount: Int get() = successful.size
    val failureCount: Int get() = failed.size
    val successRate: Double get() = if (totalCount > 0) successCount.toDouble() / totalCount else 0.0
}
