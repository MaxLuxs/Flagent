package flagent.application

import flagent.domain.entity.Segment
import flagent.service.SegmentService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class CoreSegmentServiceAdapterTest {

    @Test
    fun getSegmentReturnsSegmentInfoWhenFound() = runTest {
        val segmentService = mockk<SegmentService>()
        val segment = Segment(
            id = 1,
            flagId = 10,
            rank = 1,
            rolloutPercent = 50,
            constraints = emptyList(),
            distributions = emptyList()
        )
        coEvery { segmentService.getSegment(1) } returns segment
        val adapter = CoreSegmentServiceAdapter(segmentService)

        val result = adapter.getSegment(1)

        assertEquals(flagent.api.SegmentInfo(id = 1, rolloutPercent = 50), result)
        coVerify { segmentService.getSegment(1) }
    }

    @Test
    fun getSegmentReturnsNullWhenNotFound() = runTest {
        val segmentService = mockk<SegmentService>()
        coEvery { segmentService.getSegment(999) } returns null
        val adapter = CoreSegmentServiceAdapter(segmentService)

        val result = adapter.getSegment(999)

        assertNull(result)
    }

    @Test
    fun updateSegmentRolloutCallsServiceWhenSegmentExists() = runTest {
        val segmentService = mockk<SegmentService>()
        val segment = Segment(
            id = 2,
            flagId = 10,
            rank = 1,
            rolloutPercent = 25,
            constraints = emptyList(),
            distributions = emptyList()
        )
        coEvery { segmentService.getSegment(2) } returns segment
        coEvery { segmentService.updateSegment(2, any(), any()) } returns segment.copy(rolloutPercent = 75)
        val adapter = CoreSegmentServiceAdapter(segmentService)

        adapter.updateSegmentRollout(2, 75)

        coVerify { segmentService.getSegment(2) }
        coVerify { segmentService.updateSegment(2, match { it.rolloutPercent == 75 }, null) }
    }

    @Test
    fun updateSegmentRolloutDoesNothingWhenSegmentNotFound() = runTest {
        val segmentService = mockk<SegmentService>()
        coEvery { segmentService.getSegment(99) } returns null
        val adapter = CoreSegmentServiceAdapter(segmentService)

        adapter.updateSegmentRollout(99, 100)

        coVerify(exactly = 1) { segmentService.getSegment(99) }
        coVerify(exactly = 0) { segmentService.updateSegment(any(), any(), any()) }
    }
}
