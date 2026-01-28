package flagent.domain.usecase

import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import flagent.domain.entity.RolloutDecision
import flagent.domain.entity.SmartRolloutConfig

/**
 * SmartRolloutUseCase - makes decisions for smart/automatic rollouts
 * 
 * Analyzes metrics and determines if rollout should continue, pause, or rollback
 * Domain layer - no framework dependencies
 */
class SmartRolloutUseCase {
    /**
     * Decision result with detailed reasoning
     */
    data class DecisionResult(
        val decision: RolloutDecision,
        val config: SmartRolloutConfig,
        val metricsUsed: MetricsSummary
    )
    
    /**
     * Summary of metrics used for decision
     */
    data class MetricsSummary(
        val successRate: Double?,
        val errorRate: Double?,
        val conversionRate: Double?,
        val latencyMs: Double?,
        val sampleSize: Int,
        val timeSinceLastIncrementMs: Long?
    )
    
    /**
     * Make rollout decision based on current metrics
     * 
     * @param config Smart rollout configuration
     * @param successRateAgg Success rate aggregation
     * @param errorRateAgg Error rate aggregation
     * @param conversionRateAgg Optional conversion rate aggregation
     * @param hasAnomaly Whether anomaly was detected
     * @return Decision result with action to take
     */
    fun invoke(
        config: SmartRolloutConfig,
        successRateAgg: MetricAggregation?,
        errorRateAgg: MetricAggregation?,
        conversionRateAgg: MetricAggregation? = null,
        hasAnomaly: Boolean = false
    ): DecisionResult {
        val now = System.currentTimeMillis()
        
        // Extract metrics
        val successRate = successRateAgg?.avgValue
        val errorRate = errorRateAgg?.avgValue
        val conversionRate = conversionRateAgg?.avgValue
        val sampleSize = maxOf(
            successRateAgg?.count ?: 0,
            errorRateAgg?.count ?: 0,
            conversionRateAgg?.count ?: 0
        )
        
        val timeSinceLastIncrement = config.lastIncrementAt?.let { now - it }
        
        val metricsSummary = MetricsSummary(
            successRate = successRate,
            errorRate = errorRate,
            conversionRate = conversionRate,
            latencyMs = null, // Can add latency if needed
            sampleSize = sampleSize,
            timeSinceLastIncrementMs = timeSinceLastIncrement
        )
        
        // Check if rollout is disabled
        if (!config.enabled) {
            return DecisionResult(
                decision = RolloutDecision(
                    action = RolloutDecision.Action.PAUSE,
                    reason = "Smart rollout is disabled",
                    newRolloutPercent = config.currentRolloutPercent
                ),
                config = config,
                metricsUsed = metricsSummary
            )
        }
        
        // Check if already completed
        if (config.status == SmartRolloutConfig.RolloutStatus.COMPLETED) {
            return DecisionResult(
                decision = RolloutDecision(
                    action = RolloutDecision.Action.COMPLETE,
                    reason = "Rollout already completed",
                    newRolloutPercent = config.currentRolloutPercent
                ),
                config = config,
                metricsUsed = metricsSummary
            )
        }
        
        // Check for anomaly
        if (hasAnomaly) {
            return handleAnomaly(config, metricsSummary)
        }
        
        // Check if should rollback based on metrics
        if (successRate != null && errorRate != null) {
            if (config.shouldRollback(successRate, errorRate)) {
                return rollback(config, metricsSummary, "Metrics below threshold")
            }
        }
        
        // Check if can increment
        if (successRate != null && errorRate != null && timeSinceLastIncrement != null) {
            val canIncrement = config.canIncrement(
                currentSampleSize = sampleSize,
                currentSuccessRate = successRate,
                currentErrorRate = errorRate,
                timeSinceLastIncrementMs = timeSinceLastIncrement
            )
            
            if (canIncrement) {
                return increment(config, metricsSummary)
            }
        }
        
        // Check if reached target
        if (config.currentRolloutPercent >= config.targetRolloutPercent) {
            return complete(config, metricsSummary)
        }
        
        // Default: pause and wait for more data
        return pause(config, metricsSummary, determineWaitReason(config, metricsSummary))
    }
    
    /**
     * Handle anomaly detection
     */
    private fun handleAnomaly(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary
    ): DecisionResult {
        return if (config.rollbackOnAnomaly) {
            rollback(config, metricsSummary, "Anomaly detected, rolling back")
        } else if (config.pauseOnAnomaly) {
            pause(config, metricsSummary, "Anomaly detected, pausing rollout")
        } else {
            pause(config, metricsSummary, "Anomaly detected, monitoring")
        }
    }
    
