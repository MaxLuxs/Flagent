package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Subscription
import flagent.domain.entity.SubscriptionStatus
import flagent.domain.repository.ISubscriptionRepository
import flagent.repository.tables.Subscriptions
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * Subscription repository implementation using Exposed.
 */
class SubscriptionRepository : ISubscriptionRepository {

    override suspend fun create(subscription: Subscription): Subscription = suspendTransaction {
        val id = Subscriptions.insertAndGetId {
            it[tenantId] = subscription.tenantId
            it[stripeSubscriptionId] = subscription.stripeSubscriptionId
            it[stripeCustomerId] = subscription.stripeCustomerId
            it[stripePriceId] = subscription.stripePriceId
            it[status] = subscription.status.name
            it[currentPeriodStart] = subscription.currentPeriodStart
            it[currentPeriodEnd] = subscription.currentPeriodEnd
            it[cancelAtPeriodEnd] = subscription.cancelAtPeriodEnd
            it[canceledAt] = subscription.canceledAt
            it[trialStart] = subscription.trialStart
            it[trialEnd] = subscription.trialEnd
            it[createdAt] = subscription.createdAt
            it[updatedAt] = subscription.updatedAt
        }
        subscription.copy(id = id.value)
    }

    override suspend fun update(subscription: Subscription): Subscription = suspendTransaction {
        Subscriptions.update({ Subscriptions.id eq subscription.id }) {
            it[tenantId] = subscription.tenantId
            it[stripeSubscriptionId] = subscription.stripeSubscriptionId
            it[stripeCustomerId] = subscription.stripeCustomerId
            it[stripePriceId] = subscription.stripePriceId
            it[status] = subscription.status.name
            it[currentPeriodStart] = subscription.currentPeriodStart
            it[currentPeriodEnd] = subscription.currentPeriodEnd
            it[cancelAtPeriodEnd] = subscription.cancelAtPeriodEnd
            it[canceledAt] = subscription.canceledAt
            it[trialStart] = subscription.trialStart
            it[trialEnd] = subscription.trialEnd
            it[updatedAt] = subscription.updatedAt
        }
        subscription
    }

    override suspend fun findById(id: Long): Subscription? = suspendTransaction {
        Subscriptions.selectAll()
            .where { Subscriptions.id eq id }
            .singleOrNull()
            ?.toSubscription()
    }

    override suspend fun findByStripeId(stripeSubscriptionId: String): Subscription? = suspendTransaction {
        Subscriptions.selectAll()
            .where { Subscriptions.stripeSubscriptionId eq stripeSubscriptionId }
            .singleOrNull()
            ?.toSubscription()
    }

    override suspend fun findActiveByTenantId(tenantId: Long): Subscription? = suspendTransaction {
        Subscriptions.selectAll()
            .where { 
                (Subscriptions.tenantId eq tenantId) and 
                (Subscriptions.status inList listOf(SubscriptionStatus.ACTIVE.name, SubscriptionStatus.TRIALING.name))
            }
            .orderBy(Subscriptions.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toSubscription()
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<Subscription> = suspendTransaction {
        Subscriptions.selectAll()
            .where { Subscriptions.tenantId eq tenantId }
            .orderBy(Subscriptions.createdAt to SortOrder.DESC)
            .map { it.toSubscription() }
    }

    override suspend fun findByStatus(status: SubscriptionStatus): List<Subscription> = suspendTransaction {
        Subscriptions.selectAll()
            .where { Subscriptions.status eq status.name }
            .map { it.toSubscription() }
    }

    override suspend fun deleteById(id: Long): Boolean = suspendTransaction {
        Subscriptions.deleteWhere { Subscriptions.id eq id } > 0
    }

    private fun ResultRow.toSubscription() = Subscription(
        id = this[Subscriptions.id].value,
        tenantId = this[Subscriptions.tenantId],
        stripeSubscriptionId = this[Subscriptions.stripeSubscriptionId],
        stripeCustomerId = this[Subscriptions.stripeCustomerId],
        stripePriceId = this[Subscriptions.stripePriceId],
        status = SubscriptionStatus.valueOf(this[Subscriptions.status]),
        currentPeriodStart = this[Subscriptions.currentPeriodStart],
        currentPeriodEnd = this[Subscriptions.currentPeriodEnd],
        cancelAtPeriodEnd = this[Subscriptions.cancelAtPeriodEnd],
        canceledAt = this[Subscriptions.canceledAt],
        trialStart = this[Subscriptions.trialStart],
        trialEnd = this[Subscriptions.trialEnd],
        createdAt = this[Subscriptions.createdAt],
        updatedAt = this[Subscriptions.updatedAt]
    )
}
