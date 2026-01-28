package flagent.domain.repository

import flagent.domain.entity.UsageRecord
import flagent.domain.entity.UsageMetricType
import java.time.LocalDateTime

/**
 * Repository interface for usage record operations.
 */
interface IUsageRecordRepository {
    /**
     * Create a new usage record.
     */
    suspend fun create(record: UsageRecord): UsageRecord

    /**
     * Update existing usage record.
     */
    suspend fun update(record: UsageRecord): UsageRecord

    /**
     * Find usage record by ID.
     */
    suspend fun findById(id: Long): UsageRecord?

    /**
     * Find all usage records for tenant in period.
     */
    suspend fun findByTenantIdAndPeriod(
        tenantId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<UsageRecord>

    /**
     * Find unreported usage records for tenant.
     */
    suspend fun findUnreportedByTenantId(tenantId: Long): List<UsageRecord>

    /**
     * Get aggregated usage for tenant in period.
     */
    suspend fun getAggregatedUsage(
        tenantId: Long,
        metricType: UsageMetricType,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Long

    /**
     * Mark usage records as reported.
     */
    suspend fun markAsReported(ids: List<Long>, stripeUsageRecordId: String): Int
}
