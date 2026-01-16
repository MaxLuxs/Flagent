package flagent.service.dto

import kotlinx.serialization.Serializable

/**
 * Distribution DTO for service layer
 */
@Serializable
data class DistributionDTO(
    val id: Int = 0,
    val segmentId: Int,
    val variantId: Int,
    val variantKey: String? = null,
    val percent: Int = 0
)
