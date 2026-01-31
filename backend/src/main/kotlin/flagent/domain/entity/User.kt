package flagent.domain.entity

/**
 * User entity - represents the User
 *
 * Domain entity - no framework dependencies
 */
data class User(
    val id: Int = 0,
    val email: String? = null
)
