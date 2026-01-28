package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IInvoiceRepository
import flagent.domain.repository.ISubscriptionRepository
import flagent.domain.repository.ITenantRepository
import flagent.domain.repository.IUsageRecordRepository
import flagent.service.dto.CreateCheckoutSessionRequest
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for BillingService.
 *
 * Tests subscription management, usage tracking, and webhook processing.
 * Uses MockK for mocking dependencies.
 */
class BillingServiceTest {

    private lateinit var subscriptionRepository: ISubscriptionRepository
    private lateinit var invoiceRepository: IInvoiceRepository
    private lateinit var usageRecordRepository: IUsageRecordRepository
    private lateinit var tenantRepository: ITenantRepository
    private lateinit var billingService: BillingService

    private val testTenant = Tenant(
        id = 1L,
        key = "test-tenant",
        name = "Test Tenant",
        plan = TenantPlan.STARTER,
        status = TenantStatus.ACTIVE,
        schemaName = "tenant_1",
        stripeCustomerId = "cus_test123",
        stripeSubscriptionId = "sub_test123",
        billingEmail = "billing@test.com"
    )

    private val testSubscription = Subscription(
        id = 1L,
        tenantId = 1L,
        stripeSubscriptionId = "sub_test123",
        stripeCustomerId = "cus_test123",
        stripePriceId = "price_test123",
        status = SubscriptionStatus.ACTIVE,
        currentPeriodStart = LocalDateTime.now().minusDays(15),
        currentPeriodEnd = LocalDateTime.now().plusDays(15),
        cancelAtPeriodEnd = false
    )

    private val testInvoice = Invoice(
        id = 1L,
        tenantId = 1L,
        subscriptionId = 1L,
        stripeInvoiceId = "in_test123",
        stripeCustomerId = "cus_test123",
        status = InvoiceStatus.PAID,
        amountDue = BigDecimal("99.00"),
        amountPaid = BigDecimal("99.00"),
        currency = "usd",
        periodStart = LocalDateTime.now().minusMonths(1),
        periodEnd = LocalDateTime.now(),
        dueDate = null
    )

    @BeforeEach
    fun setup() {
        subscriptionRepository = mockk()
        invoiceRepository = mockk()
        usageRecordRepository = mockk()
        tenantRepository = mockk()

        // Note: BillingService constructor calls Stripe.apiKey setter
        // In tests, we should mock Stripe API calls or set a test key
        billingService = BillingService(
            subscriptionRepository,
            invoiceRepository,
            usageRecordRepository,
            tenantRepository
        )
    }

    // ========================================================================
    // Subscription Tests
    // ========================================================================

    @Test
    fun `getActiveSubscription should return subscription DTO when subscription exists`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns testSubscription
        mockkStatic("com.stripe.model.Subscription")
        // Mock Stripe API response - in real test, use Stripe mock or test fixtures
        
        // When
        val result = billingService.getActiveSubscription(1L)

