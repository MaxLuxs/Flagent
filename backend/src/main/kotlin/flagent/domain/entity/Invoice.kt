package flagent.domain.entity

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Invoice entity - represents a Stripe invoice.
 */
data class Invoice(
    val id: Long = 0,
    val tenantId: Long,
    val subscriptionId: Long?,
    val stripeInvoiceId: String,
    val stripeCustomerId: String,
    val status: InvoiceStatus,
    val amountDue: BigDecimal,
    val amountPaid: BigDecimal,
    val currency: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val dueDate: LocalDateTime?,
    val paidAt: LocalDateTime? = null,
    val hostedInvoiceUrl: String? = null,
    val invoicePdf: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Invoice status - mirrors Stripe invoice status.
 */
enum class InvoiceStatus {
    /**
     * Draft invoice - not finalized yet.
     */
    DRAFT,

    /**
     * Open invoice - awaiting payment.
     */
    OPEN,

    /**
     * Paid invoice - payment successful.
     */
    PAID,

    /**
     * Uncollectible - unable to collect payment.
     */
    UNCOLLECTIBLE,

    /**
     * Void - invoice voided.
     */
    VOID
}
