package flagent.domain.repository

import flagent.domain.entity.SmartRolloutConfig
import flagent.domain.entity.SmartRolloutHistory

/**
 * ISmartRolloutRepository - interface for smart rollout configs and history
 * 
 * Domain layer - defines contract for infrastructure layer
 */
interface ISmartRolloutRepository {
    /**
     * Save smart rollout config
     */
    suspend fun saveConfig(config: SmartRolloutConfig): SmartRolloutConfig
    
    /**
     * Find config by ID
     */
    suspend fun findConfigById(id: Int, tenantId: String? = null): SmartRolloutConfig?
    
    /**
     * Find config by flag ID and segment ID
     */
    suspend fun findConfigByFlagAndSegment(
        flagId: Int,
        segmentId: Int,
        tenantId: String? = null
    ): SmartRolloutConfig?
    
    /**
     * Find all configs for a flag
     */
    suspend fun findConfigsByFlagId(
        flagId: Int,
        tenantId: String? = null
    ): List<SmartRolloutConfig>
    
    /**
     * Find all active (in progress) configs
     */
    suspend fun findActiveConfigs(tenantId: String? = null): List<SmartRolloutConfig>
    
    /**
     * Find all enabled configs
     */
    suspend fun findEnabledConfigs(tenantId: String? = null): List<SmartRolloutConfig>
    
    /**
     * Update config
     */
    suspend fun updateConfig(config: SmartRolloutConfig): SmartRolloutConfig
    
    /**
     * Delete config
     */
    suspend fun deleteConfig(id: Int, tenantId: String? = null): Boolean
    
    // ===== Smart Rollout History =====
    
    /**
     * Save rollout history entry
     */
    suspend fun saveHistory(history: SmartRolloutHistory): SmartRolloutHistory
    
    /**
     * Find history by config ID
     */
    suspend fun findHistoryByConfigId(
        rolloutConfigId: Int,
        limit: Int = 100,
        tenantId: String? = null
    ): List<SmartRolloutHistory>
    
    /**
     * Find history by flag ID
     */
    suspend fun findHistoryByFlagId(
        flagId: Int,
        limit: Int = 100,
        tenantId: String? = null
    ): List<SmartRolloutHistory>
    
    /**
     * Delete old history (cleanup)
     */
    suspend fun deleteHistoryOlderThan(timestamp: Long, tenantId: String? = null): Int
}
