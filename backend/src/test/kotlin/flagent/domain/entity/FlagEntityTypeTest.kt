package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class FlagEntityTypeTest {
    @Test
    fun `create FlagEntityType with all fields`() {
        val entityType = FlagEntityType(
            id = 1,
            key = "user"
        )
        
        assertEquals(1, entityType.id)
        assertEquals("user", entityType.key)
    }
    
    @Test
    fun `create FlagEntityType with default id`() {
        val entityType = FlagEntityType(key = "session")
        
        assertEquals(0, entityType.id)
        assertEquals("session", entityType.key)
    }
    
    @Test
    fun `FlagEntityType data class equality`() {
        val type1 = FlagEntityType(id = 1, key = "user")
        val type2 = FlagEntityType(id = 1, key = "user")
        
        assertEquals(type1, type2)
        assertEquals(type1.hashCode(), type2.hashCode())
    }
    
    @Test
    fun `FlagEntityType with different keys are not equal`() {
        val type1 = FlagEntityType(id = 1, key = "user")
        val type2 = FlagEntityType(id = 1, key = "session")
        
        assert(!type1.equals(type2)) { "Types with different keys should not be equal" }
    }
}