    /**
     * Increment rollout percentage
     */
    private fun increment(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary
    ): DecisionResult {
        val newPercent = config.nextRolloutPercent()
        
        return DecisionResult(
            decision = RolloutDecision(
                action = RolloutDecision.Action.INCREMENT,
                reason = "Metrics meet thresholds: success=${String.format("%.2f%%", (metricsSummary.successRate ?: 0.0) * 100)}, error=${String.format("%.2f%%", (metricsSummary.errorRate ?: 0.0) * 100)}, samples=${metricsSummary.sampleSize}",
                newRolloutPercent = newPercent,
                confidence = calculateConfidence(config, metricsSummary)
            ),
            config = config.copy(
                currentRolloutPercent = newPercent,
                lastIncrementAt = System.currentTimeMillis(),
                status = if (newPercent >= config.targetRolloutPercent) {
                    SmartRolloutConfig.RolloutStatus.COMPLETED
                } else {
                    SmartRolloutConfig.RolloutStatus.IN_PROGRESS
                }
            ),
            metricsUsed = metricsSummary
        )
    }
    
    /**
     * Pause rollout
     */
    private fun pause(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary,
        reason: String
    ): DecisionResult {
        return DecisionResult(
            decision = RolloutDecision(
                action = RolloutDecision.Action.PAUSE,
                reason = reason,
                newRolloutPercent = config.currentRolloutPercent
            ),
            config = config.copy(
                status = SmartRolloutConfig.RolloutStatus.PAUSED
            ),
            metricsUsed = metricsSummary
        )
    }
    
    /**
     * Rollback rollout
     */
    private fun rollback(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary,
        reason: String
    ): DecisionResult {
        // Rollback to previous percentage (decrease by increment)
        val newPercent = maxOf(0, config.currentRolloutPercent - config.incrementPercent)
        
        return DecisionResult(
            decision = RolloutDecision(
                action = RolloutDecision.Action.ROLLBACK,
                reason = reason,
                newRolloutPercent = newPercent,
                confidence = 1.0 // High confidence in rollback decision
            ),
            config = config.copy(
                currentRolloutPercent = newPercent,
                status = SmartRolloutConfig.RolloutStatus.FAILED
            ),
            metricsUsed = metricsSummary
        )
    }
    
    /**
     * Complete rollout
     */
    private fun complete(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary
    ): DecisionResult {
        return DecisionResult(
            decision = RolloutDecision(
                action = RolloutDecision.Action.COMPLETE,
                reason = "Rollout reached target: ${config.currentRolloutPercent}%",
                newRolloutPercent = config.currentRolloutPercent,
                confidence = 1.0
            ),
            config = config.copy(
                status = SmartRolloutConfig.RolloutStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            ),
            metricsUsed = metricsSummary
        )
    }
    
    /**
     * Determine reason for waiting
     */
    private fun determineWaitReason(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary
    ): String {
        val reasons = mutableListOf<String>()
        
        // Check sample size
        if (metricsSummary.sampleSize < config.minSampleSize) {
            reasons.add("insufficient samples: ${metricsSummary.sampleSize} < ${config.minSampleSize}")
        }
        
        // Check time since last increment
        val timeSinceLastIncrement = metricsSummary.timeSinceLastIncrementMs
        if (timeSinceLastIncrement != null && timeSinceLastIncrement < config.incrementIntervalMs) {
            val remainingMs = config.incrementIntervalMs - timeSinceLastIncrement
            val remainingMinutes = remainingMs / 60000
            reasons.add("waiting for interval: ${remainingMinutes}min remaining")
        }
        
        // Check metrics
        val successRate = metricsSummary.successRate
        val errorRate = metricsSummary.errorRate
        
        if (successRate != null && successRate < config.successRateThreshold) {
            reasons.add("low success rate: ${String.format("%.2f%%", successRate * 100)}")
        }
        
        if (errorRate != null && errorRate > config.errorRateThreshold) {
            reasons.add("high error rate: ${String.format("%.2f%%", errorRate * 100)}")
        }
        
        return if (reasons.isEmpty()) {
            "Waiting for optimal conditions"
        } else {
            "Waiting: ${reasons.joinToString(", ")}"
        }
    }
    
    /**
     * Calculate confidence in increment decision
     * Based on sample size and metric stability
     */
    private fun calculateConfidence(
        config: SmartRolloutConfig,
        metricsSummary: MetricsSummary
    ): Double {
        val sampleRatio = metricsSummary.sampleSize.toDouble() / (config.minSampleSize * 2.0)
        val sampleConfidence = minOf(1.0, sampleRatio)
        
        val successRate = metricsSummary.successRate ?: 0.0
        val errorRate = metricsSummary.errorRate ?: 1.0
        
        val successMargin = (successRate - config.successRateThreshold) / config.successRateThreshold
        val errorMargin = (config.errorRateThreshold - errorRate) / config.errorRateThreshold
        
        val metricsConfidence = (successMargin + errorMargin) / 2.0
        
        return (sampleConfidence * 0.5 + metricsConfidence * 0.5).coerceIn(0.0, 1.0)
    }
}
