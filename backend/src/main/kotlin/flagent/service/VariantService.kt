package flagent.service

import flagent.domain.entity.Variant
import flagent.domain.repository.IDistributionRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IVariantRepository
import flagent.route.RealtimeEventBus
import flagent.service.command.CreateVariantCommand
import flagent.service.command.PutVariantCommand

/**
 * Variant CRUD operations
 */
class VariantService(
    private val variantRepository: IVariantRepository,
    private val flagRepository: IFlagRepository,
    private val distributionRepository: IDistributionRepository,
    private val flagSnapshotService: FlagSnapshotService? = null,
    private val eventBus: RealtimeEventBus? = null
) {
    suspend fun findVariantsByFlagId(flagId: Int): List<Variant> {
        return variantRepository.findByFlagId(flagId)
    }

    suspend fun createVariant(command: CreateVariantCommand, updatedBy: String? = null): Variant {
        // Validate flag exists
        val flag = flagRepository.findById(command.flagId)
            ?: throw IllegalArgumentException("error finding flagID ${command.flagId}")

        // Validate variant key
        validateVariantKey(command.key)

        val variant = Variant(
            flagId = command.flagId,
            key = command.key,
            attachment = command.attachment
        )

        // Create variant
        val created = variantRepository.create(variant)

        // Save flag snapshot after creating variant
        flagSnapshotService?.saveFlagSnapshot(command.flagId, updatedBy)
        eventBus?.publishVariantUpdated(command.flagId.toLong(), flag.key, created.id.toLong())

        return created
    }

    suspend fun updateVariant(command: PutVariantCommand, updatedBy: String? = null): Variant {
        // Validate flag exists
        val flag = flagRepository.findById(command.flagId)
            ?: throw IllegalArgumentException("error finding flagID ${command.flagId}")

        // Validate variant exists
        val existing = variantRepository.findById(command.variantId)
            ?: throw IllegalArgumentException("error finding variantID ${command.variantId}")

        // Validate variant belongs to flag
        if (existing.flagId != command.flagId) {
            throw IllegalArgumentException("variant ${command.variantId} does not belong to flag ${command.flagId}")
        }

        // Validate variant key
        validateVariantKey(command.key)

        val variant = existing.copy(
            key = command.key,
            attachment = command.attachment
        )

        // Update variant
        val updated = variantRepository.update(variant)

        // Update variantKey in distributions
        updateDistributionsVariantKey(command.variantId, command.key)

        // Save flag snapshot after updating variant
        flagSnapshotService?.saveFlagSnapshot(command.flagId, updatedBy)
        eventBus?.publishVariantUpdated(command.flagId.toLong(), flag.key, updated.id.toLong())

        return updated
    }

    suspend fun deleteVariant(flagId: Int, variantId: Int, updatedBy: String? = null) {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")

        // Validate variant exists
        val variant = variantRepository.findById(variantId)
            ?: throw IllegalArgumentException("error finding variantID $variantId")

        // Validate variant belongs to flag
        if (variant.flagId != flagId) {
            throw IllegalArgumentException("variant $variantId does not belong to flag $flagId")
        }

        // Validate that variant can be deleted (no distributions with non-zero percent)
        validateDeleteVariant(flagId, variantId)

        // Delete variant (soft delete)
        variantRepository.delete(variantId)
        eventBus?.publishVariantUpdated(flagId.toLong(), flag.key, variantId.toLong())
        // Save flag snapshot after deleting variant
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }

    private fun validateVariantKey(key: String) {
        if (key.isBlank()) {
            throw IllegalArgumentException("key cannot be empty")
        }

        // Key length limit: 63 characters
        if (key.length > 63) {
            throw IllegalArgumentException("key:$key cannot be longer than 63")
        }

        // Key format: ^[\w\d-/\.\:]+$
        val keyRegex = Regex("^[\\w\\d-/\\.:]+$")
        if (!keyRegex.matches(key)) {
            throw IllegalArgumentException("key:$key should have the format $keyRegex")
        }
    }

    private suspend fun validateDeleteVariant(flagId: Int, variantId: Int) {
        // Get all segments for the flag
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")

        // Check all distributions for this variant
        for (segment in flag.segments) {
            val distributions = distributionRepository.findBySegmentId(segment.id)
            for (distribution in distributions) {
                if (distribution.variantId == variantId) {
                    if (distribution.percent != 0) {
                        throw IllegalArgumentException(
                            "error deleting variant $variantId. distribution ${distribution.id} still has non-zero distribution ${distribution.percent}"
                        )
                    }
                }
            }
        }
    }

    private suspend fun updateDistributionsVariantKey(variantId: Int, variantKey: String) {
        // Update all distributions that reference this variantId to have the new variantKey
        // Update distributions to reference new variantKey
        distributionRepository.updateVariantKeyByVariantId(variantId, variantKey)
    }
}
