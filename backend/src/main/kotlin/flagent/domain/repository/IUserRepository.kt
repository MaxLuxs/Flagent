package flagent.domain.repository

import flagent.domain.entity.User

/**
 * User repository interface (admin users for UI login).
 * Domain layer - no framework dependencies.
 */
interface IUserRepository {
    suspend fun findById(id: Int): User?
    /** Find by email; exclude soft-deleted. Include blocked users (caller checks blockedAt). */
    suspend fun findByEmail(email: String): User?
    suspend fun findAll(limit: Int, offset: Int): List<User>
    suspend fun count(): Long
    suspend fun create(user: User): User
    suspend fun update(user: User): User
    /** Soft delete: set deletedAt. */
    suspend fun softDelete(id: Int)
    suspend fun setBlockedAt(id: Int, blockedAt: java.time.LocalDateTime?)
}
