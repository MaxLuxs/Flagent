# Performance Optimizations

> [English](performance-optimizations.md)

## Overview

Flagent provides a range of performance optimizations for maximum evaluation speed and scalability. These optimizations make Flagent ready for high load and many concurrent requests.

## Key Optimizations

### 1. Client-Side Evaluation SDK

SDKs with client-side evaluation run evaluations on the client without calling the server.

**Benefits:**
- Lower server load
- Zero latency for the client
- Offline operation
- Scalability independent of server capacity

**Architecture:**

```
Client Application
    ↓
Flagent SDK (Client-Side)
    ↓
Local Cache (Flags + Rules)
    ↓
Evaluation Engine
    ↓
Result (instant)
```

**Example:**

```kotlin
// Kotlin SDK
val manager = FlagentManager(
    baseUrl = "https://api.example.com/api/v1",
    enableClientSideEvaluation = true
)

// Evaluation runs locally
val result = manager.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US", "tier" to "premium")
)
```

**Configuration:**

```kotlin
data class FlagentManagerConfig(
    val baseUrl: String,
    val enableClientSideEvaluation: Boolean = true,
    val cacheRefreshInterval: Duration = 60.seconds,
    val cachePreload: Boolean = true
)
```

### 2. Edge Caching

Caching at edge nodes to reduce latency and load on the origin server.

**Benefits:**
- Low latency via geographic distribution
- Reduced load on the origin server
- Automatic invalidation on changes
- CDN support (CloudFront, Fastly, Cloudflare)

**Architecture:**

```
Client Request
    ↓
Edge Cache (CDN)
    ↓ (cache miss)
Origin Server (Flagent)
    ↓
Edge Cache (store)
    ↓
Client Response
```

**Configuration:**

```yaml
# CDN Configuration
edge_caching:
  enabled: true
  ttl: 60s
  stale_while_revalidate: 300s
  cache_control: "public, max-age=60, stale-while-revalidate=300"
```

### 3. Batch Optimization

Optimized batch evaluation for processing multiple requests at once.

**Benefits:**
- Lower per-request overhead
- Parallel processing
- Better resource utilization
- Support for large batch requests

**Example:**

```kotlin
// Batch evaluation
val results = manager.evaluateBatch(
    entities = listOf(
        EvaluationEntity(
            entityID = "user1",
            entityContext = mapOf("country" to "US")
        ),
        EvaluationEntity(
            entityID = "user2",
            entityContext = mapOf("country" to "CA")
        )
    ),
    flagKeys = listOf("flag1", "flag2", "flag3")
)
// All evaluations run in parallel
```

**Internal optimizations:**

- Parallel entity processing
- Shared cache for batch requests
- Optimized database queries
- Batch database operations

### 4. Connection Pooling

Connection pooling for efficient use of database connections.

**Benefits:**
- Lower connection setup overhead
- Connection reuse
- Configurable max connections
- Pool monitoring

**Configuration (HikariCP):**

```kotlin
data class DatabaseConfig(
    val driver: String = "org.postgresql.Driver",
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 5,
    val connectionTimeout: Duration = 30.seconds,
    val idleTimeout: Duration = 10.minutes,
    val maxLifetime: Duration = 30.minutes
)
```

### 5. EvalCache Optimizations

In-memory cache optimizations for evaluation.

**Current optimizations:**
- ConcurrentHashMap for thread-safe access
- Indexing by ID, Key, Tags
- Non-blocking periodic refresh
- Minimal lock time

**Planned improvements:**

- **Incremental Updates**: Update only changed flags
- **Lazy Loading**: Load flags on demand
- **Compression**: In-memory data compression
- **Memory-Mapped Files**: Off-heap storage

**Example configuration:**

```kotlin
data class EvalCacheConfig(
    val refreshInterval: Duration = 3.seconds,
    val refreshTimeout: Duration = 10.seconds,
    val enableIncrementalUpdates: Boolean = true,
    val enableCompression: Boolean = false,
    val maxCacheSize: Long = 100.megabytes
)
```

### 6. Async Data Recording

Asynchronous data recording to avoid impacting evaluation latency.

**Benefits:**
- No impact on evaluation latency
- Batched writes
- Retry logic
- Rate limiting

