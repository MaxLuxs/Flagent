package flagent.frontend.api

import kotlinx.serialization.Serializable

/**
 * Admin user API models (GET/POST/PUT /admin/users).
 */
@Serializable
data class AdminUserResponse(
    val id: Int,
    val email: String? = null,
    val name: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val blockedAt: String? = null
)

@Serializable
data class CreateAdminUserRequest(
    val email: String = "",
    val password: String = "",
    val name: String? = null
)

@Serializable
data class UpdateAdminUserRequest(
    val name: String? = null,
    val password: String? = null
)
