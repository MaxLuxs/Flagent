package flagent.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * TenantUsage entity - tracks usage for billing.
 *
 * Metered usage tracking:
 * - Evaluations count (primary billing metric)
 * - Flags count (for limits enforcement)
 * - API calls count (for rate limiting)
 *
 * Period-based tracking (daily aggregation).
 */
data class TenantUsage(
    val id: Long = 0,
    val tenantId: Long,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val evaluationsCount: Long = 0,
    val flagsCount: Int = 0,
    val apiCallsCount: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Helper to check if usage exceeds plan limits.
 */
fun TenantUsage.exceedsLimit(plan: TenantPlan): Boolean {
    val limit = when (plan) {
        TenantPlan.STARTER -> 100_000L
        TenantPlan.GROWTH -> 1_000_000L
        TenantPlan.SCALE -> 10_000_000L
        TenantPlan.ENTERPRISE -> Long.MAX_VALUE
    }
    
    return evaluationsCount > limit
}
