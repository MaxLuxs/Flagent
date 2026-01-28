package flagent.domain.entity

import java.time.LocalDateTime

/**
 * TenantUser entity - represents a user within a tenant.
 *
 * Cross-tenant user management:
 * - Users can belong to multiple tenants
 * - Different roles per tenant
 */
data class TenantUser(
    val id: Long = 0,
    val tenantId: Long,
    val email: String,
    val role: TenantRole,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * User roles within a tenant.
 */
enum class TenantRole {
    /**
     * Owner - full control over tenant.
     * - Billing
     * - User management
     * - All feature flag operations
     */
    OWNER,

    /**
     * Admin - administrative access.
     * - User management (except owner)
     * - All feature flag operations
     */
    ADMIN,

    /**
     * Member - standard access.
     * - Create/edit flags
     * - View analytics
     */
    MEMBER,

    /**
     * Viewer - read-only access.
     * - View flags
     * - View analytics
     */
    VIEWER
}
