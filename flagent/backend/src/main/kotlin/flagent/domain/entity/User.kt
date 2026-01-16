package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * User entity - represents the User
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class User(
    val id: Int = 0,
    val email: String? = null
)
