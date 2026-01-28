package flagent.domain.entity

import java.time.LocalDateTime

/**
 * Usage record - tracks metered billing usage (e.g., evaluations).
 */
data class UsageRecord(
    val id: Long = 0,
    val tenantId: Long,
    val subscriptionId: Long,
    val metricType: UsageMetricType,
    val quantity: Long,
    val timestamp: LocalDateTime,
    val reportedToStripe: Boolean = false,
    val stripeUsageRecordId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Usage metric types for metered billing.
 */
enum class UsageMetricType {
    /**
     * Flag evaluations count.
     */
    EVALUATIONS,

    /**
     * API requests count.
     */
    API_REQUESTS,

    /**
     * Active flags count.
     */
    ACTIVE_FLAGS,

    /**
     * Active environments count.
     */
    ACTIVE_ENVIRONMENTS
}
