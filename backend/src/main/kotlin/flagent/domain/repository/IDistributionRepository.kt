package flagent.domain.repository

import flagent.domain.entity.Distribution

/**
 * Distribution repository interface
 * Domain layer - no framework dependencies
 */
interface IDistributionRepository {
    suspend fun findBySegmentId(segmentId: Int): List<Distribution>
    suspend fun updateDistributions(segmentId: Int, distributions: List<Distribution>)
    suspend fun updateVariantKeyByVariantId(variantId: Int, variantKey: String)
}
