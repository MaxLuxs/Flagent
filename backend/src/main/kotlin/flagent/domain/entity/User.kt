package flagent.domain.entity

import java.time.LocalDateTime

/**
 * User entity - represents an admin user who can log in to the UI.
 *
 * Domain entity - no framework dependencies.
 * passwordHash is never exposed in API responses.
 */
data class User(
    val id: Int = 0,
    val email: String? = null,
    val name: String? = null,
    val passwordHash: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
    val blockedAt: LocalDateTime? = null
) {
    val isBlocked: Boolean get() = blockedAt != null
    val isDeleted: Boolean get() = deletedAt != null
}
