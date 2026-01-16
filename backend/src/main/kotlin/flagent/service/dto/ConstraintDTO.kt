package flagent.service.dto

import kotlinx.serialization.Serializable

/**
 * Constraint DTO for service layer
 */
@Serializable
data class ConstraintDTO(
    val id: Int = 0,
    val segmentId: Int,
    val property: String,
    val operator: String,
    val value: String
)
