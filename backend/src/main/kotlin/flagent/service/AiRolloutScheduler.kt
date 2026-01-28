package flagent.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

/**
 * AiRolloutScheduler - background job for periodic anomaly detection and smart rollout execution
 * 
 * Runs periodically to:
 * 1. Detect anomalies for all enabled configs
 * 2. Execute smart rollouts for active configs
 * 3. Clean up old data
 */
class AiRolloutScheduler(
    private val anomalyDetectionService: AnomalyDetectionService,
    private val smartRolloutService: SmartRolloutService,
    private val metricsCollectionService: MetricsCollectionService,
    private val anomalyDetectionIntervalMs: Long = 300_000, // 5 minutes
    private val smartRolloutIntervalMs: Long = 600_000, // 10 minutes
    private val cleanupIntervalMs: Long = 86400_000 // 24 hours
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var anomalyDetectionJob: Job? = null
    private var smartRolloutJob: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    /**
     * Start scheduler
     */
    fun start() {
        logger.info("Starting AI Rollout Scheduler...")
        
        // Start anomaly detection job
        anomalyDetectionJob = scope.launch {
            while (isActive) {
                try {
                    runAnomalyDetection()
                } catch (e: Exception) {
                    logger.error("Error in anomaly detection job", e)
                }
                delay(anomalyDetectionIntervalMs)
            }
        }
        
        // Start smart rollout job
        smartRolloutJob = scope.launch {
            while (isActive) {
                try {
                    runSmartRollouts()
                } catch (e: Exception) {
                    logger.error("Error in smart rollout job", e)
                }
                delay(smartRolloutIntervalMs)
            }
        }
        
        // Start cleanup job
        cleanupJob = scope.launch {
            while (isActive) {
                try {
                    runCleanup()
                } catch (e: Exception) {
                    logger.error("Error in cleanup job", e)
                }
                delay(cleanupIntervalMs)
            }
        }
        
        logger.info("AI Rollout Scheduler started successfully")
    }
    
    /**
     * Stop scheduler
     */
    fun stop() {
        logger.info("Stopping AI Rollout Scheduler...")
        
        anomalyDetectionJob?.cancel()
        smartRolloutJob?.cancel()
        cleanupJob?.cancel()
        
        scope.cancel()
        
        logger.info("AI Rollout Scheduler stopped")
    }
    
    /**
     * Run anomaly detection for all enabled configs
     */
    private suspend fun runAnomalyDetection() {
        logger.debug("Running anomaly detection...")
        
        val reports = anomalyDetectionService.detectAnomaliesForAll()
        
        val totalFlags = reports.size
        val flagsWithAnomalies = reports.count { it.hasAnomalies }
        val totalAnomalies = reports.sumOf { it.anomalyCount }
        val criticalAnomalies = reports.sumOf { it.criticalAnomalies.size }
        
        logger.info(
            "Anomaly detection completed: $totalFlags flags checked, " +
            "$flagsWithAnomalies with anomalies, " +
            "$totalAnomalies total anomalies ($criticalAnomalies critical)"
        )
        
        // Log critical anomalies
        reports.forEach { report ->
            report.criticalAnomalies.forEach { alert ->
                logger.warn(
                    "CRITICAL ANOMALY detected: flag_id=${alert.flagId}, " +
                    "type=${alert.anomalyType}, severity=${alert.severity}, " +
                    "message=${alert.message}"
                )
            }
        }
    }
    
    /**
     * Run smart rollouts for all active configs
     */
    private suspend fun runSmartRollouts() {
        logger.debug("Running smart rollouts...")
        
        val results = smartRolloutService.executeAllRollouts()
        
        val total = results.size
        val successful = results.count { it.success }
        val increments = results.count { it.decision?.action == flagent.domain.entity.RolloutDecision.Action.INCREMENT }
        val rollbacks = results.count { it.decision?.action == flagent.domain.entity.RolloutDecision.Action.ROLLBACK }
        val paused = results.count { it.decision?.action == flagent.domain.entity.RolloutDecision.Action.PAUSE }
        val completed = results.count { it.decision?.action == flagent.domain.entity.RolloutDecision.Action.COMPLETE }
        
        logger.info(
            "Smart rollouts completed: $successful/$total successful, " +
            "$increments increments, $rollbacks rollbacks, " +
            "$paused paused, $completed completed"
        )
        
        // Log important decisions
        results.forEach { result ->
            when (result.decision?.action) {
                flagent.domain.entity.RolloutDecision.Action.INCREMENT -> {
                    logger.info(
                        "Rollout incremented: config_id=${result.configId}, " +
                        "new_percent=${result.decision.newRolloutPercent}, " +
                        "reason=${result.decision.reason}"
                    )
                }
                flagent.domain.entity.RolloutDecision.Action.ROLLBACK -> {
                    logger.warn(
                        "Rollout rolled back: config_id=${result.configId}, " +
                        "new_percent=${result.decision.newRolloutPercent}, " +
                        "reason=${result.decision.reason}"
                    )
                }
                flagent.domain.entity.RolloutDecision.Action.COMPLETE -> {
                    logger.info(
                        "Rollout completed: config_id=${result.configId}, " +
                        "reason=${result.decision.reason}"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Run cleanup for old data
     */
    private suspend fun runCleanup() {
        logger.debug("Running cleanup...")
        
        val now = System.currentTimeMillis()
        val retentionDays = 30
        val cutoffTime = now - (retentionDays * 24 * 3600 * 1000L)
        
        // Clean up old metrics (older than 30 days)
        val metricsDeleted = metricsCollectionService.cleanupOldMetrics(cutoffTime)
        logger.info("Cleanup: deleted $metricsDeleted old metrics")
        
        // Clean up old alerts (older than 30 days)
        val alertsDeleted = anomalyDetectionService.cleanupOldAlerts(cutoffTime)
        logger.info("Cleanup: deleted $alertsDeleted old alerts")
        
        // Clean up old rollout history (older than 30 days)
        val historyDeleted = smartRolloutService.cleanupOldHistory(cutoffTime)
        logger.info("Cleanup: deleted $historyDeleted old rollout history entries")
        
        logger.info("Cleanup completed: total deleted ${metricsDeleted + alertsDeleted + historyDeleted} records")
    }
}
