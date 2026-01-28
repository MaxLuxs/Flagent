package flagent.middleware

import flagent.route.RealtimeEventBus
import io.ktor.server.application.*
import io.ktor.server.sse.*
import io.ktor.util.*

/**
 * Configure SSE (Server-Sent Events) support for real-time updates.
 */
fun Application.configureSSE() {
    install(SSE)
}

/**
 * Application attribute key for RealtimeEventBus.
 */
val RealtimeEventBusKey = AttributeKey<RealtimeEventBus>("RealtimeEventBus")

/**
 * Extension to get RealtimeEventBus from Application.
 */
val Application.realtimeEventBus: RealtimeEventBus
    get() = attributes.getOrNull(RealtimeEventBusKey) ?: error("RealtimeEventBus not configured")

/**
 * Extension to check if RealtimeEventBus is configured.
 */
fun Application.hasRealtimeEventBus(): Boolean = attributes.contains(RealtimeEventBusKey)

/**
 * Configure RealtimeEventBus.
 */
fun Application.configureRealtimeEventBus(): RealtimeEventBus {
    val eventBus = RealtimeEventBus()
    attributes.put(RealtimeEventBusKey, eventBus)
    return eventBus
}