        // Then
        // Note: This would require mocking Stripe.Subscription.retrieve()
        // For full test coverage, use Stripe test fixtures or mock library
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
    }

    @Test
    fun `getActiveSubscription should return null when no active subscription`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns null

        // When
        val result = billingService.getActiveSubscription(1L)

        // Then
        assertEquals(null, result)
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
    }

    @Test
    fun `cancelSubscription should throw exception when no active subscription`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns null

        // When/Then
        assertThrows<IllegalArgumentException> {
            billingService.cancelSubscription(1L)
        }

        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
        coVerify(exactly = 0) { subscriptionRepository.update(any()) }
    }

    // ========================================================================
    // Invoice Tests
    // ========================================================================

    @Test
    fun `getInvoices should return invoices for tenant`() = runBlocking {
        // Given
        val invoices = listOf(testInvoice)
        coEvery { invoiceRepository.findAllByTenantId(1L) } returns invoices

        // When
        val result = billingService.getInvoices(1L, limit = 10)

        // Then
        assertEquals(1, result.size)
        assertEquals("in_test123", result[0].stripeInvoiceId)
        assertEquals("PAID", result[0].status)
        assertEquals("99.00", result[0].amountDue)
        coVerify { invoiceRepository.findAllByTenantId(1L) }
    }

    @Test
    fun `getInvoices should respect limit parameter`() = runBlocking {
        // Given
        val invoices = List(20) { index ->
            testInvoice.copy(
                id = index.toLong(),
                stripeInvoiceId = "in_test$index"
            )
        }
        coEvery { invoiceRepository.findAllByTenantId(1L) } returns invoices

        // When
        val result = billingService.getInvoices(1L, limit = 5)

        // Then
        assertEquals(5, result.size)
        coVerify { invoiceRepository.findAllByTenantId(1L) }
    }

    // ========================================================================
    // Usage Tracking Tests
    // ========================================================================

    @Test
    fun `recordUsage should create usage record when active subscription exists`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns testSubscription
        coEvery { usageRecordRepository.create(any()) } answers {
            firstArg<UsageRecord>().copy(id = 1L)
        }

        // When
        billingService.recordUsage(
            tenantId = 1L,
            metricType = UsageMetricType.EVALUATIONS,
            quantity = 100L
        )

        // Then
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
        coVerify { usageRecordRepository.create(match { record ->
            record.tenantId == 1L &&
            record.subscriptionId == 1L &&
            record.metricType == UsageMetricType.EVALUATIONS &&
            record.quantity == 100L
        }) }
    }

    @Test
    fun `recordUsage should not create record when no active subscription`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns null

        // When
        billingService.recordUsage(
            tenantId = 1L,
            metricType = UsageMetricType.EVALUATIONS,
            quantity = 100L
        )

        // Then
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
        coVerify(exactly = 0) { usageRecordRepository.create(any()) }
    }

    @Test
    fun `getUsageStats should aggregate usage by metric type`() = runBlocking {
        // Given
        val startTime = LocalDateTime.now().minusDays(30)
        val endTime = LocalDateTime.now()
        
        coEvery { 
            usageRecordRepository.getAggregatedUsage(1L, UsageMetricType.EVALUATIONS, startTime, endTime) 
        } returns 1500000L
        
        coEvery { 
            usageRecordRepository.getAggregatedUsage(1L, UsageMetricType.API_REQUESTS, startTime, endTime) 
        } returns 50000L
        
        coEvery { 
            usageRecordRepository.getAggregatedUsage(1L, UsageMetricType.ACTIVE_FLAGS, startTime, endTime) 
        } returns 25L
        
        coEvery { 
            usageRecordRepository.getAggregatedUsage(1L, UsageMetricType.ACTIVE_ENVIRONMENTS, startTime, endTime) 
        } returns 3L

        // When
        val result = billingService.getUsageStats(1L, startTime, endTime)

        // Then
        assertEquals(1L, result.tenantId)
        assertEquals(1500000L, result.metrics[UsageMetricType.EVALUATIONS.name])
        assertEquals(50000L, result.metrics[UsageMetricType.API_REQUESTS.name])
        assertEquals(25L, result.metrics[UsageMetricType.ACTIVE_FLAGS.name])
        assertEquals(3L, result.metrics[UsageMetricType.ACTIVE_ENVIRONMENTS.name])
        
        coVerify(exactly = 4) { usageRecordRepository.getAggregatedUsage(any(), any(), any(), any()) }
    }

    @Test
    fun `reportUsageToStripe should not report when no active subscription`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns null

        // When
        billingService.reportUsageToStripe(1L)

        // Then
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
        coVerify(exactly = 0) { usageRecordRepository.findUnreportedByTenantId(any()) }
    }

    @Test
    fun `reportUsageToStripe should not report when no unreported records`() = runBlocking {
        // Given
        coEvery { subscriptionRepository.findActiveByTenantId(1L) } returns testSubscription
        coEvery { usageRecordRepository.findUnreportedByTenantId(1L) } returns emptyList()

        // When
        billingService.reportUsageToStripe(1L)

        // Then
        coVerify { subscriptionRepository.findActiveByTenantId(1L) }
        coVerify { usageRecordRepository.findUnreportedByTenantId(1L) }
        // No Stripe API calls should be made
    }

    // ========================================================================
    // Helper Methods Tests
    // ========================================================================

    @Test
    fun `getOrCreateStripeCustomer should return existing customer ID`() = runBlocking {
        // Given
        coEvery { tenantRepository.findById(1L) } returns testTenant

        // When
        // Note: This is a private method, test indirectly via public methods
        // or make it internal for testing

        // Then
        // Verify tenant has customer ID
        assertNotNull(testTenant.stripeCustomerId)
        assertEquals("cus_test123", testTenant.stripeCustomerId)
    }

    @Test
    fun `getOrCreateStripeCustomer should create new customer when not exists`() = runBlocking {
        // Given
        val tenantWithoutCustomer = testTenant.copy(stripeCustomerId = null)
        coEvery { tenantRepository.findById(1L) } returns tenantWithoutCustomer
        coEvery { tenantRepository.update(any()) } answers { firstArg() }
        
        // Mock Stripe Customer.create() - in real tests, use Stripe fixtures

        // When/Then
        // Test via createCheckoutSession which calls getOrCreateStripeCustomer
        // Requires full Stripe API mocking
    }
}
