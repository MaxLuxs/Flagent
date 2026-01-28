package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * SmartRolloutConfig - configuration for smart/automatic rollouts
 * 
 * ML-based automatic rollout percentage adjustment based on metrics
 * Domain entity - no framework dependencies
 */
@Serializable
data class SmartRolloutConfig(
    val id: Int = 0,
    val flagId: Int,
    val segmentId: Int,
    val enabled: Boolean = true,
    val targetRolloutPercent: Int = 100,         // Target rollout percentage
    val currentRolloutPercent: Int = 0,          // Current rollout percentage
    val incrementPercent: Int = 10,              // Increment per step (e.g., 10%)
    val incrementIntervalMs: Long = 3600_000,    // 1 hour between increments
    val successRateThreshold: Double = 0.95,     // 95% success rate to continue
    val errorRateThreshold: Double = 0.05,       // 5% error rate to pause/rollback
    val conversionRateThreshold: Double? = null, // Optional conversion rate threshold
    val minSampleSize: Int = 100,                // Minimum evaluations before next increment
    val autoRollback: Boolean = true,            // Auto rollback on failure
    val rollbackOnAnomaly: Boolean = true,       // Rollback on anomaly detection
    val pauseOnAnomaly: Boolean = true,          // Pause rollout on anomaly
    val notifyOnIncrement: Boolean = true,       // Send notification on each increment
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastIncrementAt: Long? = null,
    val completedAt: Long? = null,
    val status: RolloutStatus = RolloutStatus.PENDING,
    val tenantId: String? = null
) {
    /**
     * RolloutStatus - status of smart rollout
     */
    enum class RolloutStatus {
        PENDING,      // Not started yet
        IN_PROGRESS,  // Actively rolling out
        PAUSED,       // Paused due to metrics or anomaly
        COMPLETED,    // Reached target rollout
        FAILED,       // Failed and rolled back
        CANCELLED     // Manually cancelled
    }
    
    /**
     * Check if rollout can proceed to next increment
     */
    fun canIncrement(
        currentSampleSize: Int,
        currentSuccessRate: Double,
        currentErrorRate: Double,
        timeSinceLastIncrementMs: Long
    ): Boolean {
        // Not enabled
        if (!enabled) return false
        
        // Already completed
        if (status == RolloutStatus.COMPLETED) return false
        
        // Paused or failed
        if (status == RolloutStatus.PAUSED || status == RolloutStatus.FAILED) return false
        
        // Not enough samples
        if (currentSampleSize < minSampleSize) return false
        
        // Not enough time since last increment
        if (lastIncrementAt != null && timeSinceLastIncrementMs < incrementIntervalMs) {
            return false
        }
        
        // Metrics don't meet thresholds
        if (currentSuccessRate < successRateThreshold) return false
        if (currentErrorRate > errorRateThreshold) return false
        
        // Already at target
        if (currentRolloutPercent >= targetRolloutPercent) return false
        
        return true
    }
    
    /**
     * Calculate next rollout percentage
     */
    fun nextRolloutPercent(): Int {
        val next = currentRolloutPercent + incrementPercent
        return minOf(next, targetRolloutPercent)
    }
    
    /**
     * Check if rollout should be paused/rolled back based on metrics
     */
    fun shouldRollback(
        currentSuccessRate: Double,
        currentErrorRate: Double
    ): Boolean {
        if (!autoRollback) return false
        
        // Success rate too low
        if (currentSuccessRate < successRateThreshold * 0.9) return true // 10% margin
        
        // Error rate too high
        if (currentErrorRate > errorRateThreshold * 1.5) return true // 50% margin
        
        return false
    }
}

/**
 * SmartRolloutHistory - history of smart rollout changes
 */
@Serializable
data class SmartRolloutHistory(
    val id: Int = 0,
    val rolloutConfigId: Int,
    val flagId: Int,
    val segmentId: Int,
    val previousPercent: Int,
    val newPercent: Int,
    val reason: String,
    val successRate: Double?,
    val errorRate: Double?,
    val sampleSize: Int?,
    val timestamp: Long = System.currentTimeMillis(),
    val tenantId: String? = null
)

/**
 * RolloutDecision - decision made by smart rollout engine
 */
@Serializable
data class RolloutDecision(
    val action: Action,
    val reason: String,
    val newRolloutPercent: Int? = null,
    val confidence: Double = 1.0  // Confidence in decision (0.0 - 1.0)
) {
    enum class Action {
        INCREMENT,   // Increase rollout percentage
        PAUSE,       // Pause rollout (no change)
        ROLLBACK,    // Rollback to previous percentage
        COMPLETE,    // Mark as completed
        FAIL         // Mark as failed
    }
}
