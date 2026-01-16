package flagent.recorder.impl

import flagent.config.AppConfig
import flagent.recorder.DataRecorder
import flagent.recorder.DataRecordFrame
import flagent.recorder.DataRecordFrameOptions
import flagent.service.EvalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

private val logger = KotlinLogging.logger {}

/**
 * KafkaRecorder - records evaluation results to Kafka
 * Maps to pkg/handler/data_recorder_kafka.go from original project
 */
class KafkaRecorder(
    private val topic: String = AppConfig.recorderKafkaTopic,
    private val brokers: String = AppConfig.recorderKafkaBrokers,
    private val partitionKeyEnabled: Boolean = AppConfig.recorderKafkaPartitionKeyEnabled
) : DataRecorder {
    private val producer: KafkaProducer<String, ByteArray>
    
    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")
            put(ProducerConfig.COMPRESSION_TYPE_CONFIG, getCompressionType(AppConfig.recorderKafkaCompressionCodec))
            put(ProducerConfig.ACKS_CONFIG, AppConfig.recorderKafkaRequiredAcks.toString())
            put(ProducerConfig.RETRIES_CONFIG, AppConfig.recorderKafkaRetryMax)
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, AppConfig.recorderKafkaMaxOpenReqs)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, AppConfig.recorderKafkaIdempotent)
            put(ProducerConfig.BATCH_SIZE_CONFIG, 16384)
            put(ProducerConfig.LINGER_MS_CONFIG, AppConfig.recorderKafkaFlushFrequency.inWholeMilliseconds.toInt())
        }
        
        // Configure TLS if needed
        if (AppConfig.recorderKafkaCertFile.isNotEmpty() && AppConfig.recorderKafkaKeyFile.isNotEmpty()) {
            configureTLS(props)
        }
        
        // Configure SASL if needed
        if (AppConfig.recorderKafkaSASLUsername.isNotEmpty() && AppConfig.recorderKafkaSASLPassword.isNotEmpty()) {
            props.put("security.protocol", "SASL_SSL")
            props.put("sasl.mechanism", "PLAIN")
            props.put("sasl.jaas.config", 
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"${AppConfig.recorderKafkaSASLUsername}\" " +
                "password=\"${AppConfig.recorderKafkaSASLPassword}\";")
        }
        
        producer = KafkaProducer(props)
        
        // Log errors in background
        // Note: KafkaProducer doesn't have a direct error callback in Java client
        // Errors are typically handled via Future callbacks
    }
    
    override suspend fun record(result: EvalResult) {
        withContext(Dispatchers.IO) {
            try {
                val frame = newDataRecordFrame(result)
                val output = frame.output()
                val partitionKey = if (partitionKeyEnabled) frame.getPartitionKey() else null
                
                val record = ProducerRecord(topic, partitionKey, output)
                producer.send(record) { metadata, exception ->
                    if (exception != null) {
                        logger.error(exception) { "Failed to write evaluation result to Kafka" }
                    } else if (AppConfig.recorderKafkaVerbose) {
                        logger.debug { "Sent evaluation result to Kafka: ${metadata.topic()}-${metadata.partition()}" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate data record frame for Kafka recorder" }
            }
        }
    }
    
    override suspend fun recordBatch(results: List<EvalResult>) {
        withContext(Dispatchers.IO) {
            results.forEach { result ->
                record(result)
            }
        }
    }
    
    override fun newDataRecordFrame(result: EvalResult): DataRecordFrame {
        return DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions(
                encrypted = AppConfig.recorderKafkaEncrypted,
                encryptionKey = if (AppConfig.recorderKafkaEncrypted) AppConfig.recorderKafkaEncryptionKey else null,
                frameOutputMode = AppConfig.recorderFrameOutputMode
            )
        )
    }
    
    /**
     * Get compression type from codec number
     */
    private fun getCompressionType(codec: Int): String {
        return when (codec) {
            1 -> "gzip"
            2 -> "snappy"
            3 -> "lz4"
            4 -> "zstd"
            else -> "none"
        }
    }
    
    /**
     * Configure TLS for Kafka producer
     */
    private fun configureTLS(props: Properties) {
        try {
            if (AppConfig.recorderKafkaSimpleSSL) {
                // Simple SSL without certificate verification
                props.put("security.protocol", "SSL")
                if (!AppConfig.recorderKafkaVerifySSL) {
                    // Note: In production, you should properly configure SSL context
                    // This is a simplified version
                }
            } else {
                // Full TLS with certificates
                props.put("security.protocol", "SSL")
                // Load certificates and configure SSL context
                // This is a simplified version - in production, use proper certificate loading
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to configure TLS for Kafka" }
        }
    }
    
    /**
     * Close producer (should be called on shutdown)
     */
    fun close() {
        producer.close()
    }
}
