package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Flag entity - the unit of flags
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
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
    val segments: List<Segment> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val updatedAt: String? = null
) {
    /**
     * FlagEvaluation holds necessary info for evaluation
     * Not stored in DB, computed at runtime
     */
    data class FlagEvaluation(
        val variantsMap: Map<Int, Variant>
    )
    
    /**
     * Prepare evaluation data structure
     */
    fun prepareEvaluation(): FlagEvaluation {
        val variantsMap = variants.associateBy { it.id }
        return FlagEvaluation(variantsMap = variantsMap)
    }
}
