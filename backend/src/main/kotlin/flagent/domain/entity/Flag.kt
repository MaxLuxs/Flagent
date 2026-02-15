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
    val projectId: Long? = null,
    val dependsOn: List<String> = emptyList(),
    val segments: List<Segment> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val updatedAt: String? = null,
    /** When set, flag is archived (soft-deleted). Restore clears it; permanent delete removes row. */
    val deletedAt: String? = null
)
