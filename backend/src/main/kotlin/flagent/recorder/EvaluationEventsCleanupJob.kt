package flagent.recorder

import flagent.config.AppConfig
import flagent.repository.impl.EvaluationEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Periodic cleanup of old evaluation_events for retention control.
 * Runs deleteOlderThan every evaluationEventsCleanupInterval.
 *
 * @param repository evaluation event repository
 * @param retentionDays override for tests (default: AppConfig)
 * @param interval override for tests (default: AppConfig)
 * @param enabled override for tests (default: AppConfig)
 */
class EvaluationEventsCleanupJob(
    private val repository: EvaluationEventRepository,
    private val retentionDays: Int = AppConfig.evaluationEventsRetentionDays,
    private val interval: Duration = AppConfig.evaluationEventsCleanupInterval,
    private val enabled: Boolean = AppConfig.evaluationEventsCleanupEnabled
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        if (!enabled) {
            logger.info { "Evaluation events cleanup disabled" }
            return
        }
        scope.launch {
            logger.info {
                "Evaluation events cleanup started: retention $retentionDays days, interval $interval"
            }
            while (isActive) {
                try {
                    val cutoffMs = System.currentTimeMillis() -
                        (retentionDays * 24L * 60 * 60 * 1000)
                    val deleted = repository.deleteOlderThan(cutoffMs)
                    if (deleted > 0) {
                        logger.info { "Evaluation events cleanup: deleted $deleted rows older than $retentionDays days" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Evaluation events cleanup failed" }
                }
                delay(interval)
            }
        }
    }

    fun stop() {
        scope.cancel()
        logger.info { "Evaluation events cleanup stopped" }
    }
}
