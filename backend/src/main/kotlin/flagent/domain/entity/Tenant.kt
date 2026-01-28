package flagent.domain.entity

import java.time.LocalDateTime

/**
 * Tenant entity - represents a SaaS tenant.
 *
 * Schema-per-tenant approach:
 * - Each tenant has a dedicated database schema
 * - Strong isolation between tenants
 * - Scalable and secure
 */
data class Tenant(
    val id: Long = 0,
    val key: String,
    val name: String,
    val plan: TenantPlan,
    val status: TenantStatus,
    val schemaName: String,
    val stripeCustomerId: String? = null,
    val stripeSubscriptionId: String? = null,
    val billingEmail: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

/**
 * Tenant plan tiers.
 */
enum class TenantPlan {
    /**
     * Starter plan - for small teams and testing.
     * - 100k evaluations/month
     * - 10 flags
     * - 2 environments
     * - Email support
     */
    STARTER,

    /**
     * Growth plan - for growing teams.
     * - 1M evaluations/month
     * - 50 flags
     * - 5 environments
     * - Priority support
     */
    GROWTH,

    /**
     * Scale plan - for large teams.
     * - 10M evaluations/month
     * - Unlimited flags
     * - Unlimited environments
     * - Priority support
     * - Webhooks
     */
    SCALE,

    /**
     * Enterprise plan - custom requirements.
     * - Unlimited evaluations
     * - Unlimited flags
     * - Unlimited environments
     * - SSO/SAML
     * - RBAC
     * - SLA 99.9%
     * - Dedicated support
     */
    ENTERPRISE
}

/**
 * Tenant status.
 */
enum class TenantStatus {
    /**
     * Active tenant - can use service.
     */
    ACTIVE,

    /**
     * Suspended tenant - temporarily blocked.
     * Reason: payment failure, abuse, etc.
     */
    SUSPENDED,

    /**
     * Cancelled tenant - soft deleted.
     * Schema will be deleted after grace period.
     */
    CANCELLED,

    /**
     * Deleted tenant - schema deleted.
     */
    DELETED
}
