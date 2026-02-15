package com.flagent.enhanced.realtime

import com.flagent.enhanced.platform.logWarn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RealtimeClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val config: RealtimeConfig = RealtimeConfig()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var connectionJob: Job? = null

    private val _events = MutableSharedFlow<FlagUpdateEvent>(
        replay = 0,
        extraBufferCapacity = 100
    )
    val events: SharedFlow<FlagUpdateEvent> = _events.asSharedFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun connect(
        flagKeys: List<String>? = null,
        flagIDs: List<Long>? = null
    ) {
        if (connectionJob?.isActive == true) {
            logWarn("Already connected to realtime updates")
            return
        }
        connectionJob = scope.launch {
            var reconnectAttempt = 0
            while (isActive) {
                try {
                    _connectionStatus.value = ConnectionStatus.Connecting
                    val url = buildString {
                        append("$baseUrl/api/v1/realtime/sse")
                        val params = mutableListOf<String>()
                        flagKeys?.forEach { params.add("flagKey=$it") }
                        flagIDs?.forEach { params.add("flagID=$it") }
                        if (params.isNotEmpty()) append("?${params.joinToString("&")}")
                    }
                    httpClient.sse(url) {
                        _connectionStatus.value = ConnectionStatus.Connected
                        reconnectAttempt = 0
                        incoming.collect { event -> handleSSEEvent(event) }
                    }
                    _connectionStatus.value = ConnectionStatus.Disconnected
                } catch (e: Exception) {
                    logWarn("SSE connection error: ${e.message}")
                    _connectionStatus.value = ConnectionStatus.Error(e.message ?: "Unknown error")
                    if (!config.autoReconnect) break
                    val delayMs = config.reconnectDelayMs * (1 shl reconnectAttempt.coerceAtMost(5))
                    reconnectAttempt++
                    delay(delayMs)
                }
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    private suspend fun handleSSEEvent(event: ServerSentEvent) {
        try {
            when (event.event) {
                "flag.created", "flag.updated", "flag.deleted", "flag.toggled",
                "segment.updated", "variant.updated" -> {
                    val flagEvent = json.decodeFromString<FlagUpdateEvent>(event.data ?: return)
                    _events.emit(flagEvent)
                }
                else -> { }
            }
        } catch (e: Exception) {
            logWarn("Failed to parse SSE event: ${event.data}")
        }
    }

    fun shutdown() {
        disconnect()
        scope.cancel()
    }
}

data class RealtimeConfig(
    val autoReconnect: Boolean = true,
    val reconnectDelayMs: Long = 1000
)

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

@Serializable
data class FlagUpdateEvent(
    val type: String,
    val flagID: Long? = null,
    val flagKey: String? = null,
    val message: String,
    val data: Map<String, String>? = null,
    val timestamp: Long
)
