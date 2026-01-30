package flagent.api

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for CoreSegmentService API contract and SegmentInfo.
 * Verifies that backend adapters can delegate to this interface correctly.
 */
class CoreSegmentServiceTest {

    @Test
    fun `SegmentInfo holds id and rolloutPercent`() {
        val info = SegmentInfo(id = 5, rolloutPercent = 75)
        assertEquals(5, info.id)
        assertEquals(75, info.rolloutPercent)
    }

    @Test
    fun `SegmentInfo copy works`() {
        val info = SegmentInfo(id = 3, rolloutPercent = 0)
        val updated = info.copy(rolloutPercent = 100)
        assertEquals(3, updated.id)
        assertEquals(100, updated.rolloutPercent)
    }

    @Test
    fun `getSegment delegates and returns SegmentInfo`() = runTest {
        val service = mockk<CoreSegmentService>()
        val expected = SegmentInfo(id = 7, rolloutPercent = 50)
        coEvery { service.getSegment(7) } returns expected

        val result = service.getSegment(7)

        assertEquals(expected, result)
        coVerify(exactly = 1) { service.getSegment(7) }
    }

    @Test
    fun `getSegment returns null when not found`() = runTest {
        val service = mockk<CoreSegmentService>()
        coEvery { service.getSegment(999) } returns null

        val result = service.getSegment(999)

        assertNull(result)
        coVerify(exactly = 1) { service.getSegment(999) }
    }

    @Test
    fun `updateSegmentRollout delegates to implementation`() = runTest {
        val service = mockk<CoreSegmentService>()
        coEvery { service.updateSegmentRollout(1, 25) } returns Unit

        service.updateSegmentRollout(1, 25)

        coVerify(exactly = 1) { service.updateSegmentRollout(1, 25) }
    }
}
