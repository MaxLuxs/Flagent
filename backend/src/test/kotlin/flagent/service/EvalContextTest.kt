package flagent.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.*

class EvalContextTest {
    @Test
    fun `create EvalContext with all fields`() {
        val entityContext = buildJsonObject {
            put("country", "US")
            put("age", 25)
        }
        
        val context = EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = entityContext
        )
        
        assertEquals("user123", context.entityID)
        assertEquals("user", context.entityType)
        assertEquals(entityContext, context.entityContext)
    }
    
    @Test
    fun `create EvalContext with default values`() {
        val context = EvalContext()
        
        assertNull(context.entityID)
        assertNull(context.entityType)
        assertNull(context.entityContext)
    }
    
    @Test
    fun `create EvalContext with only entityID`() {
        val context = EvalContext(entityID = "user123")
        
        assertEquals("user123", context.entityID)
        assertNull(context.entityType)
        assertNull(context.entityContext)
    }
    
    @Test
    fun `EvalContext data class equality`() {
        val context1 = EvalContext(
            entityID = "user1",
            entityType = "user"
        )
        val context2 = EvalContext(
            entityID = "user1",
            entityType = "user"
        )
        
        assertEquals(context1, context2)
        assertEquals(context1.hashCode(), context2.hashCode())
    }
    
    @Test
    fun `EvalContext with different entityContext are not equal`() {
        val context1 = EvalContext(
            entityID = "user1",
            entityContext = buildJsonObject { put("region", "US") }
        )
        val context2 = EvalContext(
            entityID = "user1",
            entityContext = buildJsonObject { put("region", "EU") }
        )
        
        assert(!context1.equals(context2)) { "Contexts with different entityContext should not be equal" }
    }
}
