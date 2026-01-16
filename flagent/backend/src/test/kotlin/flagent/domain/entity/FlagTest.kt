package flagent.domain.entity

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlagTest {
    @Test
    fun `prepareEvaluation creates FlagEvaluation with variants map`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            variants = listOf(variant1, variant2)
        )
        
        val evaluation = flag.prepareEvaluation()
        
        assertNotNull(evaluation)
        assertEquals(2, evaluation.variantsMap.size)
        assertEquals(variant1, evaluation.variantsMap[1])
        assertEquals(variant2, evaluation.variantsMap[2])
    }
    
    @Test
    fun `prepareEvaluation with empty variants creates empty map`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            variants = emptyList()
        )
        
        val evaluation = flag.prepareEvaluation()
        
        assertNotNull(evaluation)
        assertTrue(evaluation.variantsMap.isEmpty())
    }
}
