package flagent.service

import flagent.repository.impl.EvaluationEventRepository
import flagent.repository.impl.FlagEvaluationStatsResult
import flagent.repository.impl.MetricsOverviewResult
import flagent.repository.impl.TopFlagEntry
import flagent.repository.impl.TimeSeriesEntry

/**
 * Core metrics service - evaluation count overview (OSS).
 */
class CoreMetricsService(
    private val evaluationEventRepository: EvaluationEventRepository
) {
    suspend fun getOverview(
        startMs: Long,
        endMs: Long,
        topLimit: Int = 10,
        timeBucketMs: Long = 3600_000
    ): MetricsOverviewResult =
        evaluationEventRepository.getOverview(startMs, endMs, topLimit, timeBucketMs)

    suspend fun getFlagStats(
        flagId: Int,
        startMs: Long,
        endMs: Long,
        timeBucketMs: Long = 3600_000
    ): FlagEvaluationStatsResult =
        evaluationEventRepository.getStatsForFlag(flagId, startMs, endMs, timeBucketMs)
}
