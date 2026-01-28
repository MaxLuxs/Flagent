package flagent.route

import com.stripe.model.Event
import com.stripe.net.Webhook
import flagent.config.AppConfig
import flagent.domain.entity.UsageMetricType
import flagent.middleware.UserPrincipal
import flagent.service.BillingService
import flagent.service.dto.CreateCheckoutSessionRequest
import flagent.service.dto.CreatePortalSessionRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Configure billing routes.
 *
 * Endpoints:
 * - POST /api/billing/checkout - Create checkout session
 * - POST /api/billing/portal - Create billing portal session
 * - GET /api/billing/subscription - Get active subscription
 * - POST /api/billing/subscription/cancel - Cancel subscription
 * - GET /api/billing/invoices - Get invoices
 * - GET /api/billing/usage - Get usage statistics
 * - POST /api/billing/webhook - Stripe webhook endpoint (public)
 */
fun Routing.configureBillingRoutes(billingService: BillingService) {
    route("/api/billing") {
        // Protected routes (require authentication)
        authenticate("jwt-auth", optional = false) {
            /**
             * Create Stripe checkout session.
             *
             * POST /api/billing/checkout
             * Body: CreateCheckoutSessionRequest
             * Response: CreateCheckoutSessionResponse
             */
            post("/checkout") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                val request = try {
                    call.receive<CreateCheckoutSessionRequest>()
                } catch (e: Exception) {
                    logger.error(e) { "Failed to parse checkout session request" }
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                }

                try {
                    val response = billingService.createCheckoutSession(tenantId, request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to create checkout session for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create checkout session")
                }
            }

            /**
             * Create Stripe billing portal session.
             *
             * POST /api/billing/portal
             * Body: CreatePortalSessionRequest
             * Response: CreatePortalSessionResponse
             */
            post("/portal") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                val request = try {
                    call.receive<CreatePortalSessionRequest>()
                } catch (e: Exception) {
                    logger.error(e) { "Failed to parse portal session request" }
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                }

                try {
                    val response = billingService.createPortalSession(tenantId, request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to create portal session for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create portal session")
                }
            }

            /**
             * Get active subscription.
             *
             * GET /api/billing/subscription
             * Response: SubscriptionDTO or 404
             */
            get("/subscription") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                try {
                    val subscription = billingService.getActiveSubscription(tenantId)
                    if (subscription != null) {
                        call.respond(HttpStatusCode.OK, subscription)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No active subscription")
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to get subscription for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get subscription")
                }
            }

            /**
             * Cancel subscription.
             *
             * POST /api/billing/subscription/cancel
             * Query params: cancelAtPeriodEnd (default: true)
             */
            post("/subscription/cancel") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                val cancelAtPeriodEnd = call.request.queryParameters["cancelAtPeriodEnd"]?.toBoolean() ?: true

                try {
                    billingService.cancelSubscription(tenantId, cancelAtPeriodEnd)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Subscription canceled"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Subscription not found")
                } catch (e: Exception) {
                    logger.error(e) { "Failed to cancel subscription for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to cancel subscription")
                }
            }

            /**
             * Get invoices.
             *
             * GET /api/billing/invoices
             * Query params: limit (default: 10)
             * Response: List<InvoiceDTO>
             */
            get("/invoices") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

                try {
                    val invoices = billingService.getInvoices(tenantId, limit)
                    call.respond(HttpStatusCode.OK, invoices)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to get invoices for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get invoices")
                }
            }

            /**
             * Get usage statistics.
             *
             * GET /api/billing/usage
             * Query params:
             * - startTime (ISO-8601, default: start of month)
             * - endTime (ISO-8601, default: now)
             * Response: UsageStatsDTO
             */
            get("/usage") {
                val tenantId = call.principal<UserPrincipal>()?.tenantId
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "Tenant ID not found")

                val now = LocalDateTime.now()
                val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)

                val startTime = call.request.queryParameters["startTime"]?.let {
                    LocalDateTime.parse(it)
                } ?: startOfMonth

                val endTime = call.request.queryParameters["endTime"]?.let {
                    LocalDateTime.parse(it)
                } ?: now

                try {
                    val stats = billingService.getUsageStats(tenantId, startTime, endTime)
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to get usage stats for tenant $tenantId" }
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get usage stats")
                }
            }
        }

        /**
         * Stripe webhook endpoint (public, no authentication).
         *
         * POST /api/billing/webhook
         * Headers: Stripe-Signature
         * Body: Stripe Event JSON
         *
         * Validates webhook signature using Stripe-Signature header
         * and processes the event.
         */
        post("/webhook") {
            val payload = call.receiveText()
            val sigHeader = call.request.header("Stripe-Signature")

            if (sigHeader == null) {
                logger.warn { "Webhook request missing Stripe-Signature header" }
                return@post call.respond(HttpStatusCode.BadRequest, "Missing signature")
            }

            try {
                // Verify webhook signature
                val event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    AppConfig.stripeWebhookSecret
                )

                // Process event
                billingService.processWebhookEvent(event)

                call.respond(HttpStatusCode.OK, mapOf("received" to true))
            } catch (e: com.stripe.exception.SignatureVerificationException) {
                logger.error(e) { "Invalid webhook signature" }
                call.respond(HttpStatusCode.BadRequest, "Invalid signature")
            } catch (e: Exception) {
                logger.error(e) { "Failed to process webhook" }
                call.respond(HttpStatusCode.InternalServerError, "Webhook processing failed")
            }
        }
    }
}
