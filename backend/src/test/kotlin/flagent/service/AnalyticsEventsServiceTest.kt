package flagent.service

import flagent.repository.impl.AnalyticsEventRecord
import flagent.repository.impl.AnalyticsEventRepository
import flagent.repository.impl.AnalyticsOverviewResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyticsEventsServiceTest {

    @Test
    fun collectEvents_callsSaveBatch() = runBlocking {
        val repo = mockk<AnalyticsEventRepository>(relaxed = true)
        coEvery { repo.saveBatch(any(), any()) } returns Unit
        val service = AnalyticsEventsService(repo)
        val events = listOf(
            AnalyticsEventRecord(eventName = "first_open", eventParams = "{}", timestampMs = 1L)
        )
        service.collectEvents(events, null)
        coVerify(exactly = 1) { repo.saveBatch(events, null) }
    }

    @Test
    fun getOverview_returnsFromRepo() = runBlocking {
        val repo = mockk<AnalyticsEventRepository>(relaxed = true)
        val result = AnalyticsOverviewResult(0L, 0, emptyList(), emptyList(), emptyList())
        coEvery { repo.getOverview(any(), any(), any(), any(), any()) } returns result
        val service = AnalyticsEventsService(repo)
        assertEquals(result, service.getOverview(0L, 1000L, 20, 3600_000L, null))
    }
}
