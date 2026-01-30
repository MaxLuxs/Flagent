package flagent.evaluator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VariantSelectorTest {

    private val selector = VariantSelector()

    @Test
    fun selectVariant_returnsVariantId_whenSingleDistribution100Percent() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(
                FlagEvaluator.EvaluableDistribution(id = 1, variantId = 10, percent = 100)
            )
        )
        val result = selector.selectVariant(segment, "user123", 1)
        assertNotNull(result)
        assertEquals(10, result)
    }

    @Test
    fun selectVariant_returnsNull_whenEmptyDistributions() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = emptyList()
        )
        val result = selector.selectVariant(segment, "user123", 1)
        assertNull(result)
    }

    @Test
    fun selectVariant_returnsNull_whenRolloutPercentZero() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(
                FlagEvaluator.EvaluableDistribution(id = 1, variantId = 10, percent = 100)
            )
        )
        val result = selector.selectVariant(segment, "user123", 1)
        assertNull(result)
    }

    @Test
    fun selectVariant_returnsNull_whenEmptyEntityID() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(
                FlagEvaluator.EvaluableDistribution(id = 1, variantId = 10, percent = 100)
            )
        )
        val result = selector.selectVariant(segment, "", 1)
        assertNull(result)
    }

    @Test
    fun selectVariant_isDeterministic_sameEntityAndFlagReturnsSameVariant() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(
                FlagEvaluator.EvaluableDistribution(id = 1, variantId = 10, percent = 100)
            )
        )
        val r1 = selector.selectVariant(segment, "user456", 1)
        val r2 = selector.selectVariant(segment, "user456", 1)
        assertEquals(r1, r2)
    }

    @Test
    fun selectVariant_withMultipleDistributions_returnsOneVariant() {
        val segment = FlagEvaluator.EvaluableSegment(
            id = 1,
            rank = 0,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(
                FlagEvaluator.EvaluableDistribution(id = 1, variantId = 1, percent = 50),
                FlagEvaluator.EvaluableDistribution(id = 2, variantId = 2, percent = 50)
            )
        )
        val result = selector.selectVariant(segment, "user789", 1)
        assertNotNull(result)
        assertTrue(result == 1 || result == 2)
    }
}
