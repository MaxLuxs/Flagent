package flagent.service

import flagent.domain.entity.User
import flagent.domain.repository.IUserRepository
import mu.KotlinLogging
import org.mindrot.jbcrypt.BCrypt

private val logger = KotlinLogging.logger {}

/**
 * Service for admin users (UI login and user management).
 * Passwords are hashed with BCrypt; passwordHash is never exposed in API responses.
 */
class UserService(private val userRepository: IUserRepository) {

    /**
     * Validates email and password; returns the user if found (not deleted, not blocked) and password matches.
     */
    suspend fun validatePassword(email: String, plainPassword: String): User? {
        val user = userRepository.findByEmail(email.trim()) ?: return null
        if (user.blockedAt != null) return null
        val hash = user.passwordHash ?: return null
        return if (runCatching {
                BCrypt.checkpw(
                    plainPassword,
                    hash
                )
            }.getOrElse { false }) user else null
    }

    suspend fun findAll(limit: Int = 50, offset: Int = 0): List<User> =
        userRepository.findAll(limit.coerceIn(1, 100), offset.coerceAtLeast(0))

    suspend fun count(): Long = userRepository.count()

    suspend fun findById(id: Int): User? = userRepository.findById(id)

    suspend fun create(email: String, plainPassword: String, name: String?): User {
        val trimmedEmail = email.trim()
        require(trimmedEmail.isNotBlank()) { "Email is required" }
        require(plainPassword.isNotBlank()) { "Password is required" }
        val existing = userRepository.findByEmail(trimmedEmail)
        if (existing != null) throw IllegalArgumentException("User with this email already exists")
        val hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt())
        val user = User(
            email = trimmedEmail,
            name = name?.trim()?.takeIf { it.isNotBlank() },
            passwordHash = hash
        )
        return userRepository.create(user)
    }

    suspend fun update(id: Int, name: String?, newPassword: String?): User {
        val user =
            userRepository.findById(id) ?: throw NoSuchElementException("User not found: $id")
        val updatedHash = when {
            newPassword != null && newPassword.isNotBlank() -> BCrypt.hashpw(
                newPassword,
                BCrypt.gensalt()
            )

            else -> user.passwordHash
        }
        val updated = user.copy(
            name = name?.trim()?.takeIf { it.isNotBlank() } ?: user.name,
            passwordHash = updatedHash
        )
        return userRepository.update(updated)
    }

    suspend fun softDelete(id: Int) {
        if (userRepository.findById(id) == null) throw NoSuchElementException("User not found: $id")
        userRepository.softDelete(id)
    }

    suspend fun block(id: Int) {
        val user =
            userRepository.findById(id) ?: throw NoSuchElementException("User not found: $id")
        if (user.blockedAt != null) return // already blocked
        userRepository.setBlockedAt(id, java.time.LocalDateTime.now())
    }

    suspend fun unblock(id: Int) {
        userRepository.findById(id) ?: throw NoSuchElementException("User not found: $id")
        userRepository.setBlockedAt(id, null)
    }
}
