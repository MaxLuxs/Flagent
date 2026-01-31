package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for Flag domain entity.
 * Evaluation logic (prepareEvaluation, FlagEvaluation) moved to shared evaluator.
 */
class FlagTest {
    @Test
    fun `Flag with variants contains correct data`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")

        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            variants = listOf(variant1, variant2)
        )

        assertNotNull(flag)
        assertEquals(1, flag.id)
        assertEquals("test_flag", flag.key)
        assertEquals(2, flag.variants.size)
        assertEquals(variant1, flag.variants[0])
        assertEquals(variant2, flag.variants[1])
    }

    @Test
    fun `Flag with empty variants`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            variants = emptyList()
        )

        assertNotNull(flag)
        assert(flag.variants.isEmpty())
    }
}
