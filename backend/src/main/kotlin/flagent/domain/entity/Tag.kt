package flagent.domain.entity

/**
 * Tag entity - a descriptive identifier given to ease searchability
 *
 * Domain entity - no framework dependencies
 */
data class Tag(
    val id: Int = 0,
    val value: String
)
