package flagent.domain.repository

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig

/**
 * IAnomalyAlertRepository - interface for anomaly alerts and configs
 * 
 * Domain layer - defines contract for infrastructure layer
 */
interface IAnomalyAlertRepository {
    /**
     * Save an anomaly alert
     */
    suspend fun saveAlert(alert: AnomalyAlert): AnomalyAlert
    
    /**
     * Find alert by ID
     */
    suspend fun findAlertById(id: Int, tenantId: String? = null): AnomalyAlert?
    
    /**
     * Find alerts by flag ID
     */
    suspend fun findAlertsByFlagId(
        flagId: Int,
        resolved: Boolean? = null,
        severity: AnomalyAlert.Severity? = null,
        limit: Int = 100,
        tenantId: String? = null
    ): List<AnomalyAlert>
    
    /**
     * Find recent unresolved alerts
     */
    suspend fun findUnresolvedAlerts(
        flagId: Int? = null,
        tenantId: String? = null
    ): List<AnomalyAlert>
    
    /**
     * Update alert (mark as resolved, add action taken, etc.)
     */
    suspend fun updateAlert(alert: AnomalyAlert): AnomalyAlert
    
    /**
     * Mark alert as resolved
     */
    suspend fun markResolved(alertId: Int, tenantId: String? = null): AnomalyAlert?
    
    /**
     * Delete old alerts (cleanup)
     */
    suspend fun deleteOlderThan(timestamp: Long, tenantId: String? = null): Int
    
    // ===== Anomaly Detection Config =====
    
    /**
     * Save anomaly detection config
     */
    suspend fun saveConfig(config: AnomalyDetectionConfig): AnomalyDetectionConfig
    
    /**
     * Find config by flag ID
     */
    suspend fun findConfigByFlagId(flagId: Int, tenantId: String? = null): AnomalyDetectionConfig?
    
    /**
     * Find all enabled configs
     */
    suspend fun findEnabledConfigs(tenantId: String? = null): List<AnomalyDetectionConfig>
    
    /**
     * Update config
     */
    suspend fun updateConfig(config: AnomalyDetectionConfig): AnomalyDetectionConfig
    
    /**
     * Delete config
     */
    suspend fun deleteConfig(flagId: Int, tenantId: String? = null): Boolean
}
