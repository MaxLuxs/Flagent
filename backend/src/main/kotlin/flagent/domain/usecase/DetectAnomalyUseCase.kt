package flagent.domain.usecase

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig
import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import kotlin.math.abs

/**
 * DetectAnomalyUseCase - detects anomalies in flag metrics
 * 
 * Uses statistical methods (Z-score) and threshold-based detection
 * Domain layer - no framework dependencies
 */
class DetectAnomalyUseCase {
    /**
     * Detection result
     */
    data class DetectionResult(
        val isAnomaly: Boolean,
        val alert: AnomalyAlert?,
        val reason: String
    )
    
    /**
     * Detect anomalies in recent metrics using aggregation
     * 
     * @param config Anomaly detection configuration
     * @param aggregation Current metrics aggregation
     * @param recentMetrics Recent individual metrics for analysis
     * @return Detection result with alert if anomaly found
     */
    fun invoke(
        config: AnomalyDetectionConfig,
        aggregation: MetricAggregation,
        recentMetrics: List<MetricDataPoint>
    ): DetectionResult {
        // Check if enabled
        if (!config.enabled) {
            return DetectionResult(
                isAnomaly = false,
                alert = null,
                reason = "Anomaly detection is disabled for this flag"
            )
        }
        
        // Check minimum sample size
        if (aggregation.count < config.minSampleSize) {
            return DetectionResult(
                isAnomaly = false,
                alert = null,
                reason = "Insufficient sample size: ${aggregation.count} < ${config.minSampleSize}"
            )
        }
        
        // Statistical outlier detection (Z-score)
        val zScoreResult = detectZScoreAnomaly(config, aggregation)
        if (zScoreResult != null) {
            return zScoreResult
        }
        
        // Threshold-based detection
        val thresholdResult = detectThresholdAnomaly(config, aggregation)
        if (thresholdResult != null) {
            return thresholdResult
        }
        
        // No anomaly detected
        return DetectionResult(
            isAnomaly = false,
            alert = null,
            reason = "No anomaly detected"
        )
    }
    
    /**
     * Detect statistical outliers using Z-score
     */
    private fun detectZScoreAnomaly(
        config: AnomalyDetectionConfig,
        aggregation: MetricAggregation
    ): DetectionResult? {
        // Check if current average is anomalous compared to expected range
        val zScore = abs(aggregation.zScore(aggregation.avgValue))
        
        if (zScore > config.zScoreThreshold) {
            val severity = calculateSeverity(zScore, config.zScoreThreshold)
            
            val alert = AnomalyAlert(
                flagId = aggregation.flagId,
                flagKey = aggregation.flagKey,
                variantId = aggregation.variantId,
                variantKey = aggregation.variantKey,
                anomalyType = AnomalyAlert.AnomalyType.STATISTICAL_OUTLIER,
                severity = severity,
                detectedAt = System.currentTimeMillis(),
                metricType = aggregation.metricType,
                metricValue = aggregation.avgValue,
                expectedValue = aggregation.avgValue, // Using avg as expected for now
                zScore = zScore,
                message = "Statistical outlier detected: Z-score ${String.format("%.2f", zScore)} exceeds threshold ${config.zScoreThreshold}",
                tenantId = aggregation.tenantId
            )
            
            return DetectionResult(
                isAnomaly = true,
                alert = alert,
                reason = "Z-score ${String.format("%.2f", zScore)} > ${config.zScoreThreshold}"
            )
        }
        
        return null
    }
    
