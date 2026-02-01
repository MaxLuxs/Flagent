package flagent.recorder.impl

import flagent.config.AppConfig
import flagent.recorder.DataRecordFrame
import flagent.recorder.DataRecordFrameOptions
import flagent.recorder.DataRecorder
import flagent.service.EvalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest

private val logger = KotlinLogging.logger {}

/**
 * KinesisRecorder - records evaluation results to AWS Kinesis
 * Maps to pkg/handler/data_recorder_kinesis.go from original project
 */
class KinesisRecorder(
    private val streamName: String = AppConfig.recorderKinesisStreamName,
    private val region: Region = Region.US_EAST_1 // Default region, should be configurable
) : DataRecorder {
    private val kinesisClient: KinesisClient = KinesisClient.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build()

    override suspend fun record(result: EvalResult) {
        withContext(Dispatchers.IO) {
            try {
                val frame = newDataRecordFrame(result)
                val output = frame.output()
                val partitionKey = frame.getPartitionKey().ifEmpty { result.flagID.toString() }

                val request = PutRecordRequest.builder()
                    .streamName(streamName)
                    .data(SdkBytes.fromByteArray(output))
                    .partitionKey(partitionKey)
                    .build()

                val response = kinesisClient.putRecord(request)

                if (AppConfig.recorderKinesisVerbose) {
                    logger.debug { "Sent evaluation result to Kinesis: ${response.sequenceNumber()}" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send evaluation result to Kinesis" }
            }
        }
    }

    override suspend fun recordBatch(results: List<EvalResult>) {
        withContext(Dispatchers.IO) {
            // Kinesis supports batch operations via PutRecords API
            // For simplicity, we'll use individual records here
            // In production, consider batching for better performance
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
     * Close client (should be called on shutdown)
     */
    fun close() {
        kinesisClient.close()
    }
}
