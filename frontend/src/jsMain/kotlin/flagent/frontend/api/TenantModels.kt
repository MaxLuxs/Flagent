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
    val createdAt: String
)
