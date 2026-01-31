package flagent.domain.entity

/**
 * Segment entity - the unit of segmentation
 *
 * Domain entity - no framework dependencies.
 * Evaluation logic lives in shared evaluator (RolloutAlgorithm, FlagEvaluator).
 */
data class Segment(
    val id: Int = 0,
    val flagId: Int,
    val description: String? = null,
    val rank: Int = 999,
    val rolloutPercent: Int = 0,
    val constraints: List<Constraint> = emptyList(),
    val distributions: List<Distribution> = emptyList()
)
