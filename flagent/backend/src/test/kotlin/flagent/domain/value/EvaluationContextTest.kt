package flagent.domain.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationContextTest {
    @Test
    fun `getProperty returns value from entity context`() {
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = mapOf("age" to 25, "country" to "US")
        )
        
        assertEquals(25, context.getProperty("age"))
        assertEquals("US", context.getProperty("country"))
    }
    
    @Test
    fun `getProperty returns null for missing property`() {
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25)
        )
        
        assertNull(context.getProperty("country"))
    }
    
    @Test
    fun `hasProperty returns true for existing property`() {
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25)
        )
        
        assertTrue(context.hasProperty("age"))
    }
    
    @Test
    fun `hasProperty returns false for missing property`() {
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25)
        )
        
        assertFalse(context.hasProperty("country"))
    }
    
    @Test
    fun `hasProperty returns false when entity context is null`() {
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        assertFalse(context.hasProperty("age"))
    }
}
