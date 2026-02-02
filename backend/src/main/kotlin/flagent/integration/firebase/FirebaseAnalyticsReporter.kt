package flagent.integration.firebase

import flagent.config.AppConfig
import flagent.service.EvalResult
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Sends evaluation events to GA4 via Measurement Protocol.
 * Events attach to user when app_instance_id (Firebase) or client_id (web) is in entityContext.
 */
class FirebaseAnalyticsReporter(
    private val apiSecret: String = AppConfig.firebaseAnalyticsApiSecret,
    private val measurementId: String = AppConfig.firebaseAnalyticsMeasurementId,
    private val appInstanceIdKey: String = AppConfig.firebaseAnalyticsAppInstanceIdKey,
    private val clientIdKey: String = AppConfig.firebaseAnalyticsClientIdKey,
    httpClient: HttpClient? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = httpClient ?: HttpClient(CIO) { expectSuccess = false }
    private val closeClientOnClose = httpClient == null
    private val baseUrl = "https://www.google-analytics.com/mp/collect"

    fun recordAsync(result: EvalResult) {
        val identity = extractAnalyticsIdentity(result)
        if (identity == null) {
            return
        }
        scope.launch {
            try {
                sendEvent(result, identity)
            } catch (e: Exception) {
                logger.debug(e) { "Firebase Analytics: failed to send event for ${result.flagKey}" }
            }
        }
    }

    private fun extractAnalyticsIdentity(result: EvalResult): Pair<String, String>? {
        val ctx = result.evalContext.entityContext ?: return null
        fun stringValue(el: kotlinx.serialization.json.JsonElement?): String? =
            el?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        stringValue(ctx[appInstanceIdKey])?.let { return "app_instance_id" to it }
        stringValue(ctx[clientIdKey])?.let { return "client_id" to it }
        return null
    }

    private suspend fun sendEvent(result: EvalResult, identity: Pair<String, String>) =
        withContext(Dispatchers.IO) {
            val (identityKey, identityValue) = identity
            val eventName = if (result.variantKey != null) "experiment_assigned" else "flag_evaluated"
            val eventParams = buildJsonObject {
                put("flag_key", result.flagKey)
                result.evalContext.entityID?.let { put("entity_id", it) }
                if (result.variantKey != null) put("variant", result.variantKey)
            }
            val body = buildJsonObject {
                put(identityKey, identityValue)
                put("events", buildJsonArray {
                    add(buildJsonObject {
                        put("name", eventName)
                        put("params", eventParams)
                    })
                })
            }
            val url = "$baseUrl?measurement_id=$measurementId&api_secret=$apiSecret"
            val response = client.post(url) {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(body.toString())
            }
            if (!response.status.isSuccess()) {
                val body = response.bodyAsText()
                logger.debug { "Firebase Analytics: ${response.status} $body" }
            }
        }

    fun close() {
        if (closeClientOnClose) {
            client.close()
        }
    }
}
