package flagent.service

import flagent.config.AppConfig
import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.RolloutDecision
import flagent.domain.entity.SmartRolloutConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.math.pow

/**
 * SlackNotificationService - sends Slack notifications for critical events
 * 
 * Supports:
 * - Critical anomaly alerts
 * - Smart rollout failures/rollbacks
 * - System health issues
 */
class SlackNotificationService(
    private val webhookUrl: String? = AppConfig.slackWebhookUrl,
    private val enabled: Boolean = AppConfig.slackEnabled,
    private val channel: String? = AppConfig.slackChannel,
    private val username: String = "Flagent Alert Bot",
    private val maxRetries: Int = 3,
    private val initialRetryDelayMs: Long = 1000,
    private val maxRetryDelayMs: Long = 10000
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val client = HttpClient(CIO) {
        expectSuccess = false
    }
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Rate limiting state
    private var lastNotificationTime = 0L
    private var notificationCount = 0
    private val rateLimitWindowMs = 60_000L // 1 minute
    private val maxNotificationsPerWindow = 30 // Max 30 notifications per minute
    
    /**
     * Send critical anomaly alert to Slack
     */
    fun sendAnomalyAlert(alert: AnomalyAlert) {
        if (!enabled || webhookUrl.isNullOrBlank()) {
            logger.debug("Slack notifications disabled, skipping anomaly alert")
            return
        }
        
        // Only send critical and high severity alerts
        if (alert.severity != AnomalyAlert.Severity.CRITICAL && 
            alert.severity != AnomalyAlert.Severity.HIGH) {
            logger.debug("Anomaly severity ${alert.severity} below threshold, skipping notification")
            return
        }
        
        scope.launch {
            try {
                val message = buildAnomalyAlertMessage(alert)
                sendSlackMessage(message)
                logger.info("Sent anomaly alert to Slack: flag=${alert.flagKey}, severity=${alert.severity}")
            } catch (e: Exception) {
                logger.error("Failed to send anomaly alert to Slack", e)
            }
        }
    }
    
    /**
     * Send smart rollout notification to Slack
     */
    fun sendRolloutNotification(
        config: SmartRolloutConfig,
        decision: RolloutDecision,
        flagKey: String
    ) {
        if (!enabled || webhookUrl.isNullOrBlank()) {
            logger.debug("Slack notifications disabled, skipping rollout notification")
            return
        }
        
        // Only send for important actions
        if (decision.action != RolloutDecision.Action.ROLLBACK && 
            decision.action != RolloutDecision.Action.FAIL &&
            !config.notifyOnIncrement) {
            return
        }
        
        scope.launch {
            try {
                val message = buildRolloutNotificationMessage(config, decision, flagKey)
                sendSlackMessage(message)
                logger.info("Sent rollout notification to Slack: flag=$flagKey, action=${decision.action}")
            } catch (e: Exception) {
                logger.error("Failed to send rollout notification to Slack", e)
            }
        }
    }
    
    /**
     * Send test notification
     */
    suspend fun sendTestNotification(): Boolean {
        if (!enabled || webhookUrl.isNullOrBlank()) {
            logger.warn("Slack notifications disabled")
            return false
        }
        
        return try {
            val message = SlackMessage(
                text = "🧪 Test notification from Flagent",
                blocks = listOf(
                    SlackBlock(
                        type = "section",
                        text = SlackText(
                            type = "mrkdwn",
                            text = "*Flagent Alert Bot* is configured correctly! :white_check_mark:\n\nYou will receive notifications for:\n• Critical anomaly alerts\n• Smart rollout failures/rollbacks"
                        )
                    )
                ),
                username = username,
                channel = channel
            )
            
            sendSlackMessage(message)
            logger.info("Test notification sent to Slack successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to send test notification to Slack", e)
            false
        }
    }
    
    /**
     * Build Slack message for anomaly alert
     */
    private fun buildAnomalyAlertMessage(alert: AnomalyAlert): SlackMessage {
        val emoji = when (alert.severity) {
            AnomalyAlert.Severity.CRITICAL -> ":rotating_light:"
            AnomalyAlert.Severity.HIGH -> ":warning:"
            AnomalyAlert.Severity.MEDIUM -> ":exclamation:"
            AnomalyAlert.Severity.LOW -> ":information_source:"
        }
        
        val color = when (alert.severity) {
            AnomalyAlert.Severity.CRITICAL -> "#ff0000"
            AnomalyAlert.Severity.HIGH -> "#ff9900"
            AnomalyAlert.Severity.MEDIUM -> "#ffcc00"
            AnomalyAlert.Severity.LOW -> "#3399ff"
        }
        
        val actionText = alert.actionTaken?.let {
            "\n*Action Taken:* ${formatActionTaken(it)}"
        } ?: ""
        
        return SlackMessage(
            text = "$emoji *${alert.severity} Anomaly Detected* in flag `${alert.flagKey}`",
            blocks = listOf(
                SlackBlock(
                    type = "header",
                    text = SlackText(
                        type = "plain_text",
                        text = "$emoji ${alert.severity} Anomaly Detected",
                        emoji = true
                    )
                ),
                SlackBlock(
                    type = "section",
                    fields = listOf(
                        SlackText(
                            type = "mrkdwn",
                            text = "*Flag:*\n`${alert.flagKey}`"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*Type:*\n${formatAnomalyType(alert.anomalyType)}"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*Metric:*\n${alert.metricType.name}"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*Severity:*\n${alert.severity.name}"
                        )
                    )
                ),
                SlackBlock(
                    type = "section",
                    text = SlackText(
                        type = "mrkdwn",
                        text = "*Details:*\n${alert.message}$actionText"
                    )
                ),
                SlackBlock(
                    type = "context",
                    elements = listOf(
                        SlackText(
                            type = "mrkdwn",
                            text = "Detected at: <!date^${alert.detectedAt / 1000}^{date_short_pretty} {time}|${alert.detectedAt}>"
                        )
                    )
                )
            ),
            attachments = listOf(
                SlackAttachment(
                    color = color,
                    fields = listOf(
                        SlackAttachmentField(
                            title = "Current Value",
                            value = String.format("%.4f", alert.metricValue),
                            short = true
                        ),
                        SlackAttachmentField(
                            title = "Expected Value",
                            value = String.format("%.4f", alert.expectedValue),
                            short = true
                        )
                    )
                )
            ),
            username = username,
            channel = channel
        )
    }
    
    /**
     * Build Slack message for rollout notification
     */
    private fun buildRolloutNotificationMessage(
        config: SmartRolloutConfig,
        decision: RolloutDecision,
        flagKey: String
    ): SlackMessage {
        val emoji = when (decision.action) {
            RolloutDecision.Action.INCREMENT -> ":arrow_up:"
            RolloutDecision.Action.ROLLBACK -> ":arrow_down:"
            RolloutDecision.Action.FAIL -> ":x:"
            RolloutDecision.Action.COMPLETE -> ":white_check_mark:"
            RolloutDecision.Action.PAUSE -> ":pause_button:"
        }
        
        val color = when (decision.action) {
            RolloutDecision.Action.INCREMENT -> "#00ff00"
            RolloutDecision.Action.COMPLETE -> "#00cc00"
            RolloutDecision.Action.PAUSE -> "#ffcc00"
            RolloutDecision.Action.ROLLBACK -> "#ff9900"
            RolloutDecision.Action.FAIL -> "#ff0000"
        }
        
        val title = when (decision.action) {
            RolloutDecision.Action.INCREMENT -> "Rollout Incremented"
            RolloutDecision.Action.ROLLBACK -> "Rollout Rolled Back"
            RolloutDecision.Action.FAIL -> "Rollout Failed"
            RolloutDecision.Action.COMPLETE -> "Rollout Completed"
            RolloutDecision.Action.PAUSE -> "Rollout Paused"
        }
        
        return SlackMessage(
            text = "$emoji *$title* for flag `$flagKey`",
            blocks = listOf(
                SlackBlock(
                    type = "header",
                    text = SlackText(
                        type = "plain_text",
                        text = "$emoji $title",
                        emoji = true
                    )
                ),
                SlackBlock(
                    type = "section",
                    fields = listOf(
                        SlackText(
                            type = "mrkdwn",
                            text = "*Flag:*\n`$flagKey`"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*Action:*\n${decision.action.name}"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*Current:*\n${config.currentRolloutPercent}%"
                        ),
                        SlackText(
                            type = "mrkdwn",
                            text = "*New:*\n${decision.newRolloutPercent ?: config.currentRolloutPercent}%"
                        )
                    )
                ),
                SlackBlock(
                    type = "section",
                    text = SlackText(
                        type = "mrkdwn",
                        text = "*Reason:*\n${decision.reason}"
                    )
                )
            ),
            attachments = listOf(
                SlackAttachment(
                    color = color,
                    fields = listOf(
                        SlackAttachmentField(
                            title = "Confidence",
                            value = String.format("%.1f%%", decision.confidence * 100),
                            short = true
                        ),
                        SlackAttachmentField(
                            title = "Target",
                            value = "${config.targetRolloutPercent}%",
                            short = true
                        )
                    )
                )
            ),
            username = username,
            channel = channel
        )
    }
    
    /**
     * Send message to Slack webhook with retry logic and rate limiting
     */
    private suspend fun sendSlackMessage(message: SlackMessage) {
        if (webhookUrl.isNullOrBlank()) {
            throw IllegalStateException("Slack webhook URL not configured")
        }
        
        // Check rate limit
        if (!checkRateLimit()) {
            logger.warn("Slack rate limit exceeded, dropping notification")
            return
        }
        
        // Retry logic with exponential backoff
        var lastException: Exception? = null
        
        for (attempt in 0..maxRetries) {
            try {
                val response = client.post(webhookUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(SlackMessage.serializer(), message))
                }
                
                if (response.status.isSuccess()) {
                    if (attempt > 0) {
                        logger.info("Slack notification sent successfully after $attempt retries")
                    }
                    return
                }
                
                // Handle Slack rate limiting (HTTP 429)
                if (response.status == HttpStatusCode.TooManyRequests) {
                    val retryAfter = response.headers["Retry-After"]?.toLongOrNull() ?: 60
                    logger.warn("Slack rate limit hit (429), waiting ${retryAfter}s before retry")
                    delay(retryAfter * 1000)
                    continue
                }
                
                // Non-retryable errors (4xx except 429)
                if (response.status.value in 400..499) {
                    throw Exception("Slack API returned ${response.status}: ${response.bodyAsText()}")
                }
                
                // Server errors (5xx) - retry
                lastException = Exception("Slack API returned ${response.status}: ${response.bodyAsText()}")
                
            } catch (e: Exception) {
                lastException = e
                logger.warn("Slack notification attempt ${attempt + 1} failed: ${e.message}")
            }
            
            // Don't retry on last attempt
            if (attempt < maxRetries) {
                val delayMs = calculateBackoffDelay(attempt)
                logger.debug("Retrying in ${delayMs}ms...")
                delay(delayMs)
            }
        }
        
        // All retries failed
        logger.error("Failed to send Slack notification after $maxRetries retries", lastException)
        throw lastException ?: Exception("Unknown error sending Slack notification")
    }
    
    /**
     * Calculate exponential backoff delay with jitter
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = (initialRetryDelayMs * 2.0.pow(attempt)).toLong()
        val delayWithCap = exponentialDelay.coerceAtMost(maxRetryDelayMs)
        
        // Add jitter (±25%)
        val jitter = (delayWithCap * 0.25 * (Math.random() - 0.5)).toLong()
        return delayWithCap + jitter
    }
    
    /**
     * Check rate limit
     * Returns true if notification can be sent, false if rate limit exceeded
     */
    private fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        
        // Reset counter if window expired
        if (now - lastNotificationTime > rateLimitWindowMs) {
            notificationCount = 0
            lastNotificationTime = now
        }
        
        // Check if limit exceeded
        if (notificationCount >= maxNotificationsPerWindow) {
            return false
        }
        
        // Increment counter
        notificationCount++
        return true
    }
    
    private fun formatAnomalyType(type: AnomalyAlert.AnomalyType): String {
        return when (type) {
            AnomalyAlert.AnomalyType.HIGH_ERROR_RATE -> "High Error Rate"
            AnomalyAlert.AnomalyType.LOW_SUCCESS_RATE -> "Low Success Rate"
            AnomalyAlert.AnomalyType.HIGH_LATENCY -> "High Latency"
            AnomalyAlert.AnomalyType.LOW_CONVERSION_RATE -> "Low Conversion Rate"
            AnomalyAlert.AnomalyType.STATISTICAL_OUTLIER -> "Statistical Outlier"
            AnomalyAlert.AnomalyType.THRESHOLD_EXCEEDED -> "Threshold Exceeded"
        }
    }
    
    private fun formatActionTaken(action: AnomalyAlert.ActionTaken): String {
        return when (action) {
            AnomalyAlert.ActionTaken.NONE -> "None"
            AnomalyAlert.ActionTaken.ALERT_SENT -> "Alert Sent"
            AnomalyAlert.ActionTaken.ROLLOUT_PAUSED -> "Rollout Paused"
            AnomalyAlert.ActionTaken.ROLLOUT_DECREASED -> "Rollout Decreased"
            AnomalyAlert.ActionTaken.FLAG_DISABLED -> "Flag Disabled (Kill Switch)"
            AnomalyAlert.ActionTaken.VARIANT_DISABLED -> "Variant Disabled"
        }
    }
    
    fun close() {
        client.close()
    }
}

/**
 * Slack message models
 */
@Serializable
data class SlackMessage(
    val text: String,
    val blocks: List<SlackBlock>? = null,
    val attachments: List<SlackAttachment>? = null,
    val username: String? = null,
    val channel: String? = null,
    val icon_emoji: String? = null
)

@Serializable
data class SlackBlock(
    val type: String,
    val text: SlackText? = null,
    val fields: List<SlackText>? = null,
    val elements: List<SlackText>? = null
)

@Serializable
data class SlackText(
    val type: String,
    val text: String,
    val emoji: Boolean? = null
)

@Serializable
data class SlackAttachment(
    val color: String,
    val fields: List<SlackAttachmentField>? = null
)

@Serializable
data class SlackAttachmentField(
    val title: String,
    val value: String,
    val short: Boolean = false
)
