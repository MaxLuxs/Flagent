package flagent.frontend.api

import kotlinx.serialization.Serializable

/**
 * Tenant API models (matches backend TenantDTO)
 */
@Serializable
data class TenantResponse(
    val id: Long,
    val key: String,
    val name: String,
    val plan: String,
    val status: String,
    val schemaName: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class CreateTenantRequest(
    val key: String,
    val name: String,
    val plan: String,
    val ownerEmail: String
)

@Serializable
data class CreateTenantResponse(
    val tenant: TenantResponse,
    val ownerUser: TenantUserResponse,
    val apiKey: String
)

@Serializable
data class TenantUserResponse(
    val id: Long,
    val tenantId: Long,
    val email: String,
    val role: String,
    val customRoleId: Long? = null,
    val createdAt: String
)

@Serializable
data class RoleResponse(
    val key: String,
    val name: String,
    val permissions: List<String>,
    val isBuiltIn: Boolean,
    val id: Long? = null
)

@Serializable
data class RolesResponse(
    val roles: List<RoleResponse>
)

@Serializable
data class AssignRoleRequest(
    val roleKey: String? = null,
    val customRoleId: Long? = null
)

/** Request to create an API key (admin: POST /admin/tenants/{id}/api-keys; tenant: POST /tenants/me/api-keys). */
@Serializable
data class CreateApiKeyRequest(
    val name: String = "Recovery",
    val scopes: List<String> = emptyList(),
    val environmentId: Long? = null,
    val expiresAt: String? = null
)

@Serializable
data class TenantApiKeyInfo(
    val id: Long,
    val tenantId: Long,
    val environmentId: Long? = null,
    val name: String,
    val keyHash: String,
    val scopes: List<String>,
    val expiresAt: String? = null,
    val createdAt: String,
    val lastUsedAt: String? = null
)

@Serializable
data class CreateApiKeyResponse(
    val apiKey: String,
    val apiKeyInfo: TenantApiKeyInfo
)
