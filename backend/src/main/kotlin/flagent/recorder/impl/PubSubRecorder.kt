package flagent.recorder.impl

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import flagent.config.AppConfig
import flagent.recorder.DataRecordFrame
import flagent.recorder.DataRecordFrameOptions
import flagent.recorder.DataRecorder
import flagent.service.EvalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * PubSubRecorder - records evaluation results to Google Cloud Pub/Sub
 * Maps to pkg/handler/data_recorder_pubsub.go from original project
 */
class PubSubRecorder(
    private val projectId: String = AppConfig.recorderPubsubProjectID,
    private val topicName: String = AppConfig.recorderPubsubTopicName,
    private val keyFile: String = AppConfig.recorderPubsubKeyFile
) : DataRecorder {
    private val publisher: Publisher

    init {
        val topic = TopicName.of(projectId, topicName)

        val publisherBuilder = Publisher.newBuilder(topic)

        // Configure credentials if key file is provided
        if (keyFile.isNotEmpty()) {
            try {
                val credentials = GoogleCredentials.fromStream(FileInputStream(keyFile))
                publisherBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            } catch (e: Exception) {
                logger.error(e) { "Failed to load Pub/Sub credentials from key file" }
                throw RuntimeException("Failed to initialize Pub/Sub publisher", e)
            }
        }

        publisher = publisherBuilder.build()
    }

    override suspend fun record(result: EvalResult) {
        withContext(Dispatchers.IO) {
            try {
                val frame = newDataRecordFrame(result)
                val output = frame.output()

                val message = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFrom(output))
                    .build()

                val future = publisher.publish(message)

                if (AppConfig.recorderPubsubVerbose) {
                    try {
                        withTimeout(AppConfig.recorderPubsubVerboseCancelTimeout) {
                            val messageId = future.get(
                                AppConfig.recorderPubsubVerboseCancelTimeout.inWholeMilliseconds,
                                TimeUnit.MILLISECONDS
                            )
                            logger.debug { "Sent evaluation result to Pub/Sub: $messageId" }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to get Pub/Sub publish result" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send evaluation result to Pub/Sub" }
            }
        }
    }

    override suspend fun recordBatch(results: List<EvalResult>) {
        withContext(Dispatchers.IO) {
            results.forEach { result ->
                try {
                    record(result)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to record result in batch" }
                }
            }
        }
    }

    override fun newDataRecordFrame(result: EvalResult): DataRecordFrame {
        return DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions(
                encrypted = false, // Not implemented yet in original
                encryptionKey = null,
                frameOutputMode = AppConfig.recorderFrameOutputMode
            )
        )
    }

    /**
     * Close publisher (should be called on shutdown)
     */
    fun close() {
        publisher.shutdown()
    }
}
