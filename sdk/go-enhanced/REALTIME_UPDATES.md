# Real-Time Updates for Go SDK

Real-time updates enable your application to receive instant flag changes via Server-Sent Events (SSE) without polling.

## Features

- ✅ **Instant Updates** - Receive flag changes in real-time (< 1s latency)
- ✅ **No Polling** - Eliminates need for periodic polling
- ✅ **Automatic Reconnection** - Auto-reconnects on connection loss
- ✅ **Event Filtering** - Subscribe to specific flags or all flags
- ✅ **Integrated with Client-Side Eval** - Auto-refreshes snapshot on updates

## Architecture

```
┌─────────────────────────┐
│   OfflineManager        │
│  (Client-Side Eval)     │
└────────┬────────────────┘
         │
         │ EnableRealtimeUpdates()
         ▼
┌─────────────────────────┐       SSE Connection
│      SSEClient          │◄─────────────────────┐
└────────┬────────────────┘                      │
         │ Events Channel                        │
         ▼                                       │
┌─────────────────────────┐                     │
│  Auto Snapshot Refresh  │                     │
└─────────────────────────┘                     │
                                                 │
                                                 │
┌────────────────────────────────────────────────┤
│              Flagent Server                    │
│          (SSE Endpoint: /api/v1/realtime/sse) │
└────────────────────────────────────────────────┘
```

## Quick Start

### 1. Setup Offline Manager

```go
import (
    "context"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
    enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

// Create base client
client, _ := flagent.NewClient("http://localhost:18000/api/v1")

// Create offline manager
manager := enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
defer manager.Close()

// Bootstrap
ctx := context.Background()
manager.Bootstrap(ctx, false)
```

### 2. Enable Real-Time Updates

```go
// Enable SSE for all flags
err := manager.EnableRealtimeUpdates(
    "http://localhost:18000", // Base URL
    nil,                       // All flags (or []string{"flag_a", "flag_b"})
    nil,                       // All flag IDs (or []int64{1, 2, 3})
)
if err != nil {
    log.Fatal(err)
}

// Check status
if manager.IsRealtimeEnabled() {
    fmt.Println("Real-time updates active!")
}
```

### 3. Evaluate Flags (Auto-Updated)

```go
// Evaluate - will automatically use latest snapshot after SSE updates
result, _ := manager.Evaluate(ctx, "new_feature", "user123", nil)

if result.IsEnabled() {
    // Feature is enabled (always up-to-date!)
}
```

## Advanced Usage

### Filter by Specific Flags

```go
// Only receive updates for specific flags
flagKeys := []string{"new_payment_flow", "premium_feature"}
err := manager.EnableRealtimeUpdates("http://localhost:18000", flagKeys, nil)
```

### Filter by Flag IDs

```go
// Only receive updates for specific flag IDs
flagIDs := []int64{1, 2, 5}
err := manager.EnableRealtimeUpdates("http://localhost:18000", nil, flagIDs)
```

### Disable Real-Time Updates

```go
// Disable SSE and fall back to polling (if auto-refresh enabled)
manager.DisableRealtimeUpdates()
```

### Custom SSE Configuration

For advanced use cases, you can create a standalone SSE client:

```go
sseConfig := enhanced.DefaultSSEConfig().
    WithAutoReconnect(true).
    WithReconnectDelay(2 * time.Second).
    WithMaxReconnectAttempts(10).
    WithEventBufferSize(200).
    WithDebugLogging(true)

sseClient := enhanced.NewSSEClient(
    "http://localhost:18000",
    nil, // HTTP client (nil = default)
    sseConfig,
)

// Connect
sseClient.Connect(nil, nil)
defer sseClient.Disconnect()

// Listen for events
for event := range sseClient.Events() {
    fmt.Printf("Flag updated: %s (%s)\n", *event.FlagKey, event.Type)
    // Manually trigger snapshot refresh
    manager.Refresh(ctx)
}
```

## Event Types

The following SSE events are supported:

- `flag.created` - New flag created
- `flag.updated` - Flag configuration updated
- `flag.deleted` - Flag deleted
- `flag.toggled` - Flag enabled/disabled
- `segment.updated` - Segment configuration changed
- `variant.updated` - Variant configuration changed

## Configuration Options

### SSEConfig

```go
type SSEConfig struct {
    // AutoReconnect enables automatic reconnection (default: true)
    AutoReconnect bool
    
    // ReconnectDelay is initial delay before reconnection (default: 1s)
    ReconnectDelay time.Duration
    
    // MaxReconnectAttempts limits reconnection attempts (default: 0 = unlimited)
    MaxReconnectAttempts int
    
    // EventBufferSize is the event channel buffer size (default: 100)
    EventBufferSize int
    
    // EnableDebugLogging enables debug logging (default: false)
    EnableDebugLogging bool
}
```

