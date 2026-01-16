package flagent.service.dto

import kotlinx.serialization.Serializable

/**
 * Flag DTO for service layer
 * Used for data transfer between service and domain layers
 */
@Serializable
data class FlagDTO(
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
    val segments: List<SegmentDTO> = emptyList(),
    val variants: List<VariantDTO> = emptyList(),
    val tags: List<TagDTO> = emptyList()
)
