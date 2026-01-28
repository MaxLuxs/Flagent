package flagent.route

import flagent.service.FlagService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * RealtimeRoutes - SSE endpoints for real-time flag updates.
 *
 * Provides Server-Sent Events (SSE) for clients to receive instant flag updates
 * without polling.
 *
 * Endpoints:
 * - GET /api/v1/realtime/sse - SSE stream for flag updates
 */
fun Route.realtimeRoutes(
    flagService: FlagService,
    eventBus: RealtimeEventBus
) {
    route("/realtime") {
        // SSE endpoint for real-time updates
        sse("/sse") {
            // Query parameters for filtering
            val flagKeys = call.request.queryParameters.getAll("flagKey")
            val flagIDs = call.request.queryParameters.getAll("flagID")?.mapNotNull { it.toLongOrNull() }
            
            // Send initial connection event
            send(
                ServerSentEvent(
                    data = Json.encodeToString(
                        RealtimeEvent(
                            type = "connected",
                            message = "Connected to Flagent realtime updates"
                        )
                    ),
                    event = "connected"
                )
            )
            
            // Subscribe to flag update events
            eventBus.subscribe().collect { event ->
                // Filter events if specific flags are requested
                if (flagKeys != null && event.flagKey != null) {
                    if (!flagKeys.contains(event.flagKey)) {
                        return@collect
                    }
                }
                
                if (flagIDs != null && event.flagID != null) {
                    if (!flagIDs.contains(event.flagID)) {
                        return@collect
                    }
                }
                
                // Send event to client
                send(
                    ServerSentEvent(
                        data = Json.encodeToString(event),
                        event = event.type,
                        id = event.timestamp.toString()
                    )
                )
            }
        }
        
        // Health check endpoint for SSE
        get("/sse/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "healthy",
                    "activeConnections" to eventBus.getActiveConnectionCount(),
                    "protocol" to "SSE"
                )
            )
        }
    }
}

/**
 * RealtimeEventBus - broadcasts flag update events to connected clients.
 *
 * Uses SharedFlow for broadcasting events to multiple subscribers.
 * Thread-safe and supports multiple concurrent connections.
 */
class RealtimeEventBus {
    private val _events = MutableSharedFlow<RealtimeEvent>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    val events = _events.asSharedFlow()
    
    private var activeConnections = 0
    
    /**
     * Publish an event to all subscribers.
     */
    suspend fun publish(event: RealtimeEvent) {
        _events.emit(event)
    }
    
    /**
     * Subscribe to events.
     */
    fun subscribe() = events
    
    /**
     * Get active connection count.
     */
    fun getActiveConnectionCount(): Int = _events.subscriptionCount.value
    
    /**
     * Publish flag created event.
     */
    suspend fun publishFlagCreated(flagID: Long, flagKey: String) {
        publish(
            RealtimeEvent(
                type = "flag.created",
                flagID = flagID,
                flagKey = flagKey,
                message = "Flag created: $flagKey"
            )
        )
    }
    
    /**
     * Publish flag updated event.
     */
    suspend fun publishFlagUpdated(flagID: Long, flagKey: String) {
        publish(
            RealtimeEvent(
                type = "flag.updated",
                flagID = flagID,
                flagKey = flagKey,
                message = "Flag updated: $flagKey"
            )
        )
    }
    
    /**
     * Publish flag deleted event.
     */
    suspend fun publishFlagDeleted(flagID: Long, flagKey: String) {
        publish(
            RealtimeEvent(
                type = "flag.deleted",
                flagID = flagID,
                flagKey = flagKey,
                message = "Flag deleted: $flagKey"
            )
        )
    }
    
    /**
     * Publish flag enabled/disabled event.
     */
    suspend fun publishFlagToggled(flagID: Long, flagKey: String, enabled: Boolean) {
        publish(
            RealtimeEvent(
                type = "flag.toggled",
                flagID = flagID,
                flagKey = flagKey,
                message = "Flag ${if (enabled) "enabled" else "disabled"}: $flagKey",
                data = mapOf("enabled" to enabled)
            )
        )
    }
    
    /**
     * Publish segment updated event.
     */
    suspend fun publishSegmentUpdated(flagID: Long, flagKey: String, segmentID: Long) {
        publish(
            RealtimeEvent(
                type = "segment.updated",
                flagID = flagID,
                flagKey = flagKey,
                message = "Segment updated in flag: $flagKey",
                data = mapOf("segmentID" to segmentID)
            )
        )
    }
    
    /**
     * Publish variant updated event.
     */
    suspend fun publishVariantUpdated(flagID: Long, flagKey: String, variantID: Long) {
        publish(
            RealtimeEvent(
                type = "variant.updated",
                flagID = flagID,
                flagKey = flagKey,
                message = "Variant updated in flag: $flagKey",
                data = mapOf("variantID" to variantID)
            )
        )
    }
}

/**
 * Realtime event model.
 */
@Serializable
data class RealtimeEvent(
    val type: String,
    val flagID: Long? = null,
    val flagKey: String? = null,
    val message: String,
    val data: Map<String, @Contextual Any>? = null,
    val timestamp: Long = System.currentTimeMillis()
)
