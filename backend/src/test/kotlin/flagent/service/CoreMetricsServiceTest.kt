package flagent.service

import flagent.repository.impl.EvaluationEventRepository
import flagent.repository.impl.FlagEvaluationStatsResult
import flagent.repository.impl.FlagUsageByClientResult
import flagent.repository.impl.MetricsOverviewResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreMetricsServiceTest {

    @Test
    fun getOverview_returnsFromRepo() = runBlocking {
        val repo = mockk<EvaluationEventRepository>(relaxed = true)
        val result = MetricsOverviewResult(0L, 0, emptyList(), emptyList())
        coEvery { repo.getOverview(any(), any(), any(), any()) } returns result
        val service = CoreMetricsService(repo)
        assertEquals(result, service.getOverview(0L, 1000L, 10, 3600_000L))
    }

    @Test
    fun getFlagStats_returnsFromRepo() = runBlocking {
        val repo = mockk<EvaluationEventRepository>(relaxed = true)
        val result = FlagEvaluationStatsResult(1, 0L, emptyList())
        coEvery { repo.getStatsForFlag(any(), any(), any(), any()) } returns result
        val service = CoreMetricsService(repo)
        assertEquals(result, service.getFlagStats(1, 0L, 1000L))
    }

    @Test
    fun getFlagUsage_returnsFromRepo() = runBlocking {
        val repo = mockk<EvaluationEventRepository>(relaxed = true)
        val result = FlagUsageByClientResult(1, 0L, 1000L, 0L, emptyList())
        coEvery { repo.getUsageByClient(any(), any(), any()) } returns result
        val service = CoreMetricsService(repo)
        assertEquals(result, service.getFlagUsage(1, 0L, 1000L))
    }
}