**Architecture:**

```
Evaluation Request
    ↓
Evaluation Service
    ↓ (async)
Data Recording Channel
    ↓
Batch Processor
    ↓
Kafka/Kinesis/PubSub
```

**Example configuration:**

```kotlin
data class DataRecordingConfig(
    val enabled: Boolean = true,
    val async: Boolean = true,
    val batchSize: Int = 100,
    val batchTimeout: Duration = 1.second,
    val retryAttempts: Int = 3,
    val retryDelay: Duration = 100.milliseconds
)
```

## Performance Metrics

### Targets

- **Evaluation Latency (p50)**: < 1ms (client-side), < 5ms (server-side)
- **Evaluation Latency (p99)**: < 10ms (client-side), < 50ms (server-side)
- **Throughput**: > 100 req/s per server (see backend PerformanceTest)
- **Cache Hit Rate**: > 95%
- **Batch Processing**: > 1,000 evaluations/second

### Monitoring

```kotlin
// Prometheus metrics
val evaluationLatency = Histogram.build()
    .name("flagent_evaluation_latency_seconds")
    .help("Evaluation latency in seconds")
    .register()

val cacheHitRate = Gauge.build()
    .name("flagent_cache_hit_rate")
    .help("Cache hit rate")
    .register()

val throughput = Counter.build()
    .name("flagent_evaluations_total")
    .help("Total number of evaluations")
    .register()
```

## Configuration

### Environment Variables

```bash
# EvalCache
FLAGENT_EVAL_CACHE_REFRESH_INTERVAL=3s
FLAGENT_EVAL_CACHE_REFRESH_TIMEOUT=10s
FLAGENT_EVAL_CACHE_ENABLE_INCREMENTAL_UPDATES=true

# Connection Pooling
FLAGENT_DB_MAX_POOL_SIZE=10
FLAGENT_DB_MIN_IDLE=5
FLAGENT_DB_CONNECTION_TIMEOUT=30s

# Data Recording
FLAGENT_RECORDER_ASYNC=true
FLAGENT_RECORDER_BATCH_SIZE=100
FLAGENT_RECORDER_BATCH_TIMEOUT=1s

# Client-Side Evaluation
FLAGENT_CLIENT_SIDE_EVAL_ENABLED=true
FLAGENT_CLIENT_SIDE_EVAL_CACHE_TTL=60s
```

## Roadmap

### Phase 1: Core optimizations (Done)
- ✅ Connection pooling
- ✅ Async data recording
- ✅ EvalCache with indexing
- ✅ Batch evaluation

### Phase 2: Client-Side Evaluation (Planned)
- ⏳ Client-side evaluation SDK
- ⏳ Flag synchronization
- ⏳ Offline support
- ⏳ Incremental updates

### Phase 3: Edge Caching (Planned)
- ⏳ CDN integration
- ⏳ Cache invalidation
- ⏳ Stale-while-revalidate
- ⏳ Geodistribution

### Phase 4: Advanced optimizations (Planned)
- ⏳ Incremental cache updates
- ⏳ Memory compression
- ⏳ Memory-mapped files
- ⏳ Predictive caching

## Usage Examples

### High-load application

```kotlin
// High-load configuration
val config = FlagentConfig(
    evalCache = EvalCacheConfig(
        refreshInterval = 1.second,
        enableIncrementalUpdates = true
    ),
    database = DatabaseConfig(
        maximumPoolSize = 50,
        minimumIdle = 20
    ),
    recording = DataRecordingConfig(
        async = true,
        batchSize = 500,
        batchTimeout = 500.milliseconds
    )
)
```

### Microservices

```kotlin
// Client-side evaluation for microservices
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    enableClientSideEvaluation = true,
    cacheRefreshInterval = 30.seconds
)

// Evaluation without network latency
val result = manager.evaluate(
    flagKey = "feature_enabled",
    entityID = serviceId,
    entityContext = mapOf("region" to region)
)
```

## Related documentation

- [Architecture](../architecture/backend.md)
- [EvalCache Implementation](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/cache/impl/EvalCache.kt)
- [Performance Tests](https://github.com/MaxLuxs/Flagent/tree/main/backend/src/test/kotlin)
