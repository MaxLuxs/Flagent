package flagent.service

import flagent.cache.impl.toEvalCacheExport
import flagent.domain.entity.FlagSnapshot
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IFlagSnapshotRepository
import kotlinx.serialization.json.Json

/**
 * Flag snapshot CRUD operations
 */
class FlagSnapshotService(
    private val flagSnapshotRepository: IFlagSnapshotRepository,
    private val flagRepository: IFlagRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun findSnapshotsByFlagId(
        flagId: Int,
        limit: Int? = null,
        offset: Int = 0,
        sort: String? = null
    ): List<FlagSnapshot> {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")

        val sortDesc = sort == null || sort != "ASC"

        return flagSnapshotRepository.findByFlagId(flagId, limit, offset, sortDesc)
    }

    /**
     * Save flag snapshot - creates a snapshot of the flag with all related entities
     *
     * @param flagId ID of the flag to snapshot
     * @param updatedBy User who made the change (can be null)
     */
    suspend fun saveFlagSnapshot(flagId: Int, updatedBy: String? = null) {
        // Load full flag with segments, variants, tags
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")

        // Serialize flag to JSON (domain Flag has no @Serializable; use export DTO)
        val flagJson = try {
            json.encodeToString(flag.toEvalCacheExport())
        } catch (e: Exception) {
            throw IllegalStateException(
                "failed to marshal the flag into JSON when SaveFlagSnapshot: ${e.message}",
                e
            )
        }

        // Create snapshot
        val snapshot = flagSnapshotRepository.create(
            FlagSnapshot(
                flagId = flagId,
                updatedBy = updatedBy,
                flag = flagJson
            )
        )

        // Update flag with updatedBy and snapshotID
        flagRepository.update(
            flag.copy(
                updatedBy = updatedBy,
                snapshotId = snapshot.id
            )
        )
    }
}
