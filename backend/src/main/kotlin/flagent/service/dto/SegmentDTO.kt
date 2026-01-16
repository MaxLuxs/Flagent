package flagent.service.dto

import kotlinx.serialization.Serializable

/**
 * Segment DTO for service layer
 */
@Serializable
data class SegmentDTO(
    val id: Int = 0,
    val flagId: Int,
    val description: String? = null,
    val rank: Int = 999,
    val rolloutPercent: Int = 0,
    val constraints: List<ConstraintDTO> = emptyList(),
    val distributions: List<DistributionDTO> = emptyList()
)
