package flagent.service

import flagent.api.model.FlagResponse
import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.domain.repository.IWebhookRepository
import flagent.route.mapper.ResponseMappers.mapFlagToResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

private val logger = KotlinLogging.logger {}

/**
 * WebhookService - CRUD for webhooks and async dispatch with retry.
 */
class WebhookService(
    private val webhookRepository: IWebhookRepository,
    private val flagRepository: flagent.domain.repository.IFlagRepository? = null,
    private val tenantId: String? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
    }

    suspend fun create(webhook: Webhook): Webhook =
        webhookRepository.create(webhook.copy(tenantId = webhook.tenantId ?: tenantId))

    suspend fun findById(id: Int): Webhook? = webhookRepository.findById(id, tenantId)
    suspend fun findAll(): List<Webhook> = webhookRepository.findAll(tenantId)
    suspend fun update(webhook: Webhook): Webhook = webhookRepository.update(webhook)
    suspend fun delete(id: Int): Boolean = webhookRepository.delete(id, tenantId)

    /**
     * Dispatch webhook event asynchronously (fire-and-forget with retry).
     */
    fun dispatch(event: String, payload: WebhookPayload) {
        scope.launch {
            val webhooks = webhookRepository.findByEvent(event, tenantId)
            webhooks.forEach { webhook ->
                launch { dispatchToWebhook(webhook, event, payload) }
            }
        }
    }

    /**
     * Dispatch flag.created event.
     */
    fun dispatchFlagCreated(flagId: Int) {
        scope.launch {
            val flag = flagRepository?.findById(flagId) ?: return@launch
            val data = mapFlagToResponse(flag)
            dispatch(WebhookEvents.FLAG_CREATED, WebhookPayload(event = WebhookEvents.FLAG_CREATED, flagData = data))
        }
    }

    /**
     * Dispatch flag.updated event.
     */
    fun dispatchFlagUpdated(flagId: Int) {
        scope.launch {
            val flag = flagRepository?.findById(flagId) ?: return@launch
            val data = mapFlagToResponse(flag)
            dispatch(WebhookEvents.FLAG_UPDATED, WebhookPayload(event = WebhookEvents.FLAG_UPDATED, flagData = data))
        }
    }

    /**
     * Dispatch flag.deleted event.
     */
    fun dispatchFlagDeleted(flagId: Int, flagKey: String) {
        val data = WebhookFlagDeletedData(flagId = flagId, flagKey = flagKey, deleted = true)
        dispatch(WebhookEvents.FLAG_DELETED, WebhookPayload(event = WebhookEvents.FLAG_DELETED, flagDeletedData = data))
    }

    /**
     * Dispatch flag.enabled or flag.disabled event.
     */
    fun dispatchFlagToggled(flagId: Int, enabled: Boolean) {
        scope.launch {
            val flag = flagRepository?.findById(flagId) ?: return@launch
            val data = mapFlagToResponse(flag)
            val event = if (enabled) WebhookEvents.FLAG_ENABLED else WebhookEvents.FLAG_DISABLED
            dispatch(event, WebhookPayload(event = event, flagData = data))
        }
    }

    private suspend fun dispatchToWebhook(webhook: Webhook, event: String, payload: WebhookPayload) {
        val body = json.encodeToString(payload)
        val retryDelays = listOf(1000L, 5000L, 30000L)
        for ((attempt, delayMs) in retryDelays.withIndex()) {
            try {
                val signature = webhook.secret?.let { secret -> computeHmacSha256(body, secret) }
                httpClient.post(webhook.url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                    header("X-Flagent-Event", event)
                    header("X-Flagent-Delivery", java.util.UUID.randomUUID().toString())
                    signature?.let { header("X-Flagent-Signature", "sha256=$it") }
                }
                logger.debug { "Webhook delivered: ${webhook.url} event=$event" }
                return
            } catch (e: Exception) {
                logger.warn(e) { "Webhook delivery failed (attempt ${attempt + 1}/${retryDelays.size}): ${webhook.url} - ${e.message}" }
                if (attempt < retryDelays.lastIndex) delay(delayMs)
            }
        }
    }

    private fun computeHmacSha256(message: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return Base64.getEncoder().encodeToString(mac.doFinal(message.toByteArray(Charsets.UTF_8)))
    }
}

@Serializable
data class WebhookPayload(
    val event: String,
    val timestamp: Long = System.currentTimeMillis(),
    val flagData: FlagResponse? = null,
    val flagDeletedData: WebhookFlagDeletedData? = null
) {
    constructor(event: String, flagData: FlagResponse) : this(event, System.currentTimeMillis(), flagData, null)
    constructor(event: String, flagDeletedData: WebhookFlagDeletedData) : this(event, System.currentTimeMillis(), null, flagDeletedData)
}

@Serializable
data class WebhookFlagDeletedData(
    val flagId: Int,
    val flagKey: String,
    val deleted: Boolean = true
)
