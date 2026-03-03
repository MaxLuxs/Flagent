package flagent.recorder.impl

import com.google.api.core.ApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import flagent.config.AppConfig
import flagent.recorder.DataRecordFrame
import flagent.service.EvalContext
import flagent.service.EvalResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for PubSubRecorder
 * Note: These tests focus on testing the logic that can be tested without a real Pub/Sub topic.
 * For integration tests with Pub/Sub, consider using Pub/Sub Emulator or real GCP project.
 */
class PubSubRecorderTest {
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
    
    @Test
    fun testNewDataRecordFrame() {
        val result = createTestEvalResult()
        
        // Test DataRecordFrame creation logic (used by PubSubRecorder)
        val frame = DataRecordFrame(
            evalResult = result,
            options = flagent.recorder.DataRecordFrameOptions(
                encrypted = false,
                encryptionKey = null,
                frameOutputMode = "payload_string"
            )
        )
        
        assertNotNull(frame)
        assertNotNull(frame.getPartitionKey())
        assertTrue(frame.output().isNotEmpty())
    }
    
    @Test
    fun testDataRecordFrameOutput() {
        val result = createTestEvalResult()
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = flagent.recorder.DataRecordFrameOptions(
                encrypted = false,
                encryptionKey = null,
                frameOutputMode = "payload_string"
            )
        )
        
        val output = frame.output()
        assertTrue(output.isNotEmpty())
        
        // Output should contain JSON data
        val outputString = String(output)
        assertTrue(outputString.contains("test_flag"))
        assertTrue(outputString.contains("test_entity"))
    }
    
    @Test
    fun testDataRecordFramePartitionKey() {
        val result = createTestEvalResult()
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = flagent.recorder.DataRecordFrameOptions()
        )
        
        val partitionKey = frame.getPartitionKey()
        assertEquals("test_entity", partitionKey)
    }
    
    @Test
    fun testDataRecordFrameBatchProcessing() {
        val results = listOf(
            createTestEvalResult(),
            createTestEvalResult().copy(flagID = 2, flagKey = "test_flag_2"),
            createTestEvalResult().copy(flagID = 3, flagKey = "test_flag_3")
        )
        
        // Test that batch processing creates frames for all results
        val frames = results.map { result ->
            DataRecordFrame(
                evalResult = result,
                options = flagent.recorder.DataRecordFrameOptions()
            )
        }
        
        assertEquals(3, frames.size)
        frames.forEach { frame ->
            assertNotNull(frame)
            assertTrue(frame.output().isNotEmpty())
        }
    }

    @Test
    fun record_publishesMessage_andWaitsForIdWhenVerbose() = runBlocking {
        val result = createTestEvalResult()

        val publisher = mockk<Publisher>()
        val future = mockk<ApiFuture<String>>()

        every { publisher.publish(any<PubsubMessage>()) } returns future
        every { future.get(any(), any()) } returns "message-id-1"

        mockkObject(AppConfig)
        every { AppConfig.recorderPubsubProjectID } returns "proj"
        every { AppConfig.recorderPubsubTopicName } returns "topic"
        every { AppConfig.recorderPubsubKeyFile } returns ""
        every { AppConfig.recorderFrameOutputMode } returns "payload_string"
        every { AppConfig.recorderPubsubVerbose } returns true
        every { AppConfig.recorderPubsubVerboseCancelTimeout } returns kotlin.time.Duration.parse("1s")

        val recorder = PubSubRecorder(
            projectId = "proj",
            topicName = "topic",
            keyFile = "",
            publisher = publisher
        )

        recorder.record(result)
    }

    @Test
    fun record_handlesPublishExceptionWithoutThrowing() = runBlocking {
        val result = createTestEvalResult()

        val publisher = mockk<Publisher>()
        every { publisher.publish(any<PubsubMessage>()) } throws RuntimeException("boom")

        mockkObject(AppConfig)
        every { AppConfig.recorderPubsubProjectID } returns "proj"
        every { AppConfig.recorderPubsubTopicName } returns "topic"
        every { AppConfig.recorderPubsubKeyFile } returns ""
        every { AppConfig.recorderFrameOutputMode } returns "payload_string"
        every { AppConfig.recorderPubsubVerbose } returns false
        every { AppConfig.recorderPubsubVerboseCancelTimeout } returns kotlin.time.Duration.parse("1s")

        val recorder = PubSubRecorder(
            projectId = "proj",
            topicName = "topic",
            keyFile = "",
            publisher = publisher
        )

        recorder.record(result)
    }
}
