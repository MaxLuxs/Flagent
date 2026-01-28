# Flagent Go Enhanced SDK

Enhanced Go client library for Flagent API with caching, management, and convenient API.

## Features

- ✅ **Client-Side Evaluation**: Offline-first local evaluation (< 1ms latency)
- ✅ **Offline Support**: Works without network connection
- ✅ **Real-Time Updates (SSE)**: Instant flag updates without polling ⭐ **NEW**
- ✅ **Caching**: In-memory cache for evaluation results with configurable TTL
- ✅ **Convenient API**: High-level API for flag evaluation
- ✅ **Auto-Refresh**: Background snapshot updates (optional)
- ✅ **Batch Evaluation**: Support for batch evaluation
- ✅ **Cache Management**: Clear cache, evict expired entries
- ✅ **Thread-Safe**: All operations are thread-safe

## Installation

```bash
go get github.com/MaxLuxs/Flagent/sdk/go-enhanced
```

**Note**: This library depends on the base Flagent Go SDK (`github.com/MaxLuxs/Flagent/sdk/go`).

## Quick Start Guide

### Option 1: Server-Side Evaluation (Traditional)

Best for: Real-time updates, server-controlled flags

### Option 2: Client-Side Evaluation (Recommended) ⭐

Best for: Low latency, offline support, reduced server load

📖 **[Full Client-Side Evaluation Guide](CLIENT_SIDE_EVALUATION.md)**  
📖 **[Real-Time Updates Guide](REALTIME_UPDATES.md)** ⭐ **NEW**

```go
import (
    "context"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
    enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

// Setup (once on app start)
client, _ := flagent.NewClient("http://localhost:18000/api/v1")
manager := enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
defer manager.Close()

ctx := context.Background()
manager.Bootstrap(ctx, false) // Loads snapshot

// Evaluate (local, < 1ms, no API call)
result, _ := manager.Evaluate(ctx, "new_feature", "user123", map[string]interface{}{
    "tier": "premium",
})

if result.IsEnabled() {
    // Feature is enabled
}
```

**Benefits**:
- 🚀 50-200x faster (< 1ms vs 50-200ms)
- 📴 Works offline
- 💰 90%+ server load reduction
- 📦 Persistent caching

---

## Usage

### Server-Side Evaluation (Traditional)

### Basic Setup

```go
import (
    "context"
    "log"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
    enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

func main() {
    // Create base client
    client, err := flagent.NewClient("http://localhost:18000/api/v1")
    if err != nil {
        log.Fatal(err)
    }
    
    // Create enhanced manager with caching
    config := enhanced.DefaultConfig()
    manager := enhanced.NewManager(client, config)
    defer manager.Close()
    
    ctx := context.Background()
    
    // Evaluate a flag (with automatic caching)
    result, err := manager.Evaluate(ctx, "new_feature", "user123", nil)
    if err != nil {
        log.Fatal(err)
    }
    
    if result.IsEnabled() {
        log.Println("Feature is enabled")
    }
}
```

## Usage

### Evaluate a Flag

```go
result, err := manager.Evaluate(
    ctx,
    "new_feature",              // Flag key
    "user123",                  // Entity ID
    map[string]interface{}{     // Entity context
        "region": "US",
        "tier":   "premium",
    },
)

if err != nil {
    log.Fatal(err)
}

log.Printf("Variant: %s", *result.VariantKey)
```

### Check if Flag is Enabled

```go
enabled, err := manager.IsEnabled(ctx, "new_feature", "user123", nil)
if err != nil {
    log.Fatal(err)
}

if enabled {
    // Feature is enabled
}
```

### Get Variant

```go
variant, err := manager.GetVariant(ctx, "experiment_checkout", "user123", nil)
if err != nil {
    log.Fatal(err)
}

switch variant {
case "control":
    // Control variant
case "treatment":
    // Treatment variant
}
```

### Batch Evaluation

```go
entities := []flagent.EvaluationEntity{
    {
        EntityID: "user1",
        EntityContext: map[string]interface{}{"tier": "free"},
    },
    {
        EntityID: "user2",
        EntityContext: map[string]interface{}{"tier": "premium"},
    },
}

results, err := manager.EvaluateBatch(
    ctx,
    []string{"feature_a", "feature_b"},
    entities,
)

for _, result := range results {
    log.Printf("%s for %s: %s", *result.FlagKey, *result.EntityID, *result.VariantKey)
}
```

### Cache Management

```go
// Clear all cached entries
manager.ClearCache()

// Evict expired entries only
manager.EvictExpired()
```

## Configuration

