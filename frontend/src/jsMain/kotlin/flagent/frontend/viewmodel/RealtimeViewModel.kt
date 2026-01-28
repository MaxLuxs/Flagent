package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.service.RealtimeEvent
import flagent.frontend.service.RealtimeEventType
import flagent.frontend.service.RealtimeService
import flagent.frontend.state.Notification
import flagent.frontend.state.NotificationType
import flagent.frontend.util.AppLogger

/**
 * ViewModel for Realtime updates
 */
class RealtimeViewModel(
    private val onNotification: (Notification) -> Unit
) {
    private val TAG = "RealtimeViewModel"
    private var service: RealtimeService? = null
    
    var isConnected by mutableStateOf(false)
        private set
    
    var recentEvents by mutableStateOf<List<RealtimeEvent>>(emptyList())
        private set
    
    fun connect(flagKeys: List<String> = emptyList(), flagIds: List<Int> = emptyList()) {
        service = RealtimeService(
            onEvent = { event ->
                handleEvent(event)
            },
            onConnectionChange = { connected ->
                isConnected = connected
            }
        )
        service?.connect(flagKeys, flagIds)
    }
    
    fun disconnect() {
        service?.disconnect()
        service = null
    }
    
    private fun handleEvent(event: RealtimeEvent) {
        recentEvents = (listOf(event) + recentEvents).take(50)
        
        // Create notification
        val message = when (event.type) {
            RealtimeEventType.FLAG_CREATED -> "New flag created"
            RealtimeEventType.FLAG_UPDATED -> "Flag updated"
            RealtimeEventType.FLAG_DELETED -> "Flag deleted"
            RealtimeEventType.FLAG_TOGGLED -> "Flag toggled"
            RealtimeEventType.SEGMENT_UPDATED -> "Segment updated"
            RealtimeEventType.VARIANT_UPDATED -> "Variant updated"
        }
        
        onNotification(
            Notification(
                message = message,
                type = NotificationType.INFO,
                duration = 3000L
            )
        )
        
        AppLogger.info(TAG, "Handled event: ${event.type}")
    }
}
