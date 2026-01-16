package flagent.service.dto

import kotlinx.serialization.Serializable

/**
 * Tag DTO for service layer
 */
@Serializable
data class TagDTO(
    val id: Int = 0,
    val value: String
)
