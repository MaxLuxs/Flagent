package flagent.recorder.impl

import flagent.recorder.DataRecordFrame
import flagent.service.EvalContext
import flagent.service.EvalResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

class NoopRecorderTest {
    @Test
    fun testRecord() = runBlocking {
        val recorder = NoopRecorder()
        val result = createTestEvalResult()
        
        // Should not throw
        recorder.record(result)
    }
    
    @Test
    fun testRecordBatch() = runBlocking {
        val recorder = NoopRecorder()
        val results = listOf(createTestEvalResult(), createTestEvalResult())
        
        // Should not throw
        recorder.recordBatch(results)
    }
    
    @Test
    fun testNewDataRecordFrame() {
        val recorder = NoopRecorder()
        val result = createTestEvalResult()
        
        val frame = recorder.newDataRecordFrame(result)
        assertNotNull(frame)
    }
    
    private fun createTestEvalResult() = EvalResult(
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
}