```go
// Default configuration
config := enhanced.DefaultConfig()

// Custom configuration
config := enhanced.DefaultConfig().
    WithCacheTTL(10 * time.Minute).           // Cache TTL (default: 5 minutes)
    WithEnableCache(true).                     // Enable caching (default: true)
    WithDebugLogging(true).                    // Enable debug logging (default: false)
    WithSnapshotRefreshInterval(1 * time.Minute) // Auto-refresh interval (default: disabled)

manager := enhanced.NewManager(client, config)
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `CacheTTL` | `time.Duration` | `5 * time.Minute` | Time-to-live for cached results |
| `EnableCache` | `bool` | `true` | Enable or disable caching |
| `EnableDebugLogging` | `bool` | `false` | Enable debug logging |
| `SnapshotRefreshInterval` | `time.Duration` | `0` (disabled) | Interval for automatic cache refresh |

## Architecture

- **Manager**: Main entry point for enhanced SDK
- **EvaluationCache**: Cache interface for evaluation results
- **InMemoryCache**: Thread-safe in-memory cache implementation
- **Config**: Configuration for enhanced SDK

## Differences from Base SDK

The Enhanced SDK provides:

1. **Automatic caching**: Evaluation results are cached with TTL
2. **Convenient API**: Simpler method signatures with sensible defaults
3. **Cache management**: Clear cache, evict expired entries
4. **Better performance**: Reduced API calls through caching
5. **Auto-refresh**: Optional background cache refresh

The base SDK (`github.com/MaxLuxs/Flagent/sdk/go`) provides low-level API access without caching.

## Advanced Usage

### Custom Cache Implementation

You can implement your own cache by implementing the `EvaluationCache` interface:

```go
type MyCache struct {
    // Your implementation
}

func (c *MyCache) Get(key string) (*flagent.EvaluationResult, bool) {
    // Your implementation
}

func (c *MyCache) Set(key string, result *flagent.EvaluationResult, ttl time.Duration) {
    // Your implementation
}

func (c *MyCache) Delete(key string) {
    // Your implementation
}

func (c *MyCache) Clear() {
    // Your implementation
}

func (c *MyCache) EvictExpired() {
    // Your implementation
}

// Use custom cache
manager := &enhanced.Manager{
    client: client,
    config: config,
    cache:  &MyCache{},
}
```

### Auto-Refresh with Background Updates

```go
config := enhanced.DefaultConfig().
    WithSnapshotRefreshInterval(1 * time.Minute) // Refresh every minute

manager := enhanced.NewManager(client, config)
defer manager.Close() // Important: stops auto-refresh goroutine

// Manager will automatically clear cache every minute
// forcing fresh evaluations from server
```

### Integration with HTTP Server

```go
var flagentManager *enhanced.Manager

func init() {
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    flagentManager = enhanced.NewManager(client, enhanced.DefaultConfig())
}

func checkFeatureHandler(w http.ResponseWriter, r *http.Request) {
    featureName := r.URL.Query().Get("feature")
    userID := r.URL.Query().Get("user_id")
    
    enabled, err := flagentManager.IsEnabled(r.Context(), featureName, userID, nil)
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
    
    json.NewEncoder(w).Encode(map[string]bool{"enabled": enabled})
}
```

## Performance

### Caching Benefits

With default configuration (5-minute TTL):

- **First evaluation**: ~50-200ms (network roundtrip)
- **Cached evaluations**: <1ms (in-memory lookup)
- **Cache hit rate**: 90%+ for typical workloads

### Memory Usage

- Each cached entry: ~500 bytes (varies with context size)
- 10,000 cached entries: ~5 MB memory
- Auto-eviction of expired entries reduces memory footprint

## Best Practices

### 1. Reuse Manager Instances

```go
// Good: Create once, reuse
var flagentManager *enhanced.Manager

func init() {
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    flagentManager = enhanced.NewManager(client, enhanced.DefaultConfig())
}
```

### 2. Use Context for Cancellation

```go
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

result, err := manager.Evaluate(ctx, "feature", "user123", nil)
```

### 3. Handle Errors Gracefully

```go
func safeFeatureCheck(ctx context.Context, flagKey, userID string) bool {
    enabled, err := manager.IsEnabled(ctx, flagKey, userID, nil)
    if err != nil {
        log.Printf("Failed to evaluate flag: %v", err)
        return false // Fallback to default
    }
    return enabled
}
```

### 4. Clean Up Resources

```go
manager := enhanced.NewManager(client, config)
defer manager.Close() // Important: stops auto-refresh if enabled
```

### 5. Monitor Cache Performance

```go
if config.EnableDebugLogging {
    // Logs will show cache hits/misses
    manager.Evaluate(ctx, "feature", "user123", nil)
}
```

## Testing

Run tests:

```bash
go test ./...
```

Run tests with coverage:

```bash
go test -cover ./...
```

Run tests with race detector:

```bash
go test -race ./...
```

## Comparison with Base SDK

| Feature | Base SDK | Enhanced SDK |
|---------|----------|--------------|
| HTTP Client | ✅ | ✅ |
| Evaluation API | ✅ | ✅ |
| Batch Evaluation | ✅ | ✅ |
| Caching | ❌ | ✅ |
| Auto-Refresh | ❌ | ✅ |
| Convenient API | ❌ | ✅ |
| Cache Management | ❌ | ✅ |

## Migration from Base SDK

```go
// Before (Base SDK)
client, _ := flagent.NewClient("http://localhost:18000/api/v1")
result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey:  flagent.StringPtr("feature"),
    EntityID: flagent.StringPtr("user123"),
})

// After (Enhanced SDK)
client, _ := flagent.NewClient("http://localhost:18000/api/v1")
manager := enhanced.NewManager(client, enhanced.DefaultConfig())
result, err := manager.Evaluate(ctx, "feature", "user123", nil)
```

## License

Apache 2.0

## Links

- **Documentation**: https://maxluxs.github.io/Flagent
- **GitHub**: https://github.com/MaxLuxs/Flagent
- **Go Package**: https://pkg.go.dev/github.com/MaxLuxs/Flagent/sdk/go-enhanced
