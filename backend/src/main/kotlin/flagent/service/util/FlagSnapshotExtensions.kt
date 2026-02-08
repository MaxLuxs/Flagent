package flagent.service.util

import flagent.service.FlagSnapshotService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Saves flag snapshot when FlagSnapshotService is available.
 * Does not throw; snapshot is optional.
 */
suspend fun FlagSnapshotService?.saveFlagSnapshotIfPresent(flagId: Int, updatedBy: String) {
    try {
        this?.saveFlagSnapshot(flagId, updatedBy)
        logger.debug { "Saved snapshot for flag $flagId by $updatedBy" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to save snapshot for flag $flagId" }
    }
}
