package flagent.domain.repository

import flagent.domain.entity.Invoice
import flagent.domain.entity.InvoiceStatus

/**
 * Repository interface for invoice operations.
 */
interface IInvoiceRepository {
    /**
     * Create a new invoice.
     */
    suspend fun create(invoice: Invoice): Invoice

    /**
     * Update existing invoice.
     */
    suspend fun update(invoice: Invoice): Invoice

    /**
     * Find invoice by ID.
     */
    suspend fun findById(id: Long): Invoice?

    /**
     * Find invoice by Stripe invoice ID.
     */
    suspend fun findByStripeId(stripeInvoiceId: String): Invoice?

    /**
     * Find all invoices for tenant.
     */
    suspend fun findAllByTenantId(tenantId: Long): List<Invoice>

    /**
     * Find all invoices for subscription.
     */
    suspend fun findAllBySubscriptionId(subscriptionId: Long): List<Invoice>

    /**
     * Find invoices by status.
     */
    suspend fun findByStatus(status: InvoiceStatus): List<Invoice>

    /**
     * Delete invoice by ID.
     */
    suspend fun deleteById(id: Long): Boolean
}
