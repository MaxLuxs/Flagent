package flagent.service

import flagent.domain.entity.Variant
import flagent.domain.repository.IDistributionRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IVariantRepository

/**
 * Variant service - handles variant business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class VariantService(
    private val variantRepository: IVariantRepository,
    private val flagRepository: IFlagRepository,
    private val distributionRepository: IDistributionRepository,
    private val flagSnapshotService: FlagSnapshotService? = null
) {
    suspend fun findVariantsByFlagId(flagId: Int): List<Variant> {
        return variantRepository.findByFlagId(flagId)
    }
    
    suspend fun createVariant(flagId: Int, variant: Variant, updatedBy: String? = null): Variant {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        
        // Validate variant key
        validateVariantKey(variant.key)
        
        // Create variant
        val created = variantRepository.create(
            variant.copy(flagId = flagId)
        )
        
        // Save flag snapshot after creating variant
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
        
        return created
    }
    
    suspend fun updateVariant(flagId: Int, variantId: Int, variant: Variant, updatedBy: String? = null): Variant {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        
        // Validate variant exists
        val existing = variantRepository.findById(variantId)
            ?: throw IllegalArgumentException("error finding variantID $variantId")
        
        // Validate variant belongs to flag
        if (existing.flagId != flagId) {
            throw IllegalArgumentException("variant $variantId does not belong to flag $flagId")
        }
        
        // Validate variant key
        validateVariantKey(variant.key)
        
        // Update variant
        val updated = variantRepository.update(
            variant.copy(id = variantId, flagId = flagId)
        )
        
        // Update variantKey in distributions
        updateDistributionsVariantKey(variantId, variant.key)
        
        // Save flag snapshot after updating variant
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
        
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
        // Maps to validatePutVariantForDistributions from pkg/handler/validate.go
        distributionRepository.updateVariantKeyByVariantId(variantId, variantKey)
    }
}
