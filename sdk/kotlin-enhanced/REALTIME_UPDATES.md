# Real-Time Updates

Real-time updates enable your application to receive instant flag updates without polling. This provides:

- **Instant Updates** - Flags updated in < 1 second
- **No Polling** - Reduces server load and bandwidth
- **Event-Driven** - React to specific flag changes
- **Efficient** - Single SSE connection for all flags

## Architecture

```
┌──────────────┐
│   Flagent    │
│   Server     │
└──────┬───────┘
       │ SSE Events
       │ (flag.updated, flag.created, etc.)
       ▼
┌──────────────────┐
│ RealtimeClient   │◄─────┐
└──────┬───────────┘      │ Auto-reconnect
       │                  │
       │ Flag Update Events
       ▼
┌──────────────────────┐
│ OfflineFlagentManager│
│  (auto refresh)      │
└──────────────────────┘
```

## Quick Start

### 1. Setup with Real-Time Updates

```kotlin
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.config.OfflineFlagentConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*

// Create HTTP client with SSE support
val httpClient = HttpClient(CIO) {
    install(SSE)
}

// Create manager with httpClient
val manager = OfflineFlagentManager(
    exportApi = exportApi,
    flagApi = flagApi,
    config = OfflineFlagentConfig(
        autoRefresh = false // Disable polling, use realtime instead
    ),
    httpClient = httpClient
)

// Bootstrap
manager.bootstrap()

// Enable real-time updates
manager.enableRealtimeUpdates(
    baseUrl = "http://localhost:18000"
)
```

### 2. Filter Specific Flags

```kotlin
// Only receive updates for specific flags
manager.enableRealtimeUpdates(
    baseUrl = "http://localhost:18000",
    flagKeys = listOf("payment_flow", "checkout_ui"),
    flagIDs = listOf(1L, 2L, 3L)
)
```

### 3. React to Updates

```kotlin
import com.flagent.enhanced.realtime.RealtimeClient

// Create standalone realtime client
val realtimeClient = RealtimeClient(
    httpClient = httpClient,
    baseUrl = "http://localhost:18000"
)

// Connect and listen for events
realtimeClient.connect()

lifecycleScope.launch {
    realtimeClient.events.collect { event ->
        when (event.type) {
            "flag.created" -> {
                println("New flag created: ${event.flagKey}")
            }
            "flag.updated" -> {
                println("Flag updated: ${event.flagKey}")
                // Trigger UI refresh or feature re-evaluation
                refreshFeatures()
            }
            "flag.deleted" -> {
                println("Flag deleted: ${event.flagKey}")
            }
            "flag.toggled" -> {
                val enabled = event.data?.get("enabled")?.toBoolean() ?: false
                println("Flag ${event.flagKey} ${if (enabled) "enabled" else "disabled"}")
            }
        }
    }
}
```

## Event Types

### Flag Events

| Event Type | Description | When Triggered |
|------------|-------------|----------------|
| `flag.created` | New flag created | Flag created via API |
| `flag.updated` | Flag configuration changed | Any flag field updated |
| `flag.deleted` | Flag deleted | Flag deleted via API |
| `flag.toggled` | Flag enabled/disabled | Flag enabled status changed |

### Configuration Events

| Event Type | Description | When Triggered |
|------------|-------------|----------------|
| `segment.updated` | Segment configuration changed | Segment added/updated/deleted |
| `variant.updated` | Variant configuration changed | Variant added/updated/deleted |

### Connection Events

| Event Type | Description |
|------------|-------------|
| `connected` | Successfully connected to SSE |

## Advanced Usage

### Connection Status Monitoring

```kotlin
val realtimeClient = RealtimeClient(httpClient, baseUrl)
realtimeClient.connect()

lifecycleScope.launch {
    realtimeClient.connectionStatus.collect { status ->
        when (status) {
            is ConnectionStatus.Connected -> {
                println("Connected to realtime updates")
                showOnlineIndicator()
            }
            is ConnectionStatus.Connecting -> {
                println("Connecting...")
                showConnectingIndicator()
            }
            is ConnectionStatus.Disconnected -> {
                println("Disconnected")
                showOfflineIndicator()
            }
            is ConnectionStatus.Error -> {
                println("Error: ${status.message}")
                showErrorIndicator(status.message)
            }
        }
    }
}
```

### Custom Reconnection Strategy

```kotlin
val config = RealtimeConfig(
    autoReconnect = true,
    reconnectDelayMs = 2000 // 2 seconds initial delay
)

val realtimeClient = RealtimeClient(
    httpClient = httpClient,
    baseUrl = baseUrl,
    config = config
)
```

### Combining with Auto-Refresh

```kotlin
// Use both polling and real-time for maximum reliability
val manager = OfflineFlagentManager(
    exportApi = exportApi,
    flagApi = flagApi,
    config = OfflineFlagentConfig(
        autoRefresh = true,        // Fallback polling
        refreshIntervalMs = 300_000 // Every 5 minutes
    ),
    httpClient = httpClient
)

manager.bootstrap()

// Enable real-time for instant updates
manager.enableRealtimeUpdates(baseUrl = "http://localhost:18000")

// Now you have:
// 1. Real-time updates (< 1s latency)
// 2. Fallback polling (if SSE fails)
// 3. Persistent cache (offline support)
```

## Configuration Options

