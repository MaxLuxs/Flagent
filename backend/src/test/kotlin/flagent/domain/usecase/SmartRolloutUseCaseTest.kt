package flagent.domain.usecase

import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import flagent.domain.entity.RolloutDecision
import flagent.domain.entity.SmartRolloutConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SmartRolloutUseCaseTest {
    private val useCase = SmartRolloutUseCase()
    
    @Test
    fun `should increment rollout when metrics are good`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 10,
            incrementPercent = 10,
            incrementIntervalMs = 3600_000,
            successRateThreshold = 0.95,
            errorRateThreshold = 0.05,
            minSampleSize = 100,
            lastIncrementAt = System.currentTimeMillis() - 3700_000 // More than 1 hour ago
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.98, // 98% success rate (above 95% threshold)
            count = 150
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01, // 1% error rate (below 5% threshold)
            count = 150
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg)
        
        // Then
        assertEquals(RolloutDecision.Action.INCREMENT, result.decision.action)
        assertEquals(20, result.decision.newRolloutPercent) // 10 + 10
        assertNotNull(result.metricsUsed.successRate)
        assertNotNull(result.metricsUsed.errorRate)
    }
    
    @Test
    fun `should rollback when error rate is too high`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 50,
            incrementPercent = 10,
            successRateThreshold = 0.95,
            errorRateThreshold = 0.05,
            autoRollback = true
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.80, // 80% success rate (below threshold)
            count = 150
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.15, // 15% error rate (above threshold)
            count = 150
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg)
        
        // Then
        assertEquals(RolloutDecision.Action.ROLLBACK, result.decision.action)
        assertEquals(40, result.decision.newRolloutPercent) // 50 - 10
    }
    
    @Test
    fun `should pause when sample size is insufficient`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 10,
            incrementPercent = 10,
            minSampleSize = 100,
            lastIncrementAt = System.currentTimeMillis() - 3700_000
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.98,
            count = 50 // Insufficient samples
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01,
            count = 50
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg)
        
        // Then
        assertEquals(RolloutDecision.Action.PAUSE, result.decision.action)
        assertEquals(10, result.decision.newRolloutPercent) // No change
    }
    
    @Test
    fun `should complete when target is reached`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 100, // Already at target
            incrementPercent = 10
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.98,
            count = 150
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01,
            count = 150
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg)
        
        // Then
        assertEquals(RolloutDecision.Action.COMPLETE, result.decision.action)
    }
    
    @Test
    fun `should pause when anomaly is detected and rollback on anomaly is enabled`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 50,
            incrementPercent = 10,
            rollbackOnAnomaly = true,
            pauseOnAnomaly = false,
            autoRollback = true
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.98,
            count = 150
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01,
            count = 150
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg, hasAnomaly = true)
        
        // Then
        assertEquals(RolloutDecision.Action.ROLLBACK, result.decision.action)
        assertEquals(40, result.decision.newRolloutPercent) // 50 - 10
    }
    
    @Test
    fun `should pause when increment interval has not passed`() {
        // Given
        val config = SmartRolloutConfig(
            id = 1,
            flagId = 1,
            segmentId = 1,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 10,
            incrementPercent = 10,
            incrementIntervalMs = 3600_000,
            minSampleSize = 100,
            lastIncrementAt = System.currentTimeMillis() - 1800_000 // Only 30 minutes ago
        )
        
        val successRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            avgValue = 0.98,
            count = 150
        )
        
        val errorRateAgg = createAggregation(
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            avgValue = 0.01,
            count = 150
        )
        
        // When
        val result = useCase.invoke(config, successRateAgg, errorRateAgg)
        
        // Then
        assertEquals(RolloutDecision.Action.PAUSE, result.decision.action)
    }
    
    private fun createAggregation(
        metricType: MetricDataPoint.MetricType,
        avgValue: Double,
        count: Int
    ): MetricAggregation {
        return MetricAggregation(
            flagId = 1,
            flagKey = "test_flag",
            variantId = null,
            variantKey = null,
            metricType = metricType,
            avgValue = avgValue,
            minValue = avgValue * 0.9,
            maxValue = avgValue * 1.1,
            stdDev = avgValue * 0.1,
            count = count,
            windowStartMs = 0,
            windowEndMs = 1000
        )
    }
}
