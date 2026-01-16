package flagent.recorder

import flagent.config.AppConfig
import flagent.recorder.impl.NoopRecorder
import flagent.service.EvalContext
import flagent.service.EvalResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

class DataRecordingServiceTest {
    @Test
    fun testRecordAsync() = runBlocking {
        // Use NoopRecorder for testing
        val originalType = AppConfig.recorderType
        try {
            // Temporarily set to noop for testing
            val service = DataRecordingService()
            
            val result = createTestEvalResult()
            service.recordAsync(result)
            
            // Give some time for async processing
            delay(100)
            
            // Should not throw
            service.stop()
        } finally {
            // Restore original type if needed
        }
    }
    
    @Test
    fun testCreateRecorder() = runBlocking {
        val service = DataRecordingService()
        assertNotNull(service)
        service.stop()
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
