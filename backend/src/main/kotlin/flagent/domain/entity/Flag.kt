package flagent.domain.entity

/**
 * Flag entity - the unit of flags
 *
 * Domain entity - no framework dependencies.
 * Evaluation logic lives in shared evaluator (FlagEvaluator).
 */
data class Flag(
    val id: Int = 0,
    val key: String,
    val description: String,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val enabled: Boolean = false,
    val snapshotId: Int = 0,
    val notes: String? = null,
    val dataRecordsEnabled: Boolean = false,
    val entityType: String? = null,
    val environmentId: Long? = null,
    val segments: List<Segment> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val updatedAt: String? = null
)
