package flagent.service

import flagent.domain.entity.Distribution
import flagent.domain.repository.IDistributionRepository
import flagent.domain.repository.IFlagRepository
import flagent.service.command.PutDistributionsCommand

/**
 * Distribution CRUD operations
 */
class DistributionService(
    private val distributionRepository: IDistributionRepository,
    private val flagRepository: IFlagRepository,
    private val flagSnapshotService: FlagSnapshotService? = null
) {
    suspend fun findDistributionsBySegmentId(segmentId: Int): List<Distribution> {
        return distributionRepository.findBySegmentId(segmentId)
    }
    
    suspend fun updateDistributions(command: PutDistributionsCommand, updatedBy: String? = null) {
        val flagId = command.flagId
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        val variantMap = flag.variants.associateBy { it.id }
        val distributions = command.distributions.map {
            val variant = variantMap[it.variantID]
                ?: throw IllegalArgumentException("error finding variantID ${it.variantID} under this flag. expecting ${flag.variants.map { v -> v.id }}")
            val resolvedVariantKey = it.variantKey ?: variant.key
            Distribution(
                segmentId = command.segmentId,
                variantId = it.variantID,
                variantKey = resolvedVariantKey,
                percent = it.percent
            )
        }
        validateDistributions(flagId, distributions)
        distributionRepository.updateDistributions(command.segmentId, distributions)
        
        // Save flag snapshot after updating distributions
        flagSnapshotService?.saveFlagSnapshot(command.flagId, updatedBy)
    }
    
    private suspend fun validateDistributions(flagId: Int, distributions: List<Distribution>) {
        // Validate that sum of percents equals 100
        val sum = distributions.sumOf { it.percent }
        if (sum != 100) {
            throw IllegalArgumentException("the sum of distributions' percent $sum is not 100")
        }
        
        // Validate that all distributions have valid variant IDs
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        
        val variantMap = flag.variants.associateBy { it.id }
        val variantIds = flag.variants.map { it.id }
        
        for (distribution in distributions) {
            val variant = variantMap[distribution.variantId]
            if (variant == null) {
                throw IllegalArgumentException("error finding variantID ${distribution.variantId} under this flag. expecting $variantIds")
            }
            if (variant.key != distribution.variantKey) {
                throw IllegalArgumentException("error matching variantID ${distribution.variantId} with variantKey ${distribution.variantKey}. expecting ${variant.key}")
            }
        }
    }
}