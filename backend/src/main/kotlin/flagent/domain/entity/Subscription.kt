package flagent.domain.entity

import java.time.LocalDateTime

/**
 * Subscription entity - represents a Stripe subscription.
 */
data class Subscription(
    val id: Long = 0,
    val tenantId: Long,
    val stripeSubscriptionId: String,
    val stripeCustomerId: String,
    val stripePriceId: String,
    val status: SubscriptionStatus,
    val currentPeriodStart: LocalDateTime,
    val currentPeriodEnd: LocalDateTime,
    val cancelAtPeriodEnd: Boolean = false,
    val canceledAt: LocalDateTime? = null,
    val trialStart: LocalDateTime? = null,
    val trialEnd: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Subscription status - mirrors Stripe subscription status.
 */
enum class SubscriptionStatus {
    /**
     * Active subscription - customer has access to features.
     */
    ACTIVE,

    /**
     * Trialing - subscription is in trial period.
     */
    TRIALING,

    /**
     * Past due - payment failed, but subscription is still active.
     */
    PAST_DUE,

    /**
     * Unpaid - payment failed multiple times, subscription is suspended.
     */
    UNPAID,

    /**
     * Canceled - subscription has been canceled.
     */
    CANCELED,

    /**
     * Incomplete - subscription created but payment pending.
     */
    INCOMPLETE,

    /**
     * Incomplete expired - subscription expired before first payment.
     */
    INCOMPLETE_EXPIRED
}
