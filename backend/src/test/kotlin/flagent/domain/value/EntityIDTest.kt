package flagent.domain.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityIDTest {
    @Test
    fun `create EntityID with valid value`() {
        val entityID = EntityID("user123")
        assertEquals("user123", entityID.value)
        assertEquals("user123", entityID.toString())
    }
    
    @Test
    fun `create EntityID with long value`() {
        val longValue = "user".repeat(100)
        val entityID = EntityID(longValue)
        assertEquals(longValue, entityID.value)
    }
    
    @Test
    fun `create EntityID with special characters`() {
        val specialValue = "user-123_456@domain.com"
        val entityID = EntityID(specialValue)
        assertEquals(specialValue, entityID.value)
    }
    
    @Test
    fun `EntityID throws exception for blank string`() {
        assertFailsWith<IllegalArgumentException> {
            EntityID("")
        }
    }
    
    @Test
    fun `EntityID throws exception for whitespace only`() {
        assertFailsWith<IllegalArgumentException> {
            EntityID("   ")
        }
    }
    
    @Test
    fun `EntityID value class preserves equality`() {
        val id1 = EntityID("user123")
        val id2 = EntityID("user123")
        assertEquals(id1.value, id2.value)
        assertEquals(id1.toString(), id2.toString())
    }
}
