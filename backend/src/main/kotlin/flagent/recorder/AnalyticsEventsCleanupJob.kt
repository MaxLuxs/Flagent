package flagent.recorder

import flagent.config.AppConfig
import flagent.repository.impl.AnalyticsEventRepository
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
 * Periodic cleanup of old analytics_events for retention control.
 * Runs deleteOlderThan every analyticsCleanupInterval.
 */
class AnalyticsEventsCleanupJob(
    private val repository: AnalyticsEventRepository,
    private val retentionDays: Int = AppConfig.analyticsRetentionDays,
    private val interval: Duration = AppConfig.analyticsCleanupInterval,
    private val enabled: Boolean = AppConfig.analyticsCleanupEnabled
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        if (!enabled) {
            logger.info { "Analytics events cleanup disabled" }
            return
        }
        scope.launch {
            logger.info {
                "Analytics events cleanup started: retention $retentionDays days, interval $interval"
            }
            while (isActive) {
                try {
                    val cutoffMs = System.currentTimeMillis() -
                        (retentionDays * 24L * 60 * 60 * 1000)
                    val deleted = repository.deleteOlderThan(cutoffMs)
                    if (deleted > 0) {
                        logger.info { "Analytics events cleanup: deleted $deleted rows older than $retentionDays days" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Analytics events cleanup failed" }
                }
                delay(interval)
            }
        }
    }

    fun stop() {
        scope.cancel()
        logger.info { "Analytics events cleanup stopped" }
    }
}
