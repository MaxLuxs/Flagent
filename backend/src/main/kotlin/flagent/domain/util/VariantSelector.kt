package flagent.domain.util

import flagent.domain.entity.Segment

/**
 * VariantSelector - selects variant based on distribution and consistent hashing
 * Domain layer - no framework dependencies
 * 
 * Uses consistent hashing (CRC32) for deterministic variant selection
 */
class VariantSelector {
    /**
     * Select variant from segment based on entityID and rollout percent
     * Returns variant ID if rollout matches, null otherwise
     */
    fun selectVariant(
        segment: Segment,
        entityID: String,
        flagID: Int
    ): Int? {
        val segmentEvaluation = segment.prepareEvaluation()
        val (variantID, _) = segmentEvaluation.distributionArray.rollout(
            entityID = entityID,
            salt = flagID.toString(),
            rolloutPercent = segment.rolloutPercent
        )
        return variantID
    }
}
