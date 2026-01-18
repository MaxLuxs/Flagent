package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.*

class VariantTest {
    @Test
    fun `create Variant with all fields`() {
        val variant = Variant(
            id = 1,
            flagId = 10,
            key = "control",
            attachment = null
        )
        
        assertEquals(1, variant.id)
        assertEquals(10, variant.flagId)
        assertEquals("control", variant.key)
        assertNull(variant.attachment)
    }
    
    @Test
    fun `create Variant with default values`() {
        val variant = Variant(
            flagId = 10,
            key = "treatment"
        )
        
        assertEquals(0, variant.id)
        assertEquals("treatment", variant.key)
        assertNull(variant.attachment)
    }
    
    @Test
    fun `create Variant with attachment`() {
        val attachment = buildJsonObject {
            put("color", "red")
            put("size", 42)
            put("enabled", true)
        }
        
        val variant = Variant(
            id = 1,
            flagId = 10,
            key = "treatment",
            attachment = attachment
        )
        
        assertEquals(attachment, variant.attachment)
        assertEquals("red", variant.attachment?.get("color")?.jsonPrimitive?.content)
        assertEquals(42, variant.attachment?.get("size")?.jsonPrimitive?.int)
        assertEquals(true, variant.attachment?.get("enabled")?.jsonPrimitive?.boolean)
    }
    
    @Test
    fun `Variant data class equality`() {
        val variant1 = Variant(id = 1, flagId = 10, key = "control")
        val variant2 = Variant(id = 1, flagId = 10, key = "control")
        
        assertEquals(variant1, variant2)
        assertEquals(variant1.hashCode(), variant2.hashCode())
    }
}
