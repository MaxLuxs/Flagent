package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Tag entity - a descriptive identifier given to ease searchability
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class Tag(
    val id: Int = 0,
    val value: String
)
