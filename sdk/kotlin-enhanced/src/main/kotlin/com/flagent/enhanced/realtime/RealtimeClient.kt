package com.flagent.enhanced.realtime

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * RealtimeClient - connects to Flagent SSE endpoint for real-time flag updates.
 *
 * Provides:
 * - Real-time flag update notifications
 * - Automatic reconnection on disconnect
 * - Event filtering by flag keys or IDs
 * - Connection status monitoring
 *
 * @param httpClient HTTP client for SSE connection
 * @param baseUrl Base URL of Flagent server
 * @param config Configuration for realtime client
 *
 * @example
 * ```
 * val client = RealtimeClient(
 *     httpClient = HttpClient { install(SSE) },
 *     baseUrl = "http://localhost:18000",
 *     config = RealtimeConfig()
 * )
 *
 * // Subscribe to all flag updates
 * client.connect()
 * client.events.collect { event ->
 *     println("Flag updated: ${event.flagKey}")
 *     // Trigger snapshot refresh
 * }
 *
 * // Disconnect when done
 * client.disconnect()
 * ```
 */
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
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    /**
     * Connect to SSE endpoint and start receiving events.
     *
     * @param flagKeys Optional list of flag keys to filter events
     * @param flagIDs Optional list of flag IDs to filter events
     */
    fun connect(
        flagKeys: List<String>? = null,
        flagIDs: List<Long>? = null
    ) {
        if (connectionJob?.isActive == true) {
            logger.warn { "Already connected to realtime updates" }
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
                        
                        if (params.isNotEmpty()) {
                            append("?${params.joinToString("&")}")
                        }
                    }
                    
                    logger.info { "Connecting to SSE: $url" }
                    
                    httpClient.sse(url) {
                        _connectionStatus.value = ConnectionStatus.Connected
                        reconnectAttempt = 0
                        logger.info { "Connected to realtime updates" }
                        
                        incoming.collect { event ->
                            handleSSEEvent(event)
                        }
                    }
                    
                    // Connection closed
                    _connectionStatus.value = ConnectionStatus.Disconnected
                    logger.info { "SSE connection closed" }
                    
                } catch (e: Exception) {
                    logger.error(e) { "SSE connection error: ${e.message}" }
                    _connectionStatus.value = ConnectionStatus.Error(e.message ?: "Unknown error")
                    
                    if (!config.autoReconnect) {
                        break
                    }
                    
                    // Exponential backoff for reconnection
                    val delayMs = config.reconnectDelayMs * (1 shl reconnectAttempt.coerceAtMost(5))
                    reconnectAttempt++
                    
                    logger.info { "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt)..." }
                    delay(delayMs)
                }
            }
        }
    }
    
    /**
     * Disconnect from SSE endpoint.
     */
    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _connectionStatus.value = ConnectionStatus.Disconnected
        logger.info { "Disconnected from realtime updates" }
    }
    
    /**
     * Handle incoming SSE event.
     */
    private suspend fun handleSSEEvent(event: ServerSentEvent) {
        try {
            when (event.event) {
                "connection" -> {
                    logger.debug { "Connection event: ${event.data}" }
                }
                "flag.created", "flag.updated", "flag.deleted", "flag.toggled",
                "segment.updated", "variant.updated" -> {
                    val flagEvent = json.decodeFromString<FlagUpdateEvent>(event.data ?: return)
                    _events.emit(flagEvent)
                    logger.debug { "Received event: ${flagEvent.type} for flag ${flagEvent.flagKey}" }
                }
                else -> {
                    logger.debug { "Unknown event type: ${event.event}" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse SSE event: ${event.data}" }
        }
    }
    
    /**
     * Shutdown the client.
     */
    fun shutdown() {
        disconnect()
        scope.cancel()
    }
}

/**
 * Configuration for realtime client.
 */
data class RealtimeConfig(
    /**
     * Enable automatic reconnection on disconnect.
     * Default: true
     */
    val autoReconnect: Boolean = true,
    
    /**
     * Initial delay before reconnection in milliseconds.
     * Uses exponential backoff on subsequent attempts.
     * Default: 1 second
     */
    val reconnectDelayMs: Long = 1000
)

/**
 * Connection status.
 */
sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

/**
 * Flag update event from SSE.
 */
@Serializable
data class FlagUpdateEvent(
    val type: String,
    val flagID: Long? = null,
    val flagKey: String? = null,
    val message: String,
    val data: Map<String, String>? = null,
    val timestamp: Long
)
