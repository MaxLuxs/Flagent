package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionRequest(
    val priceId: String,
    val successUrl: String,
    val cancelUrl: String,
    val trialPeriodDays: Int? = null
)

@Serializable
data class CreateCheckoutSessionResponse(
    val sessionId: String,
    val sessionUrl: String
)

@Serializable
data class CreatePortalSessionRequest(
    val returnUrl: String
)

@Serializable
data class CreatePortalSessionResponse(
    val sessionUrl: String
)

@Serializable
data class SubscriptionResponse(
    val id: Long,
    val tenantId: Long,
    val stripeSubscriptionId: String,
    val stripeCustomerId: String,
    val status: String,
    val currentPeriodStart: String,
    val currentPeriodEnd: String,
    val cancelAtPeriodEnd: Boolean,
    val canceledAt: String? = null,
    val trialStart: String? = null,
    val trialEnd: String? = null,
    val plan: PlanResponse,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PlanResponse(
    val priceId: String,
    val name: String,
    val amount: Long,
    val currency: String,
    val interval: String,
    val features: List<String>
)

@Serializable
data class InvoiceResponse(
    val id: Long,
    val tenantId: Long,
    val stripeInvoiceId: String,
    val status: String,
    val amountDue: String,
    val amountPaid: String,
    val currency: String,
    val periodStart: String,
    val periodEnd: String,
    val dueDate: String? = null,
    val paidAt: String? = null,
    val hostedInvoiceUrl: String? = null,
    val invoicePdf: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UsageStatsResponse(
    val tenantId: Long,
    val period: PeriodResponse,
    val metrics: Map<String, Long>
)

@Serializable
data class PeriodResponse(
    val start: String,
    val end: String
)