```kotlin
data class RealtimeConfig(
    // Enable automatic reconnection
    val autoReconnect: Boolean = true,
    
    // Initial delay before reconnection (uses exponential backoff)
    val reconnectDelayMs: Long = 1000
)
```

## Best Practices

### 1. Error Handling

```kotlin
try {
    manager.enableRealtimeUpdates(baseUrl = "http://localhost:18000")
} catch (e: IllegalStateException) {
    // httpClient not provided
    logger.error("Cannot enable realtime: ${e.message}")
    // Fallback to polling
}
```

### 2. Cleanup on App Shutdown

```kotlin
class MyApplication : Application() {
    override fun onTerminate() {
        manager.shutdown() // Disconnects SSE and cleans up
        super.onTerminate()
    }
}
```

### 3. Conditional Real-Time (Based on Network)

```kotlin
fun enableRealtimeIfOnline() {
    if (isNetworkAvailable() && !isMeteredConnection()) {
        manager.enableRealtimeUpdates(baseUrl = "http://localhost:18000")
    } else {
        // Use polling on metered/slow connections
        manager.disableRealtimeUpdates()
    }
}

// Listen to network changes
networkCallback.onAvailable = {
    enableRealtimeIfOnline()
}

networkCallback.onLost = {
    manager.disableRealtimeUpdates()
}
```

### 4. Debounced Refresh

```kotlin
val realtimeClient = RealtimeClient(httpClient, baseUrl)
realtimeClient.connect()

lifecycleScope.launch {
    realtimeClient.events
        .debounce(1000) // Wait 1s for more events
        .collect { event ->
            // Refresh once after burst of updates
            manager.refresh()
        }
}
```

## Performance

### Latency Comparison

```
Polling (60s interval):
- Average update latency: 30s
- Server requests: 60 req/hour

Real-Time (SSE):
- Average update latency: < 1s
- Server requests: 1 connection (persistent)

Improvement: 30x faster, 98% less traffic
```

### Resource Usage

```
SSE Connection:
- Memory: ~50KB per connection
- Bandwidth: ~1KB/min (keep-alive)
- CPU: Negligible

Polling (60s interval):
- Memory: Same as SSE
- Bandwidth: ~500KB-5MB/hour (depending on payload)
- CPU: Higher (HTTP overhead)

Resource savings: 90%+ bandwidth reduction
```

## Troubleshooting

### Connection fails

**Problem**: `enableRealtimeUpdates()` throws exception

**Solutions**:
1. Ensure `httpClient` provided in constructor
2. Check server URL is correct
3. Verify server has SSE endpoint enabled
4. Check network connectivity

### No events received

**Problem**: Connected but not receiving events

**Reasons**:
1. Filters too restrictive (flagKeys/flagIDs)
2. Server not publishing events
3. Firewall blocking SSE

**Solutions**:
1. Remove filters: `connect(flagKeys = null, flagIDs = null)`
2. Check server logs for event publication
3. Test with curl: `curl -N http://localhost:18000/api/v1/realtime/sse`

### Frequent reconnections

**Problem**: Constant connect/disconnect cycles

**Reasons**:
1. Network unstable
2. Server restarting
3. Firewall closing idle connections

**Solutions**:
1. Increase `reconnectDelayMs` to reduce reconnect frequency
2. Enable polling fallback: `config.autoRefresh = true`
3. Check server keep-alive settings

## Server-Side Configuration

### Enabling SSE in Flagent Server

SSE is enabled by default. No configuration needed.

### Health Check

```bash
# Check SSE health
curl http://localhost:18000/api/v1/realtime/sse/health

# Expected response:
{
  "status": "healthy",
  "activeConnections": 5,
  "protocol": "SSE"
}
```

### Testing SSE

```bash
# Connect to SSE stream
curl -N http://localhost:18000/api/v1/realtime/sse

# Expected output:
event: connection
data: {"type":"connected","message":"Connected to Flagent realtime updates"}

event: flag.updated
data: {"type":"flag.updated","flagID":1,"flagKey":"new_feature","message":"Flag updated: new_feature","timestamp":1699999999999}
```

## Migration from Polling

### Before (Polling Only)

```kotlin
val manager = OfflineFlagentManager(exportApi, flagApi, config)
manager.bootstrap()

// Polls every 60 seconds
// Average latency: 30s
```

### After (Real-Time)

```kotlin
val httpClient = HttpClient(CIO) { install(SSE) }

val manager = OfflineFlagentManager(
    exportApi, flagApi,
    config.copy(autoRefresh = false), // Disable polling
    httpClient
)
manager.bootstrap()
manager.enableRealtimeUpdates(baseUrl)

// Real-time updates
// Average latency: < 1s
```

## Comparison: Polling vs Real-Time

| Feature | Polling | Real-Time (SSE) |
|---------|---------|-----------------|
| Update Latency | ~30s (avg) | < 1s |
| Server Load | High | Low |
| Bandwidth | High | Low |
| Battery Impact | Medium | Low |
| Complexity | Low | Medium |
| Reliability | High | High (with auto-reconnect) |

## See Also

- [Client-Side Evaluation](CLIENT_SIDE_EVALUATION.md)
- [Kotlin Enhanced SDK README](README.md)
- [Flagent API Documentation](https://maxluxs.github.io/Flagent/api/openapi.yaml)
