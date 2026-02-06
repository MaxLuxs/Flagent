package flagent.service

import flagent.repository.impl.AnalyticsEventRecord
import flagent.repository.impl.AnalyticsEventRepository
import flagent.repository.impl.AnalyticsOverviewResult

/**
 * Service for analytics events (Firebase-level: first_open, session_start, screen_view, custom).
 */
class AnalyticsEventsService(
    private val repository: AnalyticsEventRepository
) {
    suspend fun collectEvents(events: List<AnalyticsEventRecord>, tenantId: String? = null) {
        repository.saveBatch(events, tenantId)
    }

    suspend fun getOverview(
        startMs: Long,
        endMs: Long,
        topLimit: Int = 20,
        timeBucketMs: Long = 3600_000,
        tenantId: String? = null
    ): AnalyticsOverviewResult =
        repository.getOverview(startMs, endMs, topLimit, timeBucketMs, tenantId)
}
