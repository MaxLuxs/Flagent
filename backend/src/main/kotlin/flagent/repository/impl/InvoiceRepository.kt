package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Invoice
import flagent.domain.entity.InvoiceStatus
import flagent.domain.repository.IInvoiceRepository
import flagent.repository.tables.Invoices
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * Invoice repository implementation using Exposed.
 */
class InvoiceRepository : IInvoiceRepository {

    override suspend fun create(invoice: Invoice): Invoice = suspendTransaction {
        val id = Invoices.insertAndGetId {
            it[tenantId] = invoice.tenantId
            it[subscriptionId] = invoice.subscriptionId
            it[stripeInvoiceId] = invoice.stripeInvoiceId
            it[stripeCustomerId] = invoice.stripeCustomerId
            it[status] = invoice.status.name
            it[amountDue] = invoice.amountDue
            it[amountPaid] = invoice.amountPaid
            it[currency] = invoice.currency
            it[periodStart] = invoice.periodStart
            it[periodEnd] = invoice.periodEnd
            it[dueDate] = invoice.dueDate
            it[paidAt] = invoice.paidAt
            it[hostedInvoiceUrl] = invoice.hostedInvoiceUrl
            it[invoicePdf] = invoice.invoicePdf
            it[createdAt] = invoice.createdAt
            it[updatedAt] = invoice.updatedAt
        }
        invoice.copy(id = id.value)
    }

    override suspend fun update(invoice: Invoice): Invoice = suspendTransaction {
        Invoices.update({ Invoices.id eq invoice.id }) {
            it[tenantId] = invoice.tenantId
            it[subscriptionId] = invoice.subscriptionId
            it[stripeInvoiceId] = invoice.stripeInvoiceId
            it[stripeCustomerId] = invoice.stripeCustomerId
            it[status] = invoice.status.name
            it[amountDue] = invoice.amountDue
            it[amountPaid] = invoice.amountPaid
            it[currency] = invoice.currency
            it[periodStart] = invoice.periodStart
            it[periodEnd] = invoice.periodEnd
            it[dueDate] = invoice.dueDate
            it[paidAt] = invoice.paidAt
            it[hostedInvoiceUrl] = invoice.hostedInvoiceUrl
            it[invoicePdf] = invoice.invoicePdf
            it[updatedAt] = invoice.updatedAt
        }
        invoice
    }

    override suspend fun findById(id: Long): Invoice? = suspendTransaction {
        Invoices.selectAll()
            .where { Invoices.id eq id }
            .singleOrNull()
            ?.toInvoice()
    }

    override suspend fun findByStripeId(stripeInvoiceId: String): Invoice? = suspendTransaction {
        Invoices.selectAll()
            .where { Invoices.stripeInvoiceId eq stripeInvoiceId }
            .singleOrNull()
            ?.toInvoice()
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<Invoice> = suspendTransaction {
        Invoices.selectAll()
            .where { Invoices.tenantId eq tenantId }
            .orderBy(Invoices.createdAt to SortOrder.DESC)
            .map { it.toInvoice() }
    }

    override suspend fun findAllBySubscriptionId(subscriptionId: Long): List<Invoice> = suspendTransaction {
        Invoices.selectAll()
            .where { Invoices.subscriptionId eq subscriptionId }
            .orderBy(Invoices.createdAt to SortOrder.DESC)
            .map { it.toInvoice() }
    }

    override suspend fun findByStatus(status: InvoiceStatus): List<Invoice> = suspendTransaction {
        Invoices.selectAll()
            .where { Invoices.status eq status.name }
            .map { it.toInvoice() }
    }

    override suspend fun deleteById(id: Long): Boolean = suspendTransaction {
        Invoices.deleteWhere { Invoices.id eq id } > 0
    }

    private fun ResultRow.toInvoice() = Invoice(
        id = this[Invoices.id].value,
        tenantId = this[Invoices.tenantId],
        subscriptionId = this[Invoices.subscriptionId],
        stripeInvoiceId = this[Invoices.stripeInvoiceId],
        stripeCustomerId = this[Invoices.stripeCustomerId],
        status = InvoiceStatus.valueOf(this[Invoices.status]),
        amountDue = this[Invoices.amountDue],
        amountPaid = this[Invoices.amountPaid],
        currency = this[Invoices.currency],
        periodStart = this[Invoices.periodStart],
        periodEnd = this[Invoices.periodEnd],
        dueDate = this[Invoices.dueDate],
        paidAt = this[Invoices.paidAt],
        hostedInvoiceUrl = this[Invoices.hostedInvoiceUrl],
        invoicePdf = this[Invoices.invoicePdf],
        createdAt = this[Invoices.createdAt],
        updatedAt = this[Invoices.updatedAt]
    )
}
