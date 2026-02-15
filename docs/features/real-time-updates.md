# Real-Time Updates (SSE)

> [English](real-time-updates.md) | [Русский](real-time-updates.ru.md)

## Overview

Flagent supports real-time flag updates via Server-Sent Events (SSE). Clients receive updates when flags change, without polling or restarting the application.

**Current implementation:** SSE is available in [Kotlin Enhanced](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/REALTIME_UPDATES.md) and [Go Enhanced](https://github.com/MaxLuxs/Flagent/blob/main/sdk/go-enhanced/REALTIME_UPDATES.md) SDKs. WebSocket is planned.

## Key features

### 1. WebSocket support

Bidirectional communication for real-time updates.

**Benefits:** Low latency, bidirectional, efficient, multiple connections.

**Protocol:** `ws://flags.example.com/ws`

**Example:**

```kotlin
val wsClient = FlagentWebSocketClient(
    url = "wss://flags.example.com/ws",
    apiKey = "your-api-key"
)
wsClient.subscribe("new_payment_flow") { flag ->
    println("Flag updated: ${flag.key} - enabled: ${flag.enabled}")
    localCache.update(flag)
}
wsClient.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US")
) { result -> println("Evaluation result: ${result.variantKey}") }
```

### 2. Server-Sent Events (SSE)

One-way event stream. **Benefits:** Simple, auto-reconnect, HTTP-based, low overhead.

**Endpoint:** `GET /api/v1/events/stream`

**Example:**

```kotlin
val sseClient = FlagentSSEClient(
    url = "https://flags.example.com/api/v1/events/stream",
    apiKey = "your-api-key"
)
sseClient.subscribe { event ->
    when (event.type) {
        EventType.FLAG_UPDATED -> { val flag = event.data as Flag; localCache.update(flag) }
        EventType.FLAG_DELETED -> { localCache.remove(event.data as String) }
    }
}
```

### 3. Subscription management

Subscribe by flag, by tags, to all flags, or to segment changes.

### 4. Automatic reconnection

Exponential backoff, max attempts, subscription preservation.

### 5. Event types

`FLAG_UPDATED`, `FLAG_CREATED`, `FLAG_DELETED`, `FLAG_ENABLED`, `FLAG_DISABLED`, `SEGMENT_UPDATED`, `DISTRIBUTION_UPDATED`.

## API

**WebSocket:** `wss://flags.example.com/ws`. Message types: subscribe, flag_updated, evaluate, evaluation_result (see SDK docs).

**SSE:** `GET /api/v1/events/stream`. Headers: `Accept: text/event-stream`, `Authorization: Bearer {token}`. Events: `flag_updated`, `flag_created`, `flag_deleted`.

## SDK integration

Kotlin and JavaScript clients: `FlagentWebSocketClient`, `FlagentSSEClient` with `subscribe` / `subscribeAll`.

## Configuration

`WebSocketConfig` (enabled, port, path, pingInterval, maxConnections). `SSEConfig` (enabled, path, keepAliveInterval, maxClients). Env: `FLAGENT_WS_*`, `FLAGENT_SSE_*`.

## Use cases

Real-time flag updates without restart; live configuration; debugging and monitoring.

## Roadmap

Phase 1: WebSocket server and client. Phase 2: SSE endpoint and client. Phase 3: Event filtering, batch updates, compression, auth.

## Related documentation

- [Offline-First SDK](offline-first-sdk.md)
- [API Documentation](../api/endpoints.md)
