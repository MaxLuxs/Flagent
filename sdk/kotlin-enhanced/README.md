# Flagent Kotlin Enhanced SDK

Enhanced Kotlin client library for Flagent API with caching, management, and convenient API.

## Features

- **Client-Side Evaluation**: Local evaluation without API calls (< 1ms latency) ⭐ **NEW**
- **Offline Support**: Works without network connection ⭐ **NEW**
- **Caching**: In-memory cache for evaluation results with configurable TTL
- **Convenient API**: High-level API for flag evaluation
- **Batch Evaluation**: Support for batch evaluation
- **Cache Management**: Clear cache, evict expired entries
- **Auto-Refresh**: Background snapshot updates ⭐ **NEW**

## Installation

```kotlin
dependencies {
    implementation("com.flagent:kotlin-enhanced:0.1.6")
}
```

**Note**: This library depends on the base Flagent Kotlin SDK (`com.flagent:kotlin-client`).

## Quick Start (recommended)

Use **`Flagent.builder()`** as the single entry point. No need to deal with EvaluationApi, ExportApi, or FlagentManager vs OfflineFlagentManager.

### Server-side (HTTP evaluation)

```kotlin
import com.flagent.enhanced.entry.Flagent
import com.flagent.enhanced.entry.FlagentClient

val client: FlagentClient = Flagent.builder()
    .baseUrl("https://api.example.com/api/v1")
    .cache(true, ttlMs = 5 * 60 * 1000L)
    .build()

val result = client.evaluate(flagKey = "new_feature", entityID = "user123")
if (client.isEnabled("new_feature", "user123")) {
    // feature is on
}
```

### Offline / client-side (local snapshot evaluation)

```kotlin
import com.flagent.enhanced.entry.Flagent
import com.flagent.enhanced.entry.FlagentMode

val client = Flagent.builder()
    .baseUrl("https://api.example.com/api/v1")
    .mode(FlagentMode.OFFLINE)
    .build()

client.initialize() // load snapshot once
val result = client.evaluate(flagKey = "new_feature", entityID = "user123")
```

📖 **[Full Client-Side Evaluation Guide](CLIENT_SIDE_EVALUATION.md)**

---

## Usage

### Builder options

| Method | Description |
|--------|-------------|
| `baseUrl(url)` | API base URL (required) |
| `httpClientEngine(engine)` | Custom HTTP engine (optional) |
| `auth { api -> api.setBearerToken("...") }` | Auth (optional) |
| `cache(enable, ttlMs)` | Evaluation cache (default: on) |
| `mode(FlagentMode.SERVER \| OFFLINE)` | Server or local evaluation |
| `offlineConfig(config)` | Offline config (snapshot TTL, auto-refresh, etc.) |

### Evaluate and isEnabled

```kotlin
val result = client.evaluate(
    flagKey = "new_feature",
    entityID = "user123",
    entityType = "user",
    entityContext = mapOf("region" to "US", "tier" to "premium")
)
println("Variant: ${result.variantKey}")

if (client.isEnabled("new_feature", "user123")) { ... }
```

### From Java (blocking API)

Use `buildBlocking()` to get a blocking client (no coroutines):

```java
import com.flagent.enhanced.entry.Flagent;
import com.flagent.enhanced.entry.FlagentClientBlocking;

FlagentClientBlocking client = Flagent.INSTANCE.builder()
    .baseUrl("https://api.example.com/api/v1")
    .cache(true, 300_000L)
    .buildBlocking();

EvalResult result = client.evaluate("new_feature", null, "user123", null, null, false);
boolean on = client.isEnabled("new_feature", "user123", null, null);
```

See [Java client README](../java/README.md) for dependency and Spring Boot option.

### Batch evaluation

```kotlin
import com.flagent.client.models.EvaluationEntity

val entities = listOf(
    EvaluationEntity(entityID = "user123", entityType = "user"),
    EvaluationEntity(entityID = "user456", entityType = "user")
)
val results = client.evaluateBatch(
    flagKeys = listOf("feature_a", "feature_b"),
    entities = entities
)
```

---

## Advanced: direct manager access

For cache control, bootstrap, realtime, etc., you can still create managers manually.

### Server-side (FlagentManager)

```kotlin
import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.enhanced.config.FlagentConfig

val evaluationApi = EvaluationApi(baseUrl = "https://api.example.com/api/v1")
val manager = FlagentManager(evaluationApi, FlagentConfig(enableCache = true))
val result = manager.evaluate(flagKey = "new_feature", entityID = "user123")
manager.clearCache()
```

### Offline (OfflineFlagentManager)

```kotlin
import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.config.OfflineFlagentConfig

val exportApi = ExportApi(baseUrl = baseUrl)
val flagApi = FlagApi(baseUrl = baseUrl)
val manager = OfflineFlagentManager(exportApi, flagApi, OfflineFlagentConfig())
manager.bootstrap()
val result = manager.evaluate(flagKey = "new_feature", entityID = "user123")
```

## Configuration

- **FlagentConfig** — cache (cacheTtlMs, enableCache) for server-side.
- **OfflineFlagentConfig** — snapshot (snapshotTtlMs, autoRefresh, storagePath, etc.) for offline.

## Architecture

- **Flagent** + **FlagentClient** — recommended entry (builder → evaluate / isEnabled / evaluateBatch).
- **FlagentManager** / **OfflineFlagentManager** — advanced (cache, bootstrap, realtime).
- **EvaluationCache**, **FlagentConfig**, **OfflineFlagentConfig** — configuration and caches.

## Differences from Base SDK

The Enhanced SDK provides:

1. **Automatic caching**: Evaluation results are cached with TTL
2. **Convenient API**: Simpler method signatures
3. **Cache management**: Clear cache, evict expired entries
4. **Better performance**: Reduced API calls through caching

The base SDK (`com.flagent:kotlin-client`) provides low-level API access without caching.