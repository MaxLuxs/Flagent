package flagent.service.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

// ============================================================================
// Request DTOs
// ============================================================================

/**
 * Request to create a Stripe checkout session.
 */
@Serializable
data class CreateCheckoutSessionRequest(
    val priceId: String,
    val successUrl: String,
    val cancelUrl: String,
    val trialPeriodDays: Int? = null
)

/**
 * Response containing checkout session URL.
 */
@Serializable
data class CreateCheckoutSessionResponse(
    val sessionId: String,
    val sessionUrl: String
)

/**
 * Request to create a billing portal session.
 */
@Serializable
data class CreatePortalSessionRequest(
    val returnUrl: String
)

/**
 * Response containing portal session URL.
 */
@Serializable
data class CreatePortalSessionResponse(
    val sessionUrl: String
)

// ============================================================================
// Subscription DTOs
// ============================================================================

/**
 * Subscription DTO for API responses.
 */
@Serializable
data class SubscriptionDTO(
    val id: Long,
    val tenantId: Long,
    val stripeSubscriptionId: String,
    val stripeCustomerId: String,
    val status: String,
    val currentPeriodStart: String, // ISO-8601 string
    val currentPeriodEnd: String, // ISO-8601 string
    val cancelAtPeriodEnd: Boolean,
    val canceledAt: String? = null, // ISO-8601 string
    val trialStart: String? = null, // ISO-8601 string
    val trialEnd: String? = null, // ISO-8601 string
    val plan: PlanDTO,
    val createdAt: String, // ISO-8601 string
    val updatedAt: String // ISO-8601 string
)

/**
 * Plan details DTO.
 */
@Serializable
data class PlanDTO(
    val priceId: String,
    val name: String,
    val amount: Long, // Amount in cents
    val currency: String,
    val interval: String, // month, year
    val features: List<String>
)

// ============================================================================
// Invoice DTOs
// ============================================================================

/**
 * Invoice DTO for API responses.
 */
@Serializable
data class InvoiceDTO(
    val id: Long,
    val tenantId: Long,
    val stripeInvoiceId: String,
    val status: String,
    val amountDue: String, // String representation of BigDecimal
    val amountPaid: String, // String representation of BigDecimal
    val currency: String,
    val periodStart: String, // ISO-8601 string
    val periodEnd: String, // ISO-8601 string
    val dueDate: String? = null, // ISO-8601 string
    val paidAt: String? = null, // ISO-8601 string
    val hostedInvoiceUrl: String? = null,
    val invoicePdf: String? = null,
    val createdAt: String, // ISO-8601 string
    val updatedAt: String // ISO-8601 string
)

// ============================================================================
// Usage DTOs
// ============================================================================

/**
 * Usage record DTO for API responses.
 */
@Serializable
data class UsageRecordDTO(
    val id: Long,
    val tenantId: Long,
    val metricType: String,
    val quantity: Long,
    val timestamp: String, // ISO-8601 string
    val reportedToStripe: Boolean
)

/**
 * Aggregated usage statistics DTO.
 */
@Serializable
data class UsageStatsDTO(
    val tenantId: Long,
    val period: PeriodDTO,
    val metrics: Map<String, Long> // metricType -> quantity
)

/**
 * Time period DTO.
 */
@Serializable
data class PeriodDTO(
    val start: String, // ISO-8601 string
    val end: String // ISO-8601 string
)

// ============================================================================
// Webhook DTOs
// ============================================================================

/**
 * Stripe webhook event DTO.
 */
@Serializable
data class StripeWebhookEventDTO(
    val id: String,
    val type: String,
    val data: Map<String, String> // Simplified, actual implementation uses dynamic JSON
)
