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
    implementation("com.flagent:kotlin-enhanced:0.1.5")
}
```

**Note**: This library depends on the base Flagent Kotlin SDK (`com.flagent:kotlin-client`).

## Quick Start Guide

### Option 1: Server-Side Evaluation (Traditional)

Best for: Real-time updates, server-controlled flags

### Option 2: Client-Side Evaluation (Recommended) ⭐

Best for: Low latency, offline support, reduced server load

📖 **[Full Client-Side Evaluation Guide](CLIENT_SIDE_EVALUATION.md)**

```kotlin
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.config.OfflineFlagentConfig

// Setup (once on app start)
val manager = OfflineFlagentManager(exportApi, flagApi, config)
manager.bootstrap() // Loads snapshot

// Evaluate (local, < 1ms, no API call)
val result = manager.evaluate(
    flagKey = "new_feature",
    entityID = "user123",
    entityContext = mapOf("tier" to "premium")
)

if (result.isEnabled()) {
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

```kotlin
import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.enhanced.config.FlagentConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*

// Create base API client
val httpClient = HttpClient(CIO)
val evaluationApi = EvaluationApi(
    baseUrl = "https://api.example.com/api/v1",
    httpClientEngine = httpClient.engine
)

// Create enhanced manager with caching
val config = FlagentConfig(
    cacheTtlMs = 5 * 60 * 1000L, // 5 minutes
    enableCache = true
)

val manager = FlagentManager(evaluationApi, config)
```

### Evaluate a Flag

```kotlin
// Single evaluation with caching
val result = manager.evaluate(
    flagKey = "new_feature",
    entityID = "user123",
    entityType = "user",
    entityContext = mapOf(
        "region" to "US",
        "tier" to "premium"
    )
)

println("Variant: ${result.variantKey}")
```

### Batch Evaluation

```kotlin
import com.flagent.client.models.EvaluationEntity

val entities = listOf(
    EvaluationEntity(
        entityID = "user123",
        entityType = "user",
        entityContext = mapOf("region" to "US")
    ),
    EvaluationEntity(
        entityID = "user456",
        entityType = "user",
        entityContext = mapOf("region" to "EU")
    )
)

val results = manager.evaluateBatch(
    flagKeys = listOf("feature_a", "feature_b"),
    entities = entities
)

results.forEach { result ->
    println("${result.flagKey}: ${result.variantKey}")
}
```

### Cache Management

```kotlin
// Clear all cached entries
manager.clearCache()

// Evict expired entries
manager.evictExpired()
```

## Configuration

```kotlin
data class FlagentConfig(
    // Cache TTL in milliseconds (default: 5 minutes)
    val cacheTtlMs: Long = 5 * 60 * 1000L,
    
    // Enable caching (default: true)
    val enableCache: Boolean = true,
    
    // Enable debug logging (default: false)
    val enableDebugLogging: Boolean = false
)
```

## Architecture

- **FlagentManager**: Main entry point for enhanced SDK
- **EvaluationCache**: Cache interface for evaluation results
- **InMemoryEvaluationCache**: Thread-safe in-memory cache implementation
- **FlagentConfig**: Configuration for enhanced SDK

## Differences from Base SDK

The Enhanced SDK provides:

1. **Automatic caching**: Evaluation results are cached with TTL
2. **Convenient API**: Simpler method signatures
3. **Cache management**: Clear cache, evict expired entries
4. **Better performance**: Reduced API calls through caching

The base SDK (`com.flagent:kotlin-client`) provides low-level API access without caching.