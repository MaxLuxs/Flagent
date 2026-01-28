Client-Side Evaluation for Go

Client-side evaluation enables your application to evaluate feature flags locally without making API calls to the server. This provides:

- **Offline Support** - App works without network connection
- **Sub-millisecond Latency** - Typical evaluation time < 1ms (vs 50-200ms server-side)
- **Reduced Server Load** - 90%+ reduction in API calls
- **Cost Savings** - Lower bandwidth and server costs

## Architecture

```
┌─────────────────┐
│   Application   │
└────────┬────────┘
         │ Bootstrap() - once on start
         ▼
┌─────────────────────────┐
│  OfflineManager         │
└────────┬────────────────┘
         │
         ├─► Fetcher ────► Server (periodic refresh)
         │
         ├─► Storage ────► Disk (persistent cache)
         │
         └─► Evaluator ──► Local evaluation (< 1ms)
```

## Quick Start

### 1. Setup

```go
import (
    "context"
    "log"
    "time"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
    enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

// Create base client
client, err := flagent.NewClient("http://localhost:18000/api/v1")
if err != nil {
    log.Fatal(err)
}

// Configure offline manager
config := enhanced.DefaultOfflineConfig().
    WithPersistence(true).                  // Save snapshots to disk
    WithStorageDir("/path/to/cache").       // Custom cache directory
    WithAutoRefresh(true).                  // Auto-refresh in background
    WithRefreshInterval(60 * time.Second).  // Refresh every 60 seconds
    WithSnapshotTTL(5 * time.Minute)        // 5 minute TTL

// Create offline manager
manager := enhanced.NewOfflineManager(client, config)
defer manager.Close() // Important: stops auto-refresh
```

### 2. Bootstrap (once on app start)

```go
ctx := context.Background()

// Bootstrap - loads cached snapshot or fetches from server
if err := manager.Bootstrap(ctx, false); err != nil {
    log.Printf("Bootstrap failed: %v", err)
    // Handle bootstrap failure
}

// Check if ready
if !manager.IsReady() {
    log.Fatal("Manager not ready")
}
```

### 3. Evaluate Flags (local, no API call)

```go
// Simple evaluation
result, err := manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
if err != nil {
    log.Fatal(err)
}

if result.IsEnabled() {
    // Show new payment flow
    showNewPaymentFlow()
} else {
    // Show legacy flow
    showLegacyPaymentFlow()
}

// Evaluation with context
result, err = manager.Evaluate(ctx, "premium_feature", "user123", map[string]interface{}{
    "tier":   "premium",
    "region": "US",
    "age":    25,
})

// Access variant attachment
color := result.GetAttachmentValue("color", "#000000")
config := result.VariantAttachment
```

### 4. Batch Evaluation

```go
flagKey1 := "feature_1"
flagKey2 := "feature_2"

requests := []*enhanced.OfflineEvaluationRequest{
    {
        FlagKey:  &flagKey1,
        EntityID: "user123",
        EntityContext: map[string]interface{}{
            "tier": "premium",
        },
    },
    {
        FlagKey:  &flagKey2,
        EntityID: "user123",
        EntityContext: map[string]interface{}{
            "region": "US",
        },
    },
}

results, err := manager.EvaluateBatch(ctx, requests)
if err != nil {
    log.Fatal(err)
}

for _, result := range results {
    fmt.Printf("%s: %v\n", *result.FlagKey, result.IsEnabled())
}
```

## Advanced Usage

### Manual Refresh

```go
// Manually refresh snapshot from server
if err := manager.Refresh(ctx); err != nil {
    log.Printf("Refresh failed: %v", err)
}
```

### Check Manager Status

```go
// Check if manager is ready
if manager.IsReady() {
    // Safe to evaluate
}

// Check snapshot age
age, err := manager.GetSnapshotAge()
if err == nil {
    fmt.Printf("Snapshot age: %dms\n", age)
}

// Check if snapshot is expired
if manager.IsSnapshotExpired() {
    fmt.Println("Snapshot is stale, will refresh on next evaluation")
}
```

### Debug Mode

```go
config := enhanced.DefaultOfflineConfig().
    WithDebugLogging(true)

manager := enhanced.NewOfflineManager(client, config)

// Debug logs will be printed to stdout
result, _ := manager.Evaluate(ctx, "test_feature", "user123", map[string]interface{}{
    "tier": "premium",
})

// Access debug logs
for _, log := range result.DebugLogs {
    fmt.Println(log)
}
/*
Output:
Evaluating segment 1 (rank 1)
Segment 1: constraints matched
Segment 1: matched, assigned variant 10
*/
```

### Cleanup

```go
// Clear cached data
if err := manager.ClearCache(); err != nil {
    log.Printf("Failed to clear cache: %v", err)
}

// Shutdown manager (stops auto-refresh)
manager.Close()
```

## Configuration Options

```go
type OfflineConfig struct {
    // Enable persistent storage (default: true)
    EnablePersistence bool
    
    // Storage directory (default: ~/.flagent)
    StorageDir string
    
    // Enable auto-refresh (default: true)
    AutoRefresh bool
    
    // Refresh interval (default: 60 seconds)
    RefreshInterval time.Duration
    
    // Snapshot TTL (default: 5 minutes)
    SnapshotTTL time.Duration
    
    // Enable debug logging (default: false)
    EnableDebugLogging bool
}
```

## Best Practices

### 1. Bootstrap Early

```go
// In main() or init()
var flagManager *enhanced.OfflineManager

func init() {
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    flagManager = enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
    
    // Bootstrap in background
    go func() {
        ctx := context.Background()
        if err := flagManager.Bootstrap(ctx, false); err != nil {
            log.Printf("Bootstrap failed: %v", err)
        }
    }()
}
```

### 2. Handle Bootstrap Failure

```go
func bootstrapWithRetry(ctx context.Context, manager *enhanced.OfflineManager, maxRetries int) error {
    for attempt := 1; attempt <= maxRetries; attempt++ {
        if err := manager.Bootstrap(ctx, false); err != nil {
            if attempt >= maxRetries {
                return err
            }
            time.Sleep(time.Second * time.Duration(attempt)) // Exponential backoff
            continue
        }
        return nil
    }
    return fmt.Errorf("max retries exceeded")
}
```

### 3. Evaluate with Fallback

```go
func isFeatureEnabled(ctx context.Context, flagKey, entityID string, defaultValue bool) bool {
    result, err := manager.Evaluate(ctx, flagKey, entityID, nil)
    if err != nil {
        log.Printf("Evaluation failed: %v", err)
        return defaultValue // Fallback to default
    }
    return result.IsEnabled()
}
```

### 4. Monitor Snapshot Health

```go
// Periodically check snapshot health
go func() {
    ticker := time.NewTicker(60 * time.Second)
    defer ticker.Stop()
    
    for range ticker.C {
        age, err := manager.GetSnapshotAge()
        if err != nil {
            continue
        }
        
        if age > 10*60*1000 { // 10 minutes
            log.Printf("WARNING: Snapshot is %dms old", age)
        }
    }
}()
```

## Performance

### Evaluation Latency

```
Local Evaluation (client-side):  < 1ms
Server Evaluation (API call):    50-200ms

Speed improvement: 50-200x faster
```

### Server Load Reduction

```
Without client-side eval: 1000 evaluations/sec = 1000 API calls/sec
With client-side eval:    1000 evaluations/sec = 1-2 API calls/min (refresh)

Load reduction: >99%
```

### Memory Usage

```
Snapshot size: 
- 100 flags: ~50KB
- 1000 flags: ~500KB
- 10000 flags: ~5MB

Typical usage: < 1MB
```

## Comparison: Server vs Client-Side Evaluation

| Feature | Server Evaluation | Client-Side Evaluation |
|---------|-------------------|------------------------|
| Latency | 50-200ms | < 1ms |
| Offline Support | ❌ No | ✅ Yes |
| Server Load | High | Low (refresh only) |
| Network Usage | High | Low |
| Consistency | Immediate | Eventual (TTL-based) |
| Security | Higher | Lower (config exposed) |

## Migration from Server Evaluation

### Before (Server Evaluation)

```go
manager := enhanced.NewManager(client, enhanced.DefaultConfig())

// 50-200ms API call per evaluation
result, err := manager.Evaluate(ctx, "new_feature", "user123", nil)
```

### After (Client-Side Evaluation)

```go
// One-time setup
offlineManager := enhanced.NewOfflineManager(client, enhanced.DefaultOfflineConfig())
offlineManager.Bootstrap(ctx, false) // Once on app start

// < 1ms, no API call
result, err := offlineManager.Evaluate(ctx, "new_feature", "user123", nil)
```

## Troubleshooting

### Bootstrap fails

**Problem**: `Bootstrap()` returns error

**Solutions**:
1. Check network connection
2. Verify server URL is correct
3. Check if cached snapshot exists (will use if available)
4. Enable debug logging: `config.WithDebugLogging(true)`

### Evaluations return stale results

**Problem**: Flag changes not reflected

**Reasons**:
1. Snapshot TTL not expired yet (default 5 minutes)
2. Auto-refresh disabled
3. Network issues preventing refresh

**Solutions**:
1. Reduce TTL: `config.WithSnapshotTTL(1 * time.Minute)`
2. Enable auto-refresh: `config.WithAutoRefresh(true)`
3. Force manual refresh: `manager.Refresh(ctx)`

### High memory usage

**Problem**: App using too much memory

**Solutions**:
1. Reduce snapshot TTL to free memory sooner
2. Disable persistence if not needed: `config.WithPersistence(false)`
3. Implement selective flag fetching (future feature)

## Storage

### File Storage (Default)

```go
config := enhanced.DefaultOfflineConfig().
    WithPersistence(true).
    WithStorageDir("/var/cache/flagent")

// Snapshots saved to: /var/cache/flagent/snapshot.json
```

### In-Memory Storage

```go
config := enhanced.DefaultOfflineConfig().
    WithPersistence(false)

// Snapshots only in memory (lost on restart)
```

## Supported Constraint Operators

- `EQ` - Equal
- `NEQ` - Not equal
- `LT` - Less than
- `LTE` - Less than or equal
- `GT` - Greater than
- `GTE` - Greater than or equal
- `IN` - In list (comma-separated)
- `NOTIN` - Not in list
- `CONTAINS` - Contains substring
- `NOTCONTAINS` - Does not contain substring
- `EREG` - Matches regex
- `NEREG` - Does not match regex

## Roadmap

- [ ] Delta updates (only fetch changed flags)
- [ ] Selective bootstrapping (fetch specific flags only)
- [ ] Snapshot compression
- [ ] Multi-snapshot support (A/B test different configs)
- [ ] Snapshot pre-warming (predictive pre-fetch)
- [ ] Real-time updates via WebSocket/SSE

## See Also

- [Go Enhanced SDK README](README.md)
- [API Documentation](../../docs/api/openapi.yaml)
- [Architecture Documentation](../../docs/architecture/backend.md)
