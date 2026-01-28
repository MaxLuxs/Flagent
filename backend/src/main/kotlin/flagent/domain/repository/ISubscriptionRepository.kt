package flagent.domain.repository

import flagent.domain.entity.Subscription
import flagent.domain.entity.SubscriptionStatus

/**
 * Repository interface for subscription operations.
 */
interface ISubscriptionRepository {
    /**
     * Create a new subscription.
     */
    suspend fun create(subscription: Subscription): Subscription

    /**
     * Update existing subscription.
     */
    suspend fun update(subscription: Subscription): Subscription

    /**
     * Find subscription by ID.
     */
    suspend fun findById(id: Long): Subscription?

    /**
     * Find subscription by Stripe subscription ID.
     */
    suspend fun findByStripeId(stripeSubscriptionId: String): Subscription?

    /**
     * Find active subscription for tenant.
     */
    suspend fun findActiveByTenantId(tenantId: Long): Subscription?

    /**
     * Find all subscriptions for tenant.
     */
    suspend fun findAllByTenantId(tenantId: Long): List<Subscription>

    /**
     * Find subscriptions by status.
     */
    suspend fun findByStatus(status: SubscriptionStatus): List<Subscription>

    /**
     * Delete subscription by ID.
     */
    suspend fun deleteById(id: Long): Boolean
}
