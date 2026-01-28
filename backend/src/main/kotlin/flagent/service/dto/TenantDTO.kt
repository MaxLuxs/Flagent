package flagent.service.dto

import flagent.domain.entity.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * CreateTenantRequest - request to create a new tenant.
 */
@Serializable
data class CreateTenantRequest(
    val key: String,
    val name: String,
    val plan: String, // STARTER, GROWTH, SCALE, ENTERPRISE
    val ownerEmail: String
)

/**
 * CreateTenantResponse - response after tenant creation.
 */
@Serializable
data class CreateTenantResponse(
    val tenant: TenantDTO,
    val ownerUser: TenantUserDTO,
    val apiKey: String
)

/**
 * TenantDTO - tenant information.
 */
@Serializable
data class TenantDTO(
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

/**
 * TenantUserDTO - user within a tenant.
 */
@Serializable
data class TenantUserDTO(
    val id: Long,
    val tenantId: Long,
    val email: String,
    val role: String,
    val createdAt: String
)

/**
 * TenantApiKeyDTO - API key information.
 */
@Serializable
data class TenantApiKeyDTO(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val keyHash: String,
    val scopes: List<String>,
    val expiresAt: String? = null,
    val createdAt: String,
    val lastUsedAt: String? = null
)

/**
 * CreateApiKeyRequest - request to create API key.
 */
@Serializable
data class CreateApiKeyRequest(
    val name: String,
    val scopes: List<String>,
    val expiresAt: String? = null
)

/**
 * CreateApiKeyResponse - response after API key creation.
 */
@Serializable
data class CreateApiKeyResponse(
    val apiKey: String,
    val apiKeyInfo: TenantApiKeyDTO
)

/**
 * AddUserRequest - request to add user to tenant.
 */
@Serializable
data class AddUserRequest(
    val email: String,
    val role: String
)

/**
 * UpdateTenantPlanRequest - request to update tenant plan.
 */
@Serializable
data class UpdateTenantPlanRequest(
    val plan: String
)

/**
 * TenantUsageDTO - usage statistics.
 */
@Serializable
data class TenantUsageDTO(
    val tenantId: Long,
    val periodStart: String,
    val periodEnd: String,
    val evaluationsCount: Long,
    val flagsCount: Int,
    val apiCallsCount: Long
)

// ============================================================================
// MAPPERS
// ============================================================================

fun Tenant.toDTO() = TenantDTO(
    id = id,
    key = key,
    name = name,
    plan = plan.name,
    status = status.name,
    schemaName = schemaName,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    deletedAt = deletedAt?.toString()
)

fun TenantUser.toDTO() = TenantUserDTO(
    id = id,
    tenantId = tenantId,
    email = email,
    role = role.name,
    createdAt = createdAt.toString()
)

fun TenantApiKey.toDTO() = TenantApiKeyDTO(
    id = id,
    tenantId = tenantId,
    name = name,
    keyHash = keyHash.take(10) + "...", // Don't expose full hash
    scopes = scopes.map { it.name },
    expiresAt = expiresAt?.toString(),
    createdAt = createdAt.toString(),
    lastUsedAt = lastUsedAt?.toString()
)

fun TenantUsage.toDTO() = TenantUsageDTO(
    tenantId = tenantId,
    periodStart = periodStart.toString(),
    periodEnd = periodEnd.toString(),
    evaluationsCount = evaluationsCount,
    flagsCount = flagsCount,
    apiCallsCount = apiCallsCount
)
