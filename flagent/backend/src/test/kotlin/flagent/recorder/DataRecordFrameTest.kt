package flagent.recorder

import flagent.service.EvalContext
import flagent.service.EvalResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DataRecordFrameTest {
    @Test
    fun testGetPartitionKey() {
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            segmentID = 1,
            variantID = 1,
            variantKey = "control",
            variantAttachment = null,
            evalContext = EvalContext(
                entityID = "test_entity",
                entityType = "user",
                entityContext = null
            ),
            evalDebugLog = null,
            timestamp = System.currentTimeMillis()
        )
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions()
        )
        
        assertEquals("test_entity", frame.getPartitionKey())
    }
    
    @Test
    fun testGetPartitionKeyEmpty() {
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            segmentID = 1,
            variantID = 1,
            variantKey = "control",
            variantAttachment = null,
            evalContext = EvalContext(
                entityID = null,
                entityType = "user",
                entityContext = null
            ),
            evalDebugLog = null,
            timestamp = System.currentTimeMillis()
        )
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions()
        )
        
        assertEquals("", frame.getPartitionKey())
    }
    
    @Test
    fun testOutput() {
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            segmentID = 1,
            variantID = 1,
            variantKey = "control",
            variantAttachment = null,
            evalContext = EvalContext(
                entityID = "test_entity",
                entityType = "user",
                entityContext = null
            ),
            evalDebugLog = null,
            timestamp = System.currentTimeMillis()
        )
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions()
        )
        
        val output = frame.output()
        assertNotNull(output)
        assert(output.isNotEmpty())
    }
}
