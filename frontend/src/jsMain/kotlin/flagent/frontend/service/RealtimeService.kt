package flagent.frontend.service

import flagent.frontend.config.AppConfig
import flagent.frontend.util.AppLogger
import kotlinx.browser.window
import org.w3c.dom.EventSource
import org.w3c.dom.MessageEvent

/**
 * Service for Server-Sent Events (SSE) integration
 */
class RealtimeService(
    private val onEvent: (RealtimeEvent) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit
) {
    private val TAG = "RealtimeService"
    private var eventSource: EventSource? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    
    /**
     * Connect to SSE endpoint
     */
    fun connect(flagKeys: List<String> = emptyList(), flagIds: List<Int> = emptyList()) {
        disconnect()
        
        val url = buildString {
            append("${AppConfig.apiBaseUrl}/realtime/sse")
            val params = mutableListOf<String>()
            flagKeys.forEach { params.add("flagKey=$it") }
            flagIds.forEach { params.add("flagID=$it") }
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        
        AppLogger.info(TAG, "Connecting to SSE: $url")
        
        eventSource = EventSource(url).apply {
            onopen = {
                AppLogger.info(TAG, "SSE connected")
                reconnectAttempts = 0
                onConnectionChange(true)
            }
            
            onerror = {
                AppLogger.error(TAG, "SSE error")
                onConnectionChange(false)
                
                // Attempt reconnection with backoff
                if (reconnectAttempts < maxReconnectAttempts) {
                    val delay = minOf(1000 * (1 shl reconnectAttempts), 30000)
                    window.setTimeout({
                        reconnectAttempts++
                        connect(flagKeys, flagIds)
                    }, delay)
                }
            }
            
            addEventListener("flag.created", { event ->
                handleEvent(RealtimeEventType.FLAG_CREATED, (event as MessageEvent).data as String)
            })
            
            addEventListener("flag.updated", { event ->
                handleEvent(RealtimeEventType.FLAG_UPDATED, (event as MessageEvent).data as String)
            })
            
            addEventListener("flag.deleted", { event ->
                handleEvent(RealtimeEventType.FLAG_DELETED, (event as MessageEvent).data as String)
            })
            
            addEventListener("flag.toggled", { event ->
                handleEvent(RealtimeEventType.FLAG_TOGGLED, (event as MessageEvent).data as String)
            })
            
            addEventListener("segment.updated", { event ->
                handleEvent(RealtimeEventType.SEGMENT_UPDATED, (event as MessageEvent).data as String)
            })
            
            addEventListener("variant.updated", { event ->
                handleEvent(RealtimeEventType.VARIANT_UPDATED, (event as MessageEvent).data as String)
            })
        }
    }
    
    /**
     * Disconnect from SSE
     */
    fun disconnect() {
        eventSource?.close()
        eventSource = null
        onConnectionChange(false)
        AppLogger.info(TAG, "SSE disconnected")
    }
    
    private fun handleEvent(type: RealtimeEventType, data: String) {
        AppLogger.debug(TAG, "Received event: $type")
        onEvent(RealtimeEvent(type, data))
    }
}

/**
 * Realtime event types
 */
enum class RealtimeEventType {
    FLAG_CREATED,
    FLAG_UPDATED,
    FLAG_DELETED,
    FLAG_TOGGLED,
    SEGMENT_UPDATED,
    VARIANT_UPDATED
}

/**
 * Realtime event
 */
data class RealtimeEvent(
    val type: RealtimeEventType,
    val data: String
)
