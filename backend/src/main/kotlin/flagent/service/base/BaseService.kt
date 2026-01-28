package flagent.service.base

import flagent.service.FlagSnapshotService
import mu.KotlinLogging

/**
 * BaseService - base class for services with common functionality
 * Reduces code duplication across service layer
 */
abstract class BaseService(
    protected val flagSnapshotService: FlagSnapshotService? = null
) {
    protected val logger = KotlinLogging.logger {}
    
    /**
     * Save flag snapshot after modification
     */
    protected suspend fun saveSnapshot(flagId: Int, updatedBy: String) {
        try {
            flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
            logger.debug { "Saved snapshot for flag $flagId by $updatedBy" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save snapshot for flag $flagId" }
            // Don't throw - snapshot is optional
        }
    }
    
    /**
     * Log operation
     */
    protected fun logOperation(operation: String, details: String) {
        logger.info { "$operation: $details" }
    }
    
    /**
     * Log error
     */
    protected fun logError(operation: String, error: Throwable, details: String = "") {
        logger.error(error) { "$operation failed: $details" }
    }
    
    /**
     * Validate required parameter
     */
    protected fun <T> requireNotNull(value: T?, paramName: String): T {
        return value ?: throw IllegalArgumentException("$paramName is required")
    }
    
    /**
     * Validate positive number
     */
    protected fun requirePositive(value: Int, paramName: String): Int {
        require(value > 0) { "$paramName must be positive" }
        return value
    }
    
    /**
     * Validate non-negative number
     */
    protected fun requireNonNegative(value: Int, paramName: String): Int {
        require(value >= 0) { "$paramName must be non-negative" }
        return value
    }
}
