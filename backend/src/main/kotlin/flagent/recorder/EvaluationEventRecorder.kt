package flagent.recorder

import flagent.repository.impl.EvaluationEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * Records evaluation events asynchronously for core metrics (evaluation count).
 * Fire-and-forget: does not block evaluation.
 */
class EvaluationEventRecorder(
    private val repository: EvaluationEventRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channel = Channel<Pair<Int, Long>>(Channel.UNLIMITED)
    private val batchSize = 100
    private val batchTimeout = 1.seconds

    init {
        startProcessor()
    }

    private fun startProcessor() {
        scope.launch {
            val batch = mutableListOf<Pair<Int, Long>>()
            var lastBatchTime = System.currentTimeMillis()

            while (isActive) {
                try {
                    val event = withTimeoutOrNull(batchTimeout) {
                        channel.receive()
                    }

                    if (event != null) {
                        batch.add(event)
                        lastBatchTime = System.currentTimeMillis()
                    }

                    val shouldFlush = batch.size >= batchSize ||
                        (event == null && batch.isNotEmpty() &&
                            System.currentTimeMillis() - lastBatchTime >= batchTimeout.inWholeMilliseconds)

                    if (shouldFlush && batch.isNotEmpty()) {
                        try {
                            repository.saveBatch(batch.toList())
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to save evaluation events batch" }
                        }
                        batch.clear()
                        lastBatchTime = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error in evaluation event processor" }
                }
            }
        }
    }

    fun record(flagId: Int, timestampMs: Long) {
        try {
            channel.trySend(flagId to timestampMs)
        } catch (e: Exception) {
            logger.error(e) { "Failed to queue evaluation event" }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
