package com.flagent.enhanced.analytics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Firebase-level analytics client for Flagent.
 * Logs events (first_open, session_start, screen_view, custom) to Flagent backend.
 *
 * @param baseUrl Base URL of Flagent server (e.g. "http://localhost:18000")
 * @param apiKey Optional X-API-Key for tenant-scoped analytics
 * @param platform Platform identifier: "android", "ios", "web"
 * @param appVersion Optional app version string
 */
class FlagentAnalytics(
    private val baseUrl: String,
    private val apiKey: String? = null,
    private val platform: String = "android",
    private val appVersion: String? = null,
    private val userId: String? = null,
    httpClient: HttpClient? = null
) {
    private val client = httpClient ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val eventBuffer = mutableListOf<AnalyticsEventPayload>()
    private var sessionId: String = java.util.UUID.randomUUID().toString()

    private val bufferLock = Any()
    private val maxBufferSize = 10
    private val flushIntervalMs = 5000L

    init {
        logSessionStart()
        logFirstOpenIfNeeded()
        scope.launch {
            while (true) {
                delay(flushIntervalMs)
                flush()
            }
        }
    }

    private fun logFirstOpenIfNeeded() {
        try {
            val markerDir = System.getProperty("java.io.tmpdir") ?: return
            val markerFile = java.io.File(markerDir, "flagent_first_open_${baseUrl.hashCode().toString(36)}")
            if (markerFile.exists()) return
            markerFile.writeText("1")
            logFirstOpen()
        } catch (_: Exception) { /* best-effort */ }
    }

    /**
     * Log an analytics event (Firebase-style: first_open, session_start, screen_view, custom).
     */
    fun logEvent(eventName: String, params: Map<String, String>? = null) {
        val event = AnalyticsEventPayload(
            eventName = eventName,
            eventParams = params?.let { Json.encodeToString(buildJsonObject { it.forEach { (k, v) -> put(k, v) } }) },
            userId = userId,
            sessionId = sessionId,
            platform = platform,
            appVersion = appVersion,
            timestampMs = System.currentTimeMillis()
        )
        synchronized(bufferLock) {
            eventBuffer.add(event)
            if (eventBuffer.size >= maxBufferSize) {
                scope.launch { flush() }
            }
        }
    }

    /**
     * Log first_open (call on first app launch).
     */
    fun logFirstOpen() = logEvent("first_open")

    /**
     * Log session_start (call when app/session starts).
     */
    fun logSessionStart() = logEvent("session_start")

    /**
     * Log screen_view (call on screen/navigation change).
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val params = mutableMapOf<String, String>("screen" to screenName)
        screenClass?.let { params["screen_class"] = it }
        logEvent("screen_view", params)
    }

    /**
     * Flush buffered events to server.
     */
    fun flush() {
        val toSend = synchronized(bufferLock) {
            if (eventBuffer.isEmpty()) return
            eventBuffer.toList().also { eventBuffer.clear() }
        }
        scope.launch {
            try {
                val body = AnalyticsEventsBatch(events = toSend)
                client.post("$baseUrl/api/v1/analytics/events") {
                    contentType(ContentType.Application.Json)
                    apiKey?.let { header("X-API-Key", it) }
                    setBody(Json.encodeToString(AnalyticsEventsBatch.serializer(), body))
                }
            } catch (_: Exception) {
                synchronized(bufferLock) { eventBuffer.addAll(0, toSend) }
            }
        }
    }

    @Serializable
    private data class AnalyticsEventsBatch(val events: List<AnalyticsEventPayload>)

    @Serializable
    private data class AnalyticsEventPayload(
        val eventName: String,
        val eventParams: String? = null,
        val userId: String? = null,
        val sessionId: String,
        val platform: String? = null,
        val appVersion: String? = null,
        val timestampMs: Long
    )
}

