package flagent.service

import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.*
import com.stripe.model.checkout.Session
import com.stripe.model.billingportal.Session as PortalSession
import com.stripe.model.Invoice as StripeInvoice
import com.stripe.model.Subscription as StripeSubscription
import com.stripe.model.ExpandableField
import com.stripe.param.*
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import java.net.HttpURLConnection
import java.net.URL
import flagent.config.AppConfig
import flagent.domain.entity.*
import flagent.domain.repository.IInvoiceRepository
import flagent.domain.repository.ISubscriptionRepository
import flagent.domain.repository.ITenantRepository
import flagent.domain.repository.IUsageRecordRepository
import flagent.service.dto.*
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Service for Stripe billing integration.
 *
 * Handles:
 * - Checkout session creation
 * - Subscription management
 * - Invoice tracking
 * - Usage-based billing
 * - Webhook processing
 */
class BillingService(
    private val subscriptionRepository: ISubscriptionRepository,
    private val invoiceRepository: IInvoiceRepository,
    private val usageRecordRepository: IUsageRecordRepository,
    private val tenantRepository: ITenantRepository
) {
    private val logger = KotlinLogging.logger {}

    init {
        // Initialize Stripe API with secret key from config
        Stripe.apiKey = AppConfig.stripeSecretKey
    }

    // ========================================================================
    // Checkout & Portal Sessions
    // ========================================================================

    /**
     * Create a Stripe checkout session for subscription signup.
     */
    suspend fun createCheckoutSession(
        tenantId: Long,
        request: CreateCheckoutSessionRequest
    ): CreateCheckoutSessionResponse {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")

        // Get or create Stripe customer
        val customerId = getOrCreateStripeCustomer(tenant)

        // Create checkout session
        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(customerId)
            .setSuccessUrl(request.successUrl)
            .setCancelUrl(request.cancelUrl)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(request.priceId)
                    .setQuantity(1L)
                    .build()
            )
            .apply {
                request.trialPeriodDays?.let { days ->
                    setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                            .setTrialPeriodDays(days.toLong())
                            .build()
                    )
                }
            }
            .setClientReferenceId(tenantId.toString())
            .build()

        val session = try {
            Session.create(params)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create checkout session for tenant $tenantId" }
            throw RuntimeException("Failed to create checkout session", e)
        }

        logger.info { "Created checkout session ${session.id} for tenant $tenantId" }

        return CreateCheckoutSessionResponse(
            sessionId = session.id,
            sessionUrl = session.url
        )
    }

    /**
     * Create a Stripe billing portal session for subscription management.
     */
    suspend fun createPortalSession(
        tenantId: Long,
        request: CreatePortalSessionRequest
    ): CreatePortalSessionResponse {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")

        val customerId = tenant.stripeCustomerId
            ?: throw IllegalArgumentException("Tenant $tenantId has no Stripe customer")

        val params = PortalSessionCreateParams.builder()
            .setCustomer(customerId)
            .setReturnUrl(request.returnUrl)
            .build()

        val session = try {
            PortalSession.create(params)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create portal session for tenant $tenantId" }
            throw RuntimeException("Failed to create portal session", e)
        }

        logger.info { "Created portal session ${session.id} for tenant $tenantId" }

        return CreatePortalSessionResponse(sessionUrl = session.url)
    }

    // ========================================================================
    // Subscription Management
    // ========================================================================

    /**
     * Get active subscription for tenant.
     */
    suspend fun getActiveSubscription(tenantId: Long): SubscriptionDTO? {
        val subscription = subscriptionRepository.findActiveByTenantId(tenantId)
            ?: return null

        // Fetch plan details from Stripe
        val stripeSubscription = try {
            com.stripe.model.Subscription.retrieve(subscription.stripeSubscriptionId)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to fetch subscription from Stripe: ${subscription.stripeSubscriptionId}" }
            return null
        }

        val price = stripeSubscription.items.data.firstOrNull()?.price
        val plan = price?.let {
            PlanDTO(
                priceId = it.id,
                name = it.product as? String ?: "Unknown",
                amount = it.unitAmount ?: 0L,
                currency = it.currency,
                interval = it.recurring?.interval ?: "month",
                features = emptyList() // TODO: fetch from metadata
            )
        } ?: PlanDTO("", "Unknown", 0L, "usd", "month", emptyList())

        return SubscriptionDTO(
            id = subscription.id,
            tenantId = subscription.tenantId,
            stripeSubscriptionId = subscription.stripeSubscriptionId,
            stripeCustomerId = subscription.stripeCustomerId,
            status = subscription.status.name,
            currentPeriodStart = subscription.currentPeriodStart.toString(),
            currentPeriodEnd = subscription.currentPeriodEnd.toString(),
            cancelAtPeriodEnd = subscription.cancelAtPeriodEnd,
            canceledAt = subscription.canceledAt?.toString(),
            trialStart = subscription.trialStart?.toString(),
            trialEnd = subscription.trialEnd?.toString(),
            plan = plan,
            createdAt = subscription.createdAt.toString(),
            updatedAt = subscription.updatedAt.toString()
        )
    }

    /**
     * Cancel subscription at period end.
     */
    suspend fun cancelSubscription(tenantId: Long, cancelAtPeriodEnd: Boolean = true) {
        val subscription = subscriptionRepository.findActiveByTenantId(tenantId)
            ?: throw IllegalArgumentException("No active subscription for tenant $tenantId")

        try {
            val params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(cancelAtPeriodEnd)
                .build()

            com.stripe.model.Subscription.retrieve(subscription.stripeSubscriptionId).update(params)

            // Update local subscription
            subscriptionRepository.update(
                subscription.copy(
                    cancelAtPeriodEnd = cancelAtPeriodEnd,
                    canceledAt = if (cancelAtPeriodEnd) LocalDateTime.now() else null,
                    updatedAt = LocalDateTime.now()
                )
            )

            logger.info { "Canceled subscription ${subscription.stripeSubscriptionId} for tenant $tenantId" }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to cancel subscription: ${subscription.stripeSubscriptionId}" }
            throw RuntimeException("Failed to cancel subscription", e)
        }
    }

    // ========================================================================
    // Invoice Management
    // ========================================================================

    /**
     * Get invoices for tenant.
     */
    suspend fun getInvoices(tenantId: Long, limit: Int = 10): List<InvoiceDTO> {
        return invoiceRepository.findAllByTenantId(tenantId)
            .take(limit)
            .map { invoice ->
                InvoiceDTO(
                    id = invoice.id,
                    tenantId = invoice.tenantId,
                    stripeInvoiceId = invoice.stripeInvoiceId,
                    status = invoice.status.name,
                    amountDue = invoice.amountDue.toString(),
                    amountPaid = invoice.amountPaid.toString(),
                    currency = invoice.currency,
                    periodStart = invoice.periodStart.toString(),
                    periodEnd = invoice.periodEnd.toString(),
                    dueDate = invoice.dueDate?.toString(),
                    paidAt = invoice.paidAt?.toString(),
                    hostedInvoiceUrl = invoice.hostedInvoiceUrl,
                    invoicePdf = invoice.invoicePdf,
                    createdAt = invoice.createdAt.toString(),
                    updatedAt = invoice.updatedAt.toString()
                )
            }
    }

    // ========================================================================
    // Usage Tracking & Reporting
    // ========================================================================

    /**
     * Record usage for metered billing.
     */
    suspend fun recordUsage(
        tenantId: Long,
        metricType: UsageMetricType,
        quantity: Long
    ) {
        val subscription = subscriptionRepository.findActiveByTenantId(tenantId)
            ?: return // No active subscription, skip recording

        val record = UsageRecord(
            tenantId = tenantId,
            subscriptionId = subscription.id,
            metricType = metricType,
            quantity = quantity,
            timestamp = LocalDateTime.now()
        )

        usageRecordRepository.create(record)
        logger.debug { "Recorded usage for tenant $tenantId: $metricType = $quantity" }
    }

    /**
     * Report unreported usage to Stripe for metered billing.
     */
    suspend fun reportUsageToStripe(tenantId: Long) {
        val subscription = subscriptionRepository.findActiveByTenantId(tenantId)
            ?: return

        val unreportedRecords = usageRecordRepository.findUnreportedByTenantId(tenantId)
        if (unreportedRecords.isEmpty()) {
            return
        }

        // Aggregate by metric type
        val aggregated = unreportedRecords.groupBy { it.metricType }
            .mapValues { (_, records) -> records.sumOf { it.quantity } }

        // Report to Stripe
        try {
            val stripeSubscription = StripeSubscription.retrieve(subscription.stripeSubscriptionId)
            val subscriptionItem = stripeSubscription.items.data.firstOrNull()
                ?: throw RuntimeException("No subscription item found")

            aggregated.forEach { (metricType, quantity) ->
                // UsageRecord class removed from Stripe SDK 31.x - use direct API call
                val url = URL("https://api.stripe.com/v1/subscription_items/${subscriptionItem.id}/usage_records")
                val connection = url.openConnection() as HttpURLConnection
                
                try {
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Authorization", "Bearer ${Stripe.apiKey}")
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connection.doOutput = true
                    
                    val params = "quantity=$quantity&timestamp=${Instant.now().epochSecond}&action=set"
                    connection.outputStream.write(params.toByteArray())
                    
                    val responseCode = connection.responseCode
                    if (responseCode == 200 || responseCode == 201) {
                        val response = connection.inputStream.bufferedReader().readText()
                        val usageRecordId = response.substringAfter("\"id\": \"").substringBefore("\"")
                        
                        // Mark as reported
                        val recordIds = unreportedRecords
                            .filter { it.metricType == metricType }
                            .map { it.id }
                        usageRecordRepository.markAsReported(recordIds, usageRecordId)
                        
                        logger.info { "Reported usage to Stripe for tenant $tenantId: $metricType = $quantity" }
                    } else {
                        val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                        logger.error { "Failed to report usage to Stripe (HTTP $responseCode): $errorResponse" }
                    }
                } finally {
                    connection.disconnect()
                }
            }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to report usage to Stripe for tenant $tenantId" }
        }
    }

    /**
     * Get usage statistics for tenant.
     */
    suspend fun getUsageStats(
        tenantId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): UsageStatsDTO {
        val metrics = UsageMetricType.values().associate { metricType ->
            metricType.name to usageRecordRepository.getAggregatedUsage(
                tenantId = tenantId,
                metricType = metricType,
                startTime = startTime,
                endTime = endTime
            )
        }

        return UsageStatsDTO(
            tenantId = tenantId,
            period = PeriodDTO(
                start = startTime.toString(),
                end = endTime.toString()
            ),
            metrics = metrics
        )
    }

    // ========================================================================
    // Webhook Processing
    // ========================================================================

    /**
     * Process Stripe webhook event.
     */
    suspend fun processWebhookEvent(event: Event) {
        logger.info { "Processing Stripe webhook: ${event.type}" }

        when (event.type) {
            "checkout.session.completed" -> handleCheckoutCompleted(event)
            "customer.subscription.created" -> handleSubscriptionCreated(event)
            "customer.subscription.updated" -> handleSubscriptionUpdated(event)
            "customer.subscription.deleted" -> handleSubscriptionDeleted(event)
            "invoice.created" -> handleInvoiceCreated(event)
            "invoice.updated" -> handleInvoiceUpdated(event)
            "invoice.paid" -> handleInvoicePaid(event)
            "invoice.payment_failed" -> handleInvoicePaymentFailed(event)
            else -> logger.debug { "Unhandled webhook event type: ${event.type}" }
        }
    }

    private suspend fun handleCheckoutCompleted(event: Event) {
        val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
            ?: return

        val tenantId = session.clientReferenceId?.toLongOrNull()
            ?: return

        val tenant = tenantRepository.findById(tenantId)
            ?: return

        // Update tenant with customer and subscription IDs
        tenantRepository.update(
            tenant.copy(
                stripeCustomerId = session.customer,
                stripeSubscriptionId = session.subscription,
                updatedAt = LocalDateTime.now()
            )
        )

        logger.info { "Checkout completed for tenant $tenantId" }
    }

    private suspend fun handleSubscriptionCreated(event: Event) {
        val stripeSubscription = event.dataObjectDeserializer.`object`.orElse(null) as? StripeSubscription
            ?: return

        // Find tenant by customer ID
        val tenant = tenantRepository.findByStripeCustomerId(stripeSubscription.customer)
            ?: return

        // Get current period from first subscription item
        val firstItem = stripeSubscription.items.data.firstOrNull()
        
        val subscription = Subscription(
            tenantId = tenant.id,
            stripeSubscriptionId = stripeSubscription.id,
            stripeCustomerId = stripeSubscription.customer,
            stripePriceId = firstItem?.price?.id ?: "",
            status = SubscriptionStatus.valueOf(stripeSubscription.status.uppercase()),
            currentPeriodStart = firstItem?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(it.currentPeriodStart),
                    ZoneId.systemDefault()
                )
            } ?: LocalDateTime.now(),
            currentPeriodEnd = firstItem?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(it.currentPeriodEnd),
                    ZoneId.systemDefault()
                )
            } ?: LocalDateTime.now(),
            cancelAtPeriodEnd = stripeSubscription.cancelAtPeriodEnd,
            trialStart = stripeSubscription.trialStart?.let {
                LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
            },
            trialEnd = stripeSubscription.trialEnd?.let {
                LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
            }
        )

        subscriptionRepository.create(subscription)
        logger.info { "Created subscription ${subscription.stripeSubscriptionId} for tenant ${tenant.id}" }
    }

    private suspend fun handleSubscriptionUpdated(event: Event) {
        val stripeSubscription = event.dataObjectDeserializer.`object`.orElse(null) as? StripeSubscription
            ?: return

        val subscription = subscriptionRepository.findByStripeId(stripeSubscription.id)
            ?: return

        // Get current period from first subscription item
        val firstItem = stripeSubscription.items.data.firstOrNull()

        val updated = subscription.copy(
            status = SubscriptionStatus.valueOf(stripeSubscription.status.uppercase()),
            currentPeriodStart = firstItem?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(it.currentPeriodStart),
                    ZoneId.systemDefault()
                )
            } ?: subscription.currentPeriodStart,
            currentPeriodEnd = firstItem?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(it.currentPeriodEnd),
                    ZoneId.systemDefault()
                )
            } ?: subscription.currentPeriodEnd,
            cancelAtPeriodEnd = stripeSubscription.cancelAtPeriodEnd,
            canceledAt = stripeSubscription.canceledAt?.let {
                LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
            },
            updatedAt = LocalDateTime.now()
        )

        subscriptionRepository.update(updated)
        logger.info { "Updated subscription ${subscription.stripeSubscriptionId}" }
    }

    private suspend fun handleSubscriptionDeleted(event: Event) {
        val stripeSubscription = event.dataObjectDeserializer.`object`.orElse(null) as? StripeSubscription
            ?: return

        val subscription = subscriptionRepository.findByStripeId(stripeSubscription.id)
            ?: return

        val updated = subscription.copy(
            status = SubscriptionStatus.CANCELED,
            canceledAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        subscriptionRepository.update(updated)

        // Suspend tenant
        val tenant = tenantRepository.findById(subscription.tenantId)
            ?: return
        tenantRepository.update(tenant.copy(status = TenantStatus.SUSPENDED))

        logger.info { "Deleted subscription ${subscription.stripeSubscriptionId}, suspended tenant ${tenant.id}" }
    }

    private suspend fun handleInvoiceCreated(event: Event) {
        val stripeInvoice = event.dataObjectDeserializer.`object`.orElse(null) as? StripeInvoice
            ?: return

        val tenant = tenantRepository.findByStripeCustomerId(stripeInvoice.customer)
            ?: return

        // Get subscription ID from lines - subscription invoices have subscription ID in their line items
        val subscriptionIdStr = stripeInvoice.lines?.data?.firstOrNull()?.subscription
        val subscription = subscriptionIdStr?.let { subscriptionRepository.findByStripeId(it) }

        val invoice = flagent.domain.entity.Invoice(
            tenantId = tenant.id,
            subscriptionId = subscription?.id,
            stripeInvoiceId = stripeInvoice.id,
            stripeCustomerId = stripeInvoice.customer,
            status = InvoiceStatus.valueOf(stripeInvoice.status?.uppercase() ?: "DRAFT"),
            amountDue = BigDecimal.valueOf(stripeInvoice.amountDue ?: 0L).divide(BigDecimal.valueOf(100)),
            amountPaid = BigDecimal.valueOf(stripeInvoice.amountPaid ?: 0L).divide(BigDecimal.valueOf(100)),
            currency = stripeInvoice.currency,
            periodStart = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(stripeInvoice.periodStart ?: Instant.now().epochSecond),
                ZoneId.systemDefault()
            ),
            periodEnd = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(stripeInvoice.periodEnd ?: Instant.now().epochSecond),
                ZoneId.systemDefault()
            ),
            dueDate = stripeInvoice.dueDate?.let {
                LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
            },
            hostedInvoiceUrl = stripeInvoice.hostedInvoiceUrl,
            invoicePdf = stripeInvoice.invoicePdf
        )

        invoiceRepository.create(invoice)
        logger.info { "Created invoice ${invoice.stripeInvoiceId} for tenant ${tenant.id}" }
    }

    private suspend fun handleInvoiceUpdated(event: Event) {
        val stripeInvoice = event.dataObjectDeserializer.`object`.orElse(null) as? StripeInvoice
            ?: return

        val invoice = invoiceRepository.findByStripeId(stripeInvoice.id)
            ?: return

        val updated = invoice.copy(
            status = InvoiceStatus.valueOf(stripeInvoice.status?.uppercase() ?: "DRAFT"),
            amountDue = BigDecimal.valueOf(stripeInvoice.amountDue ?: 0L).divide(BigDecimal.valueOf(100)),
            amountPaid = BigDecimal.valueOf(stripeInvoice.amountPaid ?: 0L).divide(BigDecimal.valueOf(100)),
            hostedInvoiceUrl = stripeInvoice.hostedInvoiceUrl,
            invoicePdf = stripeInvoice.invoicePdf,
            updatedAt = LocalDateTime.now()
        )

        invoiceRepository.update(updated)
        logger.info { "Updated invoice ${invoice.stripeInvoiceId}" }
    }

    private suspend fun handleInvoicePaid(event: Event) {
        val stripeInvoice = event.dataObjectDeserializer.`object`.orElse(null) as? StripeInvoice
            ?: return

        val invoice = invoiceRepository.findByStripeId(stripeInvoice.id)
            ?: return

        val updated = invoice.copy(
            status = InvoiceStatus.PAID,
            amountPaid = BigDecimal.valueOf(stripeInvoice.amountPaid ?: 0L).divide(BigDecimal.valueOf(100)),
            paidAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        invoiceRepository.update(updated)
        logger.info { "Invoice ${invoice.stripeInvoiceId} paid" }
    }

    private suspend fun handleInvoicePaymentFailed(event: Event) {
        val stripeInvoice = event.dataObjectDeserializer.`object`.orElse(null) as? StripeInvoice
            ?: return

        val tenant = tenantRepository.findByStripeCustomerId(stripeInvoice.customer)
            ?: return

        // Update tenant status to suspended
        tenantRepository.update(tenant.copy(status = TenantStatus.SUSPENDED))

        logger.warn { "Invoice payment failed for tenant ${tenant.id}, status set to SUSPENDED" }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private suspend fun getOrCreateStripeCustomer(tenant: Tenant): String {
        tenant.stripeCustomerId?.let { return it }

        // Create new Stripe customer
        val params = CustomerCreateParams.builder()
            .setName(tenant.name)
            .setEmail(tenant.billingEmail)
            .putMetadata("tenant_id", tenant.id.toString())
            .build()

        val customer = try {
            Customer.create(params)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create Stripe customer for tenant ${tenant.id}" }
            throw RuntimeException("Failed to create Stripe customer", e)
        }

        // Update tenant with customer ID
        tenantRepository.update(
            tenant.copy(
                stripeCustomerId = customer.id,
                updatedAt = LocalDateTime.now()
            )
        )

        logger.info { "Created Stripe customer ${customer.id} for tenant ${tenant.id}" }
        return customer.id
    }
}
