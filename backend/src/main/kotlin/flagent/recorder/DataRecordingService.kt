package flagent.recorder

import flagent.config.AppConfig
import flagent.recorder.impl.*
import flagent.service.EvalResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * DataRecordingService - orchestrates data recorders with retry and batch processing
 * Maps to data recording logic from pkg/handler/eval.go
 */
class DataRecordingService {
    private val recorder: DataRecorder
    private val recordingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val recordingChannel = Channel<EvalResult>(Channel.UNLIMITED)
    private val batchSize = 100
    private val batchTimeout: Duration = 1.seconds
    private val maxRetries = 3
    private val retryDelay: Duration = 100.milliseconds
    
    init {
        recorder = createRecorder()
        startRecordingProcessor()
    }
    
    /**
     * Create recorder based on configuration
     */
    private fun createRecorder(): DataRecorder {
        if (!AppConfig.recorderEnabled) {
            return NoopRecorder()
        }
        
        return when (AppConfig.recorderType.lowercase()) {
            "kafka" -> KafkaRecorder()
            "kinesis" -> KinesisRecorder()
            "pubsub" -> PubSubRecorder()
            "noop" -> NoopRecorder()
            else -> {
                logger.warn { "Unknown recorder type: ${AppConfig.recorderType}, using NoopRecorder" }
                NoopRecorder()
            }
        }
    }
    
    /**
     * Start background processor for recording
     */
    private fun startRecordingProcessor() {
        recordingScope.launch {
            val batch = mutableListOf<EvalResult>()
            var lastBatchTime = System.currentTimeMillis()
            
            while (isActive) {
                try {
                    val timeout = withTimeoutOrNull(batchTimeout) {
                        recordingChannel.receive()
                    }
                    
                    if (timeout != null) {
                        batch.add(timeout)
                        lastBatchTime = System.currentTimeMillis()
                    }
                    
                    // Send batch if size reached or timeout
                    val shouldFlush = batch.size >= batchSize || 
                                     (timeout == null && batch.isNotEmpty() && 
                                      System.currentTimeMillis() - lastBatchTime >= batchTimeout.inWholeMilliseconds)
                    
                    if (shouldFlush) {
                        if (batch.isNotEmpty()) {
                            recordBatchWithRetry(batch.toList())
                            batch.clear()
                            lastBatchTime = System.currentTimeMillis()
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error in recording processor" }
                }
            }
        }
    }
    
    /**
     * Record evaluation result asynchronously
     */
    fun recordAsync(result: EvalResult) {
        if (!AppConfig.recorderEnabled) {
            return
        }
        
        try {
            recordingChannel.trySend(result)
        } catch (e: Exception) {
            logger.error(e) { "Failed to queue evaluation result for recording" }
            // Fallback to synchronous recording
            recordingScope.launch {
                recordWithRetry(result)
            }
        }
    }
    
    /**
     * Record with retry logic
     */
    private suspend fun recordWithRetry(result: EvalResult, attempt: Int = 1) {
        try {
            recorder.record(result)
        } catch (e: Exception) {
            if (attempt < maxRetries) {
                logger.warn(e) { "Failed to record evaluation result (attempt $attempt/$maxRetries), retrying..." }
                delay(retryDelay * attempt) // Exponential backoff
                recordWithRetry(result, attempt + 1)
            } else {
                logger.error(e) { "Failed to record evaluation result after $maxRetries attempts" }
            }
        }
    }
    
    /**
     * Record batch with retry logic
     */
    private suspend fun recordBatchWithRetry(results: List<EvalResult>, attempt: Int = 1) {
        try {
            recorder.recordBatch(results)
        } catch (e: Exception) {
            if (attempt < maxRetries) {
                logger.warn(e) { "Failed to record batch (attempt $attempt/$maxRetries), retrying..." }
                delay(retryDelay * attempt)
                recordBatchWithRetry(results, attempt + 1)
            } else {
                logger.error(e) { "Failed to record batch after $maxRetries attempts" }
                // Fallback to individual records
                results.forEach { result ->
                    try {
                        recordWithRetry(result)
                    } catch (e2: Exception) {
                        logger.error(e2) { "Failed to record individual result in fallback" }
                    }
                }
            }
        }
    }
    
    /**
     * Stop recording service
     */
    fun stop() {
        recordingScope.cancel()
        when (val r = recorder) {
            is KafkaRecorder -> r.close()
            is KinesisRecorder -> r.close()
            is PubSubRecorder -> r.close()
        }
    }
}