    /**
     * Detect threshold violations
     */
    private fun detectThresholdAnomaly(
        config: AnomalyDetectionConfig,
        aggregation: MetricAggregation
    ): DetectionResult? {
        val avgValue = aggregation.avgValue
        
        return when (aggregation.metricType) {
            MetricDataPoint.MetricType.ERROR_RATE -> {
                if (avgValue > config.errorRateThreshold) {
                    createThresholdAlert(
                        config = config,
                        aggregation = aggregation,
                        anomalyType = AnomalyAlert.AnomalyType.HIGH_ERROR_RATE,
                        threshold = config.errorRateThreshold,
                        message = "High error rate: ${String.format("%.2f%%", avgValue * 100)} > ${String.format("%.2f%%", config.errorRateThreshold * 100)}"
                    )
                } else null
            }
            
            MetricDataPoint.MetricType.SUCCESS_RATE -> {
                if (avgValue < config.successRateThreshold) {
                    createThresholdAlert(
                        config = config,
                        aggregation = aggregation,
                        anomalyType = AnomalyAlert.AnomalyType.LOW_SUCCESS_RATE,
                        threshold = config.successRateThreshold,
                        message = "Low success rate: ${String.format("%.2f%%", avgValue * 100)} < ${String.format("%.2f%%", config.successRateThreshold * 100)}"
                    )
                } else null
            }
            
            MetricDataPoint.MetricType.LATENCY_MS, MetricDataPoint.MetricType.RESPONSE_TIME_MS -> {
                if (avgValue > config.latencyThresholdMs) {
                    createThresholdAlert(
                        config = config,
                        aggregation = aggregation,
                        anomalyType = AnomalyAlert.AnomalyType.HIGH_LATENCY,
                        threshold = config.latencyThresholdMs,
                        message = "High latency: ${String.format("%.2f", avgValue)}ms > ${config.latencyThresholdMs}ms"
                    )
                } else null
            }
            
            MetricDataPoint.MetricType.CONVERSION_RATE -> {
                if (avgValue < config.conversionRateThreshold) {
                    createThresholdAlert(
                        config = config,
                        aggregation = aggregation,
                        anomalyType = AnomalyAlert.AnomalyType.LOW_CONVERSION_RATE,
                        threshold = config.conversionRateThreshold,
                        message = "Low conversion rate: ${String.format("%.2f%%", avgValue * 100)} < ${String.format("%.2f%%", config.conversionRateThreshold * 100)}"
                    )
                } else null
            }
            
            MetricDataPoint.MetricType.CUSTOM -> null // Custom metrics need custom logic
        }
    }
    
    /**
     * Create threshold violation alert
     */
    private fun createThresholdAlert(
        config: AnomalyDetectionConfig,
        aggregation: MetricAggregation,
        anomalyType: AnomalyAlert.AnomalyType,
        threshold: Double,
        message: String
    ): DetectionResult {
        val severity = calculateThresholdSeverity(
            value = aggregation.avgValue,
            threshold = threshold,
            metricType = aggregation.metricType
        )
        
        val alert = AnomalyAlert(
            flagId = aggregation.flagId,
            flagKey = aggregation.flagKey,
            variantId = aggregation.variantId,
            variantKey = aggregation.variantKey,
            anomalyType = anomalyType,
            severity = severity,
            detectedAt = System.currentTimeMillis(),
            metricType = aggregation.metricType,
            metricValue = aggregation.avgValue,
            expectedValue = threshold,
            zScore = 0.0, // Not using Z-score for threshold detection
            message = message,
            tenantId = aggregation.tenantId
        )
        
        return DetectionResult(
            isAnomaly = true,
            alert = alert,
            reason = message
        )
    }
    
    /**
     * Calculate severity based on Z-score
     */
    private fun calculateSeverity(zScore: Double, threshold: Double): AnomalyAlert.Severity {
        return when {
            zScore > threshold * 2.0 -> AnomalyAlert.Severity.CRITICAL
            zScore > threshold * 1.5 -> AnomalyAlert.Severity.HIGH
            zScore > threshold * 1.2 -> AnomalyAlert.Severity.MEDIUM
            else -> AnomalyAlert.Severity.LOW
        }
    }
    
    /**
     * Calculate severity based on threshold violation
     */
    private fun calculateThresholdSeverity(
        value: Double,
        threshold: Double,
        metricType: MetricDataPoint.MetricType
    ): AnomalyAlert.Severity {
        val ratio = when (metricType) {
            MetricDataPoint.MetricType.ERROR_RATE,
            MetricDataPoint.MetricType.LATENCY_MS,
            MetricDataPoint.MetricType.RESPONSE_TIME_MS -> {
                // Higher is worse
                value / threshold
            }
            MetricDataPoint.MetricType.SUCCESS_RATE,
            MetricDataPoint.MetricType.CONVERSION_RATE -> {
                // Lower is worse
                threshold / value
            }
            MetricDataPoint.MetricType.CUSTOM -> 1.0
        }
        
        return when {
            ratio > 3.0 -> AnomalyAlert.Severity.CRITICAL
            ratio > 2.0 -> AnomalyAlert.Severity.HIGH
            ratio > 1.5 -> AnomalyAlert.Severity.MEDIUM
            else -> AnomalyAlert.Severity.LOW
        }
    }
}
