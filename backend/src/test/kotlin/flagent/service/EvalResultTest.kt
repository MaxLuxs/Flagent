package flagent.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.*

class EvalResultTest {
    @Test
    fun `create EvalResult with all fields`() {
        val entityContext = buildJsonObject {
            put("region", "US")
            put("tier", "premium")
        }
        
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 5,
            flagTags = listOf("production", "feature"),
            segmentID = 10,
            variantID = 20,
            variantKey = "control",
            variantAttachment = null,
            evalContext = EvalContext(
                entityID = "user123",
                entityType = "user",
                entityContext = entityContext
            ),
            evalDebugLog = null,
            timestamp = 1234567890L
        )
        
        assertEquals(1, result.flagID)
        assertEquals("test_flag", result.flagKey)
        assertEquals(5, result.flagSnapshotID)
        assertEquals(2, result.flagTags.size)
        assertEquals(10, result.segmentID)
        assertEquals(20, result.variantID)
        assertEquals("control", result.variantKey)
        assertEquals("user123", result.evalContext.entityID)
        assertEquals("user", result.evalContext.entityType)
        assertEquals(1234567890L, result.timestamp)
    }
    
    @Test
    fun `create EvalResult with default values`() {
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            evalContext = EvalContext(),
            timestamp = 0L
        )
        
        assertNull(result.segmentID)
        assertNull(result.variantID)
        assertNull(result.variantKey)
        assertNull(result.variantAttachment)
        assertNull(result.evalDebugLog)
        assertNull(result.evalContext.entityID)
        assertNull(result.evalContext.entityType)
        assertNull(result.evalContext.entityContext)
    }
    
    @Test
    fun `create EvalResult with variant attachment`() {
        val attachment = buildJsonObject {
            put("color", "red")
            put("enabled", true)
        }
        
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            variantID = 1,
            variantKey = "treatment",
            variantAttachment = attachment,
            evalContext = EvalContext(),
            timestamp = 0L
        )
        
        assertEquals(attachment, result.variantAttachment)
        assertEquals("red", result.variantAttachment?.get("color")?.jsonPrimitive?.content)
    }
    
    @Test
    fun `EvalResult data class equality`() {
        val result1 = EvalResult(
            flagID = 1,
            flagKey = "test",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            evalContext = EvalContext(entityID = "user1"),
            timestamp = 1000L
        )
        val result2 = EvalResult(
            flagID = 1,
            flagKey = "test",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            evalContext = EvalContext(entityID = "user1"),
            timestamp = 1000L
        )
        
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }
}
