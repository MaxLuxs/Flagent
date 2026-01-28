package flagent.domain.usecase

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig
import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DetectAnomalyUseCaseTest {
    private val useCase = DetectAnomalyUseCase()
    
    @Test
    fun `should detect high error rate anomaly`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = true,
            errorRateThreshold = 0.05,
            minSampleSize = 10
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.15, // 15% error rate (above 5% threshold)
            minValue = 0.1,
            maxValue = 0.2,
            stdDev = 0.05,
            count = 100,
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(result.isAnomaly)
        assertNotNull(result.alert)
        assertEquals(AnomalyAlert.AnomalyType.HIGH_ERROR_RATE, result.alert.anomalyType)
    }
    
    @Test
    fun `should detect low success rate anomaly`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = true,
            successRateThreshold = 0.95,
            minSampleSize = 10
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.80, // 80% success rate (below 95% threshold)
            minValue = 0.75,
            maxValue = 0.85,
            stdDev = 0.05,
            count = 100,
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(result.isAnomaly)
        assertNotNull(result.alert)
        assertEquals(AnomalyAlert.AnomalyType.LOW_SUCCESS_RATE, result.alert.anomalyType)
    }
    
    @Test
    fun `should detect high latency anomaly`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = true,
            latencyThresholdMs = 500.0,
            minSampleSize = 10
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.LATENCY_MS,
            avgValue = 1200.0, // 1200ms (above 500ms threshold)
            minValue = 1000.0,
            maxValue = 1500.0,
            stdDev = 200.0,
            count = 100,
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(result.isAnomaly)
        assertNotNull(result.alert)
        assertEquals(AnomalyAlert.AnomalyType.HIGH_LATENCY, result.alert.anomalyType)
    }
    
    @Test
    fun `should not detect anomaly when metrics are normal`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = true,
            errorRateThreshold = 0.05,
            successRateThreshold = 0.95,
            minSampleSize = 10
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01, // 1% error rate (below 5% threshold)
            minValue = 0.005,
            maxValue = 0.02,
            stdDev = 0.005,
            count = 100,
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(!result.isAnomaly)
        assertEquals(null, result.alert)
    }
    
    @Test
    fun `should not detect anomaly when sample size is too small`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = true,
            errorRateThreshold = 0.05,
            minSampleSize = 100
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.15, // High error rate but insufficient samples
            minValue = 0.1,
            maxValue = 0.2,
            stdDev = 0.05,
            count = 50, // Only 50 samples (< 100 minimum)
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(!result.isAnomaly)
        assertEquals(null, result.alert)
    }
    
    @Test
    fun `should not detect anomaly when detection is disabled`() {
        // Given
        val config = AnomalyDetectionConfig(
            flagId = 1,
            enabled = false, // Disabled
            errorRateThreshold = 0.05,
            minSampleSize = 10
        )
        
        val aggregation = MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.15,
            minValue = 0.1,
            maxValue = 0.2,
            stdDev = 0.05,
            count = 100,
            windowStartMs = 0,
            windowEndMs = 1000
        )
        
        // When
        val result = useCase.invoke(config, aggregation, emptyList())
        
        // Then
        assertTrue(!result.isAnomaly)
        assertEquals(null, result.alert)
    }
}
