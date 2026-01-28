package flagent.domain.entity

import java.time.LocalDateTime

/**
 * TenantApiKey entity - API keys for tenant access.
 *
 * Security:
 * - Hashed keys (never store plaintext)
 * - Scopes for fine-grained access
 * - Expiration support
 * - Usage tracking
 */
data class TenantApiKey(
    val id: Long = 0,
    val tenantId: Long,
    val keyHash: String,
    val name: String,
    val scopes: List<ApiKeyScope> = emptyList(),
    val expiresAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUsedAt: LocalDateTime? = null
)

/**
 * API key scopes for fine-grained access control.
 */
enum class ApiKeyScope {
    /**
     * Read flags - can fetch flag configuration.
     */
    FLAGS_READ,

    /**
     * Write flags - can create/update/delete flags.
     */
    FLAGS_WRITE,

    /**
     * Evaluate - can evaluate flags.
     */
    EVALUATE,

    /**
     * Analytics - can view analytics data.
     */
    ANALYTICS_READ,

    /**
     * Admin - full access (user management, billing).
     */
    ADMIN
}

/**
 * Helper to check if key has scope.
 */
fun TenantApiKey.hasScope(scope: ApiKeyScope): Boolean {
    return scopes.contains(scope) || scopes.contains(ApiKeyScope.ADMIN)
}

/**
 * Helper to check if key is expired.
 */
fun TenantApiKey.isExpired(): Boolean {
    return expiresAt?.isBefore(LocalDateTime.now()) == true
}
