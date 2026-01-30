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

    @Test
    fun testOutput_payloadRawJsonMode_containsPayloadKey() {
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
                entityID = "e1",
                entityType = "user",
                entityContext = null
            ),
            evalDebugLog = null,
            timestamp = 12345L
        )
        val frame = DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions(frameOutputMode = DataRecordFrame.FRAME_OUTPUT_MODE_PAYLOAD_RAW_JSON)
        )
        val output = frame.output()
        val json = String(output, Charsets.UTF_8)
        assert(json.contains("\"payload\""))
    }

    @Test
    fun testOutput_encryptedMode_producesBase64Payload() {
        val result = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            segmentID = 1,
            variantID = 1,
            variantKey = "control",
            variantAttachment = null,
            evalContext = EvalContext(entityID = "e1", entityType = "user", entityContext = null),
            evalDebugLog = null,
            timestamp = 12345L
        )
        val key = "a".repeat(32)
        val frame = DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions(encrypted = true, encryptionKey = key)
        )
        val output = frame.output()
        val json = String(output, Charsets.UTF_8)
        assert(json.contains("\"encrypted\":true"))
    }
}
