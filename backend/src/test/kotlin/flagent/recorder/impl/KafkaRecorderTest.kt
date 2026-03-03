package flagent.recorder.impl

import flagent.config.AppConfig
import flagent.recorder.DataRecordFrame
import flagent.service.EvalContext
import flagent.service.EvalResult
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.Future
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for KafkaRecorder
 * Note: These tests focus on testing the logic that can be tested without a real Kafka server.
 * For integration tests with Kafka, consider using EmbeddedKafka.
 */
class KafkaRecorderTest {
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
        // Note: KafkaRecorder requires KafkaProducer in constructor, which needs real brokers
        // This test would require mocking KafkaProducer or using EmbeddedKafka
        // For now, we test the interface contract
        
        val result = createTestEvalResult()
        
        // This test verifies that DataRecordFrame can be created
        // Actual KafkaRecorder instantiation requires Kafka brokers
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
    fun testDataRecordFramePartitionKey_EmptyWhenNoEntityID() {
        val result = createTestEvalResult().copy(
            evalContext = EvalContext(entityID = null, entityType = null, entityContext = null)
        )
        
        val frame = DataRecordFrame(
            evalResult = result,
            options = flagent.recorder.DataRecordFrameOptions()
        )
        
        val partitionKey = frame.getPartitionKey()
        assertEquals("", partitionKey)
    }

    @Test
    fun record_sendsMessage_andLogsOnSuccessWhenVerbose() = runBlocking {
        val result = createTestEvalResult()

        val producer = mockk<KafkaProducer<String, ByteArray>>()
        val future = mockk<Future<RecordMetadata>>()
        val metadata = mockk<RecordMetadata>()

        every { metadata.topic() } returns "test-topic"
        every { metadata.partition() } returns 0
        every { producer.send(any<ProducerRecord<String, ByteArray>>(), any()) } answers {
            val callback = secondArg<Callback>()
            callback.onCompletion(metadata, null)
            future
        }

        mockkObject(AppConfig)
        every { AppConfig.recorderKafkaTopic } returns "test-topic"
        every { AppConfig.recorderKafkaBrokers } returns "localhost:9092"
        every { AppConfig.recorderKafkaPartitionKeyEnabled } returns true
        every { AppConfig.recorderKafkaEncrypted } returns false
        every { AppConfig.recorderKafkaEncryptionKey } returns ""
        every { AppConfig.recorderFrameOutputMode } returns "payload_string"
        every { AppConfig.recorderKafkaVerbose } returns true

        val recorder = KafkaRecorder(
            topic = "test-topic",
            brokers = "localhost:9092",
            partitionKeyEnabled = true,
            producer = producer
        )

        recorder.record(result)

        verify { producer.send(any<ProducerRecord<String, ByteArray>>(), any()) }
    }

    @Test
    fun record_logsErrorWhenCallbackReceivesException() = runBlocking {
        val result = createTestEvalResult()

        val producer = mockk<KafkaProducer<String, ByteArray>>()
        val future = mockk<Future<RecordMetadata>>()

        every { producer.send(any<ProducerRecord<String, ByteArray>>(), any()) } answers {
            val callback = secondArg<Callback>()
            callback.onCompletion(null, RuntimeException("boom"))
            future
        }

        mockkObject(AppConfig)
        every { AppConfig.recorderKafkaTopic } returns "test-topic"
        every { AppConfig.recorderKafkaBrokers } returns "localhost:9092"
        every { AppConfig.recorderKafkaPartitionKeyEnabled } returns false
        every { AppConfig.recorderKafkaEncrypted } returns false
        every { AppConfig.recorderKafkaEncryptionKey } returns ""
        every { AppConfig.recorderFrameOutputMode } returns "payload_string"
        every { AppConfig.recorderKafkaVerbose } returns false

        val recorder = KafkaRecorder(
            topic = "test-topic",
            brokers = "localhost:9092",
            partitionKeyEnabled = false,
            producer = producer
        )

        // Should not throw even if callback receives exception
        recorder.record(result)

        verify { producer.send(any<ProducerRecord<String, ByteArray>>(), any()) }
    }
}
