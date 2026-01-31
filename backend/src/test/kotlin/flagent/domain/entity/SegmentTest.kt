package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for Segment domain entity.
 * Evaluation logic (prepareEvaluation, DistributionArray, rollout) moved to shared evaluator.
 */
class SegmentTest {
    @Test
    fun `Segment with distributions contains correct data`() {
        val distribution1 = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 50
        )
        val distribution2 = Distribution(
            id = 2,
            segmentId = 1,
            variantId = 2,
            percent = 50
        )

        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = listOf(distribution1, distribution2)
        )

        assertNotNull(segment)
        assertEquals(1, segment.id)
        assertEquals(1, segment.flagId)
        assertEquals(100, segment.rolloutPercent)
        assertEquals(2, segment.distributions.size)
        assertEquals(listOf(1, 2), segment.distributions.map { it.variantId })
    }

    @Test
    fun `Segment with empty distributions`() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = emptyList()
        )

        assertNotNull(segment)
        assert(segment.distributions.isEmpty())
    }

    @Test
    fun `Segment copy preserves distributions`() {
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        val segment = Segment(
            id = 1,
            flagId = 1,
            rank = 1,
            rolloutPercent = 50,
            distributions = listOf(distribution)
        )
        val copied = segment.copy(rolloutPercent = 75)

        assertEquals(75, copied.rolloutPercent)
        assertEquals(1, copied.distributions.size)
        assertEquals(1, copied.distributions[0].variantId)
    }
}
