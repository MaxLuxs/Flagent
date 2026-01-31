# Client-Side Evaluation

Client-side evaluation enables your application to evaluate feature flags locally without making API calls to the server. This provides:

- **Offline Support** - App works without network connection
- **Sub-millisecond Latency** - Typical evaluation time < 1ms
- **Reduced Server Load** - 90%+ reduction in API calls
- **Cost Savings** - Lower bandwidth and server costs

## Architecture

```
┌─────────────────┐
│   Application   │
└────────┬────────┘
         │ bootstrap() - once on start
         ▼
┌─────────────────────────┐
│ OfflineFlagentManager   │
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

```kotlin
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.client.infrastructure.ApiClient
import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import java.io.File

// Initialize API clients
val apiClient = ApiClient(basePath = "http://localhost:18000/api/v1")
val exportApi = ExportApi(apiClient)
val flagApi = FlagApi(apiClient)

// Configure offline manager
val config = OfflineFlagentConfig(
    enablePersistence = true,                  // Save snapshots to disk
    storageDir = File("/path/to/cache"),      // Custom cache directory
    autoRefresh = true,                        // Auto-refresh in background
    refreshIntervalMs = 60_000,                // Refresh every 60 seconds
    snapshotTtlMs = 300_000                    // 5 minute TTL
)

// Create manager
val manager = OfflineFlagentManager(
    exportApi = exportApi,
    flagApi = flagApi,
    config = config
)
```

### 2. Bootstrap (once on app start)

```kotlin
// Bootstrap - loads cached snapshot or fetches from server
try {
    manager.bootstrap()
    println("Manager ready for evaluation")
} catch (e: Exception) {
    // Handle bootstrap failure
    println("Bootstrap failed: ${e.message}")
}
```

### 3. Evaluate Flags (local, no API call)

```kotlin
// Simple evaluation
val result = manager.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123"
)

if (result.isEnabled()) {
    // Show new payment flow
    showNewPaymentFlow()
} else {
    // Show legacy flow
    showLegacyPaymentFlow()
}

// Evaluation with context
val result = manager.evaluate(
    flagKey = "premium_feature",
    entityID = "user123",
    entityContext = mapOf(
        "tier" to "premium",
        "region" to "US",
        "age" to "25"
    )
)

// Access variant attachment
val color = result.getAttachmentValue("color")
val config = result.variantAttachment
```

### 4. Batch Evaluation

```kotlin
import com.flagent.enhanced.evaluator.BatchEvaluationRequest

val requests = listOf(
    BatchEvaluationRequest(
        flagKey = "feature_1",
        entityID = "user123",
        entityContext = mapOf("tier" to "premium")
    ),
    BatchEvaluationRequest(
        flagKey = "feature_2",
        entityID = "user123",
        entityContext = mapOf("region" to "US")
    )
)

val results = manager.evaluateBatch(requests)
results.forEach { result ->
    println("${result.flagKey}: ${result.isEnabled()}")
}
```

## Advanced Usage

### Manual Refresh

```kotlin
// Manually refresh snapshot from server
try {
    manager.refresh()
    println("Snapshot refreshed")
} catch (e: Exception) {
    // Handle refresh failure
    println("Refresh failed: ${e.message}")
}
```

### Check Manager Status

```kotlin
// Check if manager is ready
if (manager.isReady()) {
    // Safe to evaluate
}

// Check snapshot age
val ageMs = manager.getSnapshotAge()
println("Snapshot age: ${ageMs}ms")

// Check if snapshot is expired
if (manager.isSnapshotExpired()) {
    println("Snapshot is stale, will refresh on next evaluation")
}
```

### Debug Mode

```kotlin
val result = manager.evaluate(
    flagKey = "test_feature",
    entityID = "user123",
    entityContext = mapOf("tier" to "premium"),
    enableDebug = true
)

// Print debug logs
result.debugLogs.forEach { log ->
    println(log)
}
/*
Output:
Evaluating segment 1 (rank 1)
Segment 1: constraints matched
Segment 1: matched, assigned variant 10
*/
```

### Cleanup

```kotlin
// Clear cached data
manager.clearCache()

