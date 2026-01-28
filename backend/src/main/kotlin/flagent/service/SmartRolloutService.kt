package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IMetricsRepository
import flagent.domain.repository.ISmartRolloutRepository
import flagent.domain.usecase.SmartRolloutUseCase

/**
 * SmartRolloutService - manages smart/automatic rollouts
 * 
 * Service layer - orchestrates smart rollout decisions and execution
 */
class SmartRolloutService(
    private val smartRolloutRepository: ISmartRolloutRepository,
    private val metricsRepository: IMetricsRepository,
    private val segmentService: SegmentService,
    private val slackNotificationService: SlackNotificationService? = null,
    private val smartRolloutUseCase: SmartRolloutUseCase = SmartRolloutUseCase()
) {
    /**
     * Create smart rollout config
     */
    suspend fun createConfig(config: SmartRolloutConfig): SmartRolloutConfig {
        return smartRolloutRepository.saveConfig(config)
    }
    
    /**
     * Update smart rollout config
     */
    suspend fun updateConfig(config: SmartRolloutConfig): SmartRolloutConfig {
        return smartRolloutRepository.updateConfig(config)
    }
    
    /**
     * Get config by ID
     */
    suspend fun getConfigById(id: Int, tenantId: String? = null): SmartRolloutConfig? {
        return smartRolloutRepository.findConfigById(id, tenantId)
    }
    
    /**
     * Get config by flag and segment
     */
    suspend fun getConfigByFlagAndSegment(
        flagId: Int,
        segmentId: Int,
        tenantId: String? = null
    ): SmartRolloutConfig? {
        return smartRolloutRepository.findConfigByFlagAndSegment(flagId, segmentId, tenantId)
    }
    
    /**
     * Get all configs for a flag
     */
    suspend fun getConfigsByFlagId(flagId: Int, tenantId: String? = null): List<SmartRolloutConfig> {
        return smartRolloutRepository.findConfigsByFlagId(flagId, tenantId)
    }
    
    /**
     * Delete config
     */
    suspend fun deleteConfig(id: Int, tenantId: String? = null): Boolean {
        return smartRolloutRepository.deleteConfig(id, tenantId)
    }
    
    /**
     * Execute smart rollout for a specific config
     */
    suspend fun executeRollout(
        configId: Int,
        hasAnomaly: Boolean = false,
        tenantId: String? = null
    ): RolloutExecutionResult {
        val config = smartRolloutRepository.findConfigById(configId, tenantId)
            ?: return RolloutExecutionResult(
                configId = configId,
                success = false,
                error = "Config not found"
            )
        
        val now = System.currentTimeMillis()
        val windowStart = now - 3600_000 // 1 hour window
        
        // Get metrics aggregations
        val successRateAgg = metricsRepository.getAggregation(
            flagId = config.flagId,
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            windowStartMs = windowStart,
            windowEndMs = now,
            tenantId = tenantId
        )
        
        val errorRateAgg = metricsRepository.getAggregation(
            flagId = config.flagId,
            metricType = MetricDataPoint.MetricType.ERROR_RATE,
            windowStartMs = windowStart,
            windowEndMs = now,
            tenantId = tenantId
        )
        
        val conversionRateAgg = config.conversionRateThreshold?.let {
            metricsRepository.getAggregation(
                flagId = config.flagId,
                metricType = MetricDataPoint.MetricType.CONVERSION_RATE,
                windowStartMs = windowStart,
                windowEndMs = now,
                tenantId = tenantId
            )
        }
        
        // Make decision
        val decisionResult = smartRolloutUseCase.invoke(
            config = config,
            successRateAgg = successRateAgg,
            errorRateAgg = errorRateAgg,
            conversionRateAgg = conversionRateAgg,
            hasAnomaly = hasAnomaly
        )
        
        // Execute decision
        val updatedConfig = when (decisionResult.decision.action) {
            RolloutDecision.Action.INCREMENT -> {
                executeIncrement(config, decisionResult)
            }
            RolloutDecision.Action.ROLLBACK -> {
                executeRollback(config, decisionResult)
            }
            RolloutDecision.Action.COMPLETE -> {
                executeComplete(config, decisionResult)
            }
            else -> {
                // PAUSE, FAIL - just update status
                smartRolloutRepository.updateConfig(decisionResult.config)
            }
        }
        
        // Send Slack notification
        val flag = segmentService.getSegment(config.segmentId)?.let { segment ->
            metricsRepository.findByFlagId(
                flagId = config.flagId,
                startTime = System.currentTimeMillis() - 1000,
                endTime = System.currentTimeMillis()
            ).firstOrNull()?.flagKey
        } ?: "unknown"
        
        slackNotificationService?.sendRolloutNotification(
            config = updatedConfig,
            decision = decisionResult.decision,
            flagKey = flag
        )
        
        return RolloutExecutionResult(
            configId = configId,
            success = true,
            decision = decisionResult.decision,
            updatedConfig = updatedConfig,
            metricsSummary = decisionResult.metricsUsed
        )
    }
    
    /**
     * Execute rollout increment
     */
    private suspend fun executeIncrement(
        config: SmartRolloutConfig,
        decisionResult: SmartRolloutUseCase.DecisionResult
    ): SmartRolloutConfig {
        val newPercent = decisionResult.decision.newRolloutPercent ?: config.currentRolloutPercent
        
        // Update segment rollout percentage
        val segment = segmentService.getSegment(config.segmentId)
        if (segment != null) {
            segmentService.updateSegment(
                segment.copy(rolloutPercent = newPercent)
            )
        }
        
        // Save history
        saveHistory(
            config = config,
            newPercent = newPercent,
            reason = decisionResult.decision.reason,
            metricsSummary = decisionResult.metricsUsed
        )
        
        // Update config
        return smartRolloutRepository.updateConfig(decisionResult.config)
    }
    
    /**
     * Execute rollout rollback
     */
    private suspend fun executeRollback(
        config: SmartRolloutConfig,
        decisionResult: SmartRolloutUseCase.DecisionResult
    ): SmartRolloutConfig {
        val newPercent = decisionResult.decision.newRolloutPercent ?: config.currentRolloutPercent
        
        // Update segment rollout percentage
        val segment = segmentService.getSegment(config.segmentId)
        if (segment != null) {
            segmentService.updateSegment(
                segment.copy(rolloutPercent = newPercent)
            )
        }
        
        // Save history
        saveHistory(
            config = config,
            newPercent = newPercent,
            reason = decisionResult.decision.reason,
            metricsSummary = decisionResult.metricsUsed
        )
        
        // Update config
        return smartRolloutRepository.updateConfig(decisionResult.config)
    }
    
    /**
     * Execute rollout completion
     */
    private suspend fun executeComplete(
        config: SmartRolloutConfig,
        decisionResult: SmartRolloutUseCase.DecisionResult
    ): SmartRolloutConfig {
        // Save history
        saveHistory(
            config = config,
            newPercent = config.currentRolloutPercent,
            reason = decisionResult.decision.reason,
            metricsSummary = decisionResult.metricsUsed
        )
        
        // Update config
        return smartRolloutRepository.updateConfig(decisionResult.config)
    }
    
    /**
     * Save rollout history
     */
    private suspend fun saveHistory(
        config: SmartRolloutConfig,
        newPercent: Int,
        reason: String,
        metricsSummary: SmartRolloutUseCase.MetricsSummary
    ) {
        val history = SmartRolloutHistory(
            rolloutConfigId = config.id,
            flagId = config.flagId,
            segmentId = config.segmentId,
            previousPercent = config.currentRolloutPercent,
            newPercent = newPercent,
            reason = reason,
            successRate = metricsSummary.successRate,
            errorRate = metricsSummary.errorRate,
            sampleSize = metricsSummary.sampleSize,
            timestamp = System.currentTimeMillis(),
            tenantId = config.tenantId
        )
        
        smartRolloutRepository.saveHistory(history)
    }
    
    /**
     * Execute rollouts for all active configs
     */
    suspend fun executeAllRollouts(tenantId: String? = null): List<RolloutExecutionResult> {
        val activeConfigs = smartRolloutRepository.findActiveConfigs(tenantId)
        
        return activeConfigs.map { config ->
            executeRollout(config.id, hasAnomaly = false, tenantId)
        }
    }
    
    /**
     * Get rollout history for a config
     */
    suspend fun getHistory(
        configId: Int,
        limit: Int = 100,
        tenantId: String? = null
    ): List<SmartRolloutHistory> {
        return smartRolloutRepository.findHistoryByConfigId(configId, limit, tenantId)
    }
    
    /**
     * Get rollout history for a flag
     */
    suspend fun getHistoryByFlagId(
        flagId: Int,
        limit: Int = 100,
        tenantId: String? = null
    ): List<SmartRolloutHistory> {
        return smartRolloutRepository.findHistoryByFlagId(flagId, limit, tenantId)
    }
    
    /**
     * Clean up old history
     */
    suspend fun cleanupOldHistory(olderThanMs: Long, tenantId: String? = null): Int {
        return smartRolloutRepository.deleteHistoryOlderThan(olderThanMs, tenantId)
    }
}

/**
 * Result of rollout execution
 */
data class RolloutExecutionResult(
    val configId: Int,
    val success: Boolean,
    val decision: RolloutDecision? = null,
    val updatedConfig: SmartRolloutConfig? = null,
    val metricsSummary: SmartRolloutUseCase.MetricsSummary? = null,
    val error: String? = null
)
