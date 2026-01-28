package flagent.domain.usecase

import flagent.domain.entity.MetricDataPoint

/**
 * CollectMetricUseCase - validates and prepares metrics before storage
 * 
 * Ensures metric data integrity and consistency
 * Domain layer - no framework dependencies
 */
class CollectMetricUseCase {
    /**
     * Collection result
     */
    data class CollectionResult(
        val success: Boolean,
        val metric: MetricDataPoint?,
        val error: String?
    )
    
    /**
     * Validate and prepare metric for collection
     * 
     * @param metric Metric data point to validate
     * @return Collection result with validated metric or error
     */
    fun invoke(metric: MetricDataPoint): CollectionResult {
        // Validate flag ID
        if (metric.flagId <= 0) {
            return CollectionResult(
                success = false,
                metric = null,
                error = "Invalid flag ID: ${metric.flagId}"
            )
        }
        
        // Validate flag key
        if (metric.flagKey.isBlank()) {
            return CollectionResult(
                success = false,
                metric = null,
                error = "Flag key cannot be blank"
            )
        }
        
        // Validate metric value
        if (!isValidMetricValue(metric)) {
            return CollectionResult(
                success = false,
                metric = null,
                error = "Invalid metric value: ${metric.metricValue} for type ${metric.metricType}"
            )
        }
        
        // Validate timestamp
        if (metric.timestamp <= 0) {
            return CollectionResult(
                success = false,
                metric = null,
                error = "Invalid timestamp: ${metric.timestamp}"
            )
        }
        
        // Normalize metric value if needed
        val normalizedMetric = normalizeMetric(metric)
        
        return CollectionResult(
            success = true,
            metric = normalizedMetric,
            error = null
        )
    }
    
    /**
     * Validate metric value based on type
     */
    private fun isValidMetricValue(metric: MetricDataPoint): Boolean {
        return when (metric.metricType) {
            MetricDataPoint.MetricType.SUCCESS_RATE,
            MetricDataPoint.MetricType.ERROR_RATE,
            MetricDataPoint.MetricType.CONVERSION_RATE -> {
                // Rate metrics should be between 0.0 and 1.0
                metric.metricValue in 0.0..1.0
            }
            
            MetricDataPoint.MetricType.LATENCY_MS,
            MetricDataPoint.MetricType.RESPONSE_TIME_MS -> {
                // Latency metrics should be non-negative
                metric.metricValue >= 0.0
            }
            
            MetricDataPoint.MetricType.CUSTOM -> {
                // Custom metrics can be any value
                true
            }
        }
    }
    
    /**
     * Normalize metric value (e.g., convert percentages to 0-1 range)
     */
    private fun normalizeMetric(metric: MetricDataPoint): MetricDataPoint {
        val normalizedValue = when (metric.metricType) {
            MetricDataPoint.MetricType.SUCCESS_RATE,
            MetricDataPoint.MetricType.ERROR_RATE,
            MetricDataPoint.MetricType.CONVERSION_RATE -> {
                // If value is > 1.0, assume it's a percentage (0-100) and convert to 0-1
                if (metric.metricValue > 1.0 && metric.metricValue <= 100.0) {
                    metric.metricValue / 100.0
                } else {
                    metric.metricValue
                }
            }
            else -> metric.metricValue
        }
        
        return if (normalizedValue != metric.metricValue) {
            metric.copy(metricValue = normalizedValue)
        } else {
            metric
        }
    }
    
    /**
     * Batch validate and prepare metrics
     */
    fun invokeBatch(metrics: List<MetricDataPoint>): BatchCollectionResult {
        val results = metrics.map { invoke(it) }
        val successful = results.filter { it.success }.mapNotNull { it.metric }
        val failed = results.filter { !it.success }
        
        return BatchCollectionResult(
            successful = successful,
            failed = failed.map { it.error ?: "Unknown error" },
            totalCount = metrics.size
        )
    }
    
    /**
     * Batch collection result
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
}