// Shutdown manager (stops auto-refresh)
manager.shutdown()
```

## Configuration Options

```kotlin
data class OfflineFlagentConfig(
    // Enable persistent storage (default: true)
    val enablePersistence: Boolean = true,
    
    // Storage directory (default: ~/.flagent)
    val storageDir: File? = null,
    
    // Enable auto-refresh (default: true)
    val autoRefresh: Boolean = true,
    
    // Refresh interval in ms (default: 60 seconds)
    val refreshIntervalMs: Long = 60_000,
    
    // Snapshot TTL in ms (default: 5 minutes)
    val snapshotTtlMs: Long = 300_000,
    
    // Enable debug logging (default: false)
    val enableDebugLogging: Boolean = false
)
```

## Best Practices

### 1. Bootstrap Early

```kotlin
class MyApplication : Application() {
    val flagManager by lazy {
        OfflineFlagentManager(exportApi, flagApi, config)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Bootstrap in background
        lifecycleScope.launch {
            flagManager.bootstrap()
        }
    }
}
```

### 2. Handle Bootstrap Failure

```kotlin
suspend fun bootstrapWithRetry(maxRetries: Int = 3) {
    var attempt = 0
    while (attempt < maxRetries) {
        try {
            manager.bootstrap()
            return
        } catch (e: Exception) {
            attempt++
            if (attempt >= maxRetries) {
                // Use default values or show error
                throw e
            }
            delay(1000 * attempt) // Exponential backoff
        }
    }
}
```

### 3. Evaluate with Fallback

```kotlin
suspend fun isFeatureEnabled(
    flagKey: String,
    entityID: String,
    default: Boolean = false
): Boolean {
    return try {
        val result = manager.evaluate(flagKey, entityID = entityID)
        result.isEnabled()
    } catch (e: Exception) {
        // Fallback to default if evaluation fails
        default
    }
}
```

### 4. Monitor Snapshot Health

```kotlin
// Periodically check snapshot health
lifecycleScope.launch {
    while (isActive) {
        delay(60_000) // Check every minute
        
        val age = manager.getSnapshotAge() ?: continue
        if (age > 600_000) { // 10 minutes
            // Snapshot is very old, might want to alert
            logger.warn("Snapshot is ${age}ms old")
        }
    }
}
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

```kotlin
val manager = FlagentManager(evaluationApi, config)

val result = manager.evaluate(
    flagKey = "new_feature",
    entityID = "user123"
) // 50-200ms API call
```

### After (Client-Side Evaluation)

```kotlin
// One-time setup
val offlineManager = OfflineFlagentManager(exportApi, flagApi, config)
offlineManager.bootstrap() // Once on app start

// Fast, local evaluation
val result = offlineManager.evaluate(
    flagKey = "new_feature",
    entityID = "user123"
) // < 1ms, no API call
```

## Troubleshooting

### Bootstrap fails

**Problem**: `bootstrap()` throws exception

**Solutions**:
1. Check network connection
2. Verify server URL is correct
3. Check if cached snapshot exists (will use if available)
4. Enable debug logging: `config.enableDebugLogging = true`

### Evaluations return stale results

**Problem**: Flag changes not reflected

**Reasons**:
1. Snapshot TTL not expired yet (default 5 minutes)
2. Auto-refresh disabled
3. Network issues preventing refresh

**Solutions**:
1. Reduce TTL: `config.snapshotTtlMs = 60_000` (1 minute)
2. Enable auto-refresh: `config.autoRefresh = true`
3. Force manual refresh: `manager.refresh()`

### High memory usage

**Problem**: App using too much memory

**Solutions**:
1. Reduce snapshot TTL to free memory sooner
2. Disable persistence if not needed: `config.enablePersistence = false`
3. Implement selective flag fetching (future feature)

## Roadmap

- [ ] Delta updates (only fetch changed flags)
- [ ] Selective bootstrapping (fetch specific flags only)
- [ ] Snapshot compression
- [ ] Multi-snapshot support (A/B test different configs)
- [ ] Snapshot pre-warming (predictive pre-fetch)

## See Also

- [Kotlin Enhanced SDK README](README.md)
- [API Documentation](https://maxluxs.github.io/Flagent/api/openapi.yaml)
- [Architecture Documentation](https://maxluxs.github.io/Flagent/#/architecture/backend)
