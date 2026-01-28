package flagent.domain.repository

import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import flagent.domain.entity.MetricOverviewRecord

/**
 * IMetricsRepository - interface for metrics data access
 * 
 * Domain layer - defines contract for infrastructure layer
 */
interface IMetricsRepository {
    /**
     * Save a metric data point
     */
    suspend fun save(metric: MetricDataPoint): MetricDataPoint
    
    /**
     * Save multiple metric data points in batch
     */
    suspend fun saveBatch(metrics: List<MetricDataPoint>): List<MetricDataPoint>
    
    /**
     * Find metrics by flag ID within time range
     */
    suspend fun findByFlagId(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): List<MetricDataPoint>
    
    /**
     * Find metrics by flag key within time range
     */
    suspend fun findByFlagKey(
        flagKey: String,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): List<MetricDataPoint>
    
    /**
     * Get aggregated metrics for a flag within time window
     */
    suspend fun getAggregation(
        flagId: Int,
        metricType: MetricDataPoint.MetricType,
        windowStartMs: Long,
        windowEndMs: Long,
        variantId: Int? = null,
        tenantId: String? = null
    ): MetricAggregation?
    
    /**
     * Get aggregated metrics for multiple flags
     */
    suspend fun getAggregations(
        flagIds: List<Int>,
        metricType: MetricDataPoint.MetricType,
        windowStartMs: Long,
        windowEndMs: Long,
        tenantId: String? = null
    ): List<MetricAggregation>
    
    /**
     * Delete old metrics (cleanup)
     */
    suspend fun deleteOlderThan(timestamp: Long, tenantId: String? = null): Int
    
    /**
     * Count metrics for a flag
     */
    suspend fun countByFlagId(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): Int

    /**
     * Find metrics in time range (timestamp, flagId, flagKey only) for global overview.
     */
    suspend fun findMetricsInTimeRange(
        startTime: Long,
        endTime: Long,
        tenantId: String? = null
    ): List<MetricOverviewRecord>
}