## Best Practices

### 1. Enable Real-Time Updates After Bootstrap

```go
// Bootstrap first (loads snapshot)
manager.Bootstrap(ctx, false)

// Then enable SSE
manager.EnableRealtimeUpdates("http://localhost:18000", nil, nil)
```

### 2. Disable Polling When Using SSE

```go
config := enhanced.DefaultOfflineConfig().
    WithAutoRefresh(false) // Disable polling, use SSE instead

manager := enhanced.NewOfflineManager(client, config)
```

### 3. Handle Connection Errors Gracefully

The SSE client automatically reconnects on connection loss with exponential backoff. No action needed!

```go
// SSE client handles reconnection automatically
// Your app continues to work with cached snapshot during reconnection
```

### 4. Always Close Manager on Shutdown

```go
defer manager.Close() // Stops SSE and auto-refresh
```

## Performance

### Latency Comparison

```
Polling (60s interval):     0-60s delay for updates
Real-Time (SSE):            < 1s delay for updates

Improvement: 60x faster notification
```

### Network Usage

```
Polling (60s interval):     1 HTTP request/min = ~1KB/min
Real-Time (SSE):            1 persistent connection = ~100 bytes/update

Server load: 90% reduction (no polling requests)
```

## Comparison: Polling vs Real-Time

| Feature | Polling | Real-Time (SSE) |
|---------|---------|-----------------|
| Update Latency | 0-60s | < 1s |
| Network Usage | Periodic requests | Persistent connection |
| Server Load | High (periodic) | Low (event-driven) |
| Battery Impact | High | Low |
| Scalability | Limited | High |
| Offline Resilience | ✅ Yes | ✅ Yes (auto-reconnect) |

## Troubleshooting

### SSE Connection Fails

**Problem**: `EnableRealtimeUpdates()` returns error or immediately disconnects

**Solutions**:
1. Check server URL is correct (use base URL, not `/api/v1`)
2. Verify server has SSE endpoint enabled at `/api/v1/realtime/sse`
3. Check firewall/proxy allows SSE connections
4. Enable debug logging: `config.WithDebugLogging(true)`

### Updates Not Received

**Problem**: SSE connected but flag changes not reflected

**Reasons**:
1. Event filtering (only subscribed to specific flags)
2. Network issues preventing events
3. Server not publishing events

**Solutions**:
1. Check SSE client is connected: `manager.IsRealtimeEnabled()`
2. Subscribe to all flags: `manager.EnableRealtimeUpdates(baseURL, nil, nil)`
3. Check server logs for event publication
4. Enable debug logging to see received events

### High Reconnection Rate

**Problem**: SSE client constantly reconnecting

**Reasons**:
1. Network instability
2. Server closing connections
3. Load balancer timeout too short

**Solutions**:
1. Increase reconnect delay: `config.WithReconnectDelay(5 * time.Second)`
2. Configure load balancer for long-lived connections (SSE)
3. Check server logs for connection errors

## Examples

### Complete Example with Graceful Shutdown

```go
package main

import (
    "context"
    "fmt"
    "log"
    "os"
    "os/signal"
    "syscall"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
    enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

func main() {
    // Setup
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    manager := enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
    defer manager.Close()
    
    ctx := context.Background()
    
    // Bootstrap
    if err := manager.Bootstrap(ctx, false); err != nil {
        log.Fatal(err)
    }
    
    // Enable SSE
    if err := manager.EnableRealtimeUpdates("http://localhost:18000", nil, nil); err != nil {
        log.Fatal(err)
    }
    
    fmt.Println("Listening for real-time flag updates (Press Ctrl+C to exit)...")
    
    // Graceful shutdown
    sigChan := make(chan os.Signal, 1)
    signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
    
    <-sigChan
    fmt.Println("\nShutting down...")
}
```

### HTTP Server with Real-Time Updates

```go
var manager *enhanced.OfflineManager

func init() {
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    manager = enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
    manager.Bootstrap(context.Background(), false)
    manager.EnableRealtimeUpdates("http://localhost:18000", nil, nil)
}

func checkFeatureHandler(w http.ResponseWriter, r *http.Request) {
    enabled, _ := manager.IsEnabled(r.Context(), "new_feature", "user123", nil)
    json.NewEncoder(w).Encode(map[string]bool{"enabled": enabled})
}
```

## Roadmap

- [ ] WebSocket support (alternative to SSE)
- [ ] Binary protocol for efficiency
- [ ] Server push for targeted clients
- [ ] Event replay from timestamp
- [ ] Snapshot delta updates

## See Also

- [Client-Side Evaluation Guide](CLIENT_SIDE_EVALUATION.md)
- [Go Enhanced SDK README](README.md)
- [SSE Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
