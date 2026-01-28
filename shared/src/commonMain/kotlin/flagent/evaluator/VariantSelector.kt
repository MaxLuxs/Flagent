package flagent.evaluator

/**
 * VariantSelector - selects variant based on distribution and consistent hashing
 * Shared across all platforms (JVM, JS, Native)
 * 
 * Uses consistent hashing (CRC32) for deterministic variant selection
 */
class VariantSelector {
    /**
     * Select variant from segment based on entityID and rollout percent
     * Returns variant ID if rollout matches, null otherwise
     */
    fun selectVariant(
        segment: FlagEvaluator.EvaluableSegment,
        entityID: String,
        flagID: Int
    ): Int? {
        val distributionArray = segment.prepareDistribution()
        val (variantID, _) = distributionArray.rollout(
            entityID = entityID,
            salt = flagID.toString(),
            rolloutPercent = segment.rolloutPercent
        )
        return variantID
    }
}
