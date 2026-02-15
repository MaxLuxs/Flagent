# Оптимизации производительности

> [English](performance-optimizations.md) | Русский

## Обзор

Flagent предоставляет множество оптимизаций производительности для обеспечения максимальной скорости evaluation и масштабируемости. Эти оптимизации делают Flagent готовым к работе с высокими нагрузками и большим количеством одновременных запросов.

## Ключевые оптимизации

### 1. Client-Side Evaluation SDK

SDK с поддержкой client-side evaluation позволяет выполнять evaluation на стороне клиента без необходимости обращаться к серверу.

**Преимущества:**
- Снижение нагрузки на сервер
- Нулевая латентность для клиента
- Работа в offline режиме
- Масштабируемость без ограничений сервера

**Архитектура:**

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

**Пример использования:**

```kotlin
// Kotlin SDK
val manager = FlagentManager(
    baseUrl = "https://api.example.com/api/v1",
    enableClientSideEvaluation = true
)

// Evaluation происходит локально
val result = manager.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US", "tier" to "premium")
)
```

**Конфигурация:**

```kotlin
data class FlagentManagerConfig(
    val baseUrl: String,
    val enableClientSideEvaluation: Boolean = true,
    val cacheRefreshInterval: Duration = 60.seconds,
    val cachePreload: Boolean = true
)
```

### 2. Edge Caching

Кэширование на edge-узлах для снижения латентности и нагрузки на центральный сервер.

**Преимущества:**
- Низкая латентность благодаря географическому распределению
- Снижение нагрузки на центральный сервер
- Автоматическая инвалидация при изменениях
- Поддержка CDN (CloudFront, Fastly, Cloudflare)

**Архитектура:**

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

**Конфигурация:**

```yaml
# CDN Configuration
edge_caching:
  enabled: true
  ttl: 60s
  stale_while_revalidate: 300s
  cache_control: "public, max-age=60, stale-while-revalidate=300"
```

### 3. Batch Optimization

Оптимизация batch evaluation для обработки множественных запросов одновременно.

**Преимущества:**
- Снижение overhead на запрос
- Параллельная обработка
- Оптимизация использования ресурсов
- Поддержка больших batch-запросов

**Пример использования:**

```kotlin
// Batch evaluation с оптимизацией
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
// Все evaluation выполняются параллельно
```

**Внутренняя оптимизация:**

- Параллельная обработка entity
- Общий кэш для всех запросов в batch
- Оптимизация database queries
- Batch database operations

### 4. Connection Pooling

Пул соединений для эффективного использования database connections.

**Преимущества:**
- Снижение overhead на установку соединений
- Переиспользование соединений
- Контроль максимального количества соединений
- Мониторинг пула

**Конфигурация (HikariCP):**

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

Оптимизации in-memory кэша для evaluation.

**Текущие оптимизации:**
- ConcurrentHashMap для thread-safe доступа
- Индексация по ID, Key, Tags
- Периодическое обновление без блокировок
- Минимальное время блокировки

**Планируемые улучшения:**

- **Incremental Updates**: Обновление только измененных флагов
- **Lazy Loading**: Загрузка флагов по требованию
- **Compression**: Сжатие данных в памяти
- **Memory-Mapped Files**: Использование off-heap памяти

**Пример конфигурации:**

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

Асинхронная запись данных для снижения impact на evaluation latency.

**Преимущества:**
- Нулевой impact на evaluation latency
- Batch обработка записей
- Retry логика
- Rate limiting

**Архитектура:**

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

**Пример конфигурации:**

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

## Метрики производительности

### Целевые показатели

- **Evaluation Latency (p50)**: < 1ms (client-side), < 5ms (server-side)
- **Evaluation Latency (p99)**: < 10ms (client-side), < 50ms (server-side)
- **Throughput**: > 100 req/s на одном сервере (см. backend PerformanceTest)
- **Cache Hit Rate**: > 95%
- **Batch Processing**: > 1,000 evaluations/second

### Мониторинг

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

## Конфигурация

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

### Фаза 1: Базовые оптимизации (Реализовано)
- ✅ Connection pooling
- ✅ Async data recording
- ✅ EvalCache с индексацией
- ✅ Batch evaluation

### Фаза 2: Client-Side Evaluation (В планах)
- ⏳ Client-side evaluation SDK
- ⏳ Flag synchronization
- ⏳ Offline support
- ⏳ Incremental updates

### Фаза 3: Edge Caching (В планах)
- ⏳ CDN интеграция
- ⏳ Cache invalidation
- ⏳ Stale-while-revalidate
- ⏳ Geodistribution

### Фаза 4: Продвинутые оптимизации (В планах)
- ⏳ Incremental cache updates
- ⏳ Memory compression
- ⏳ Memory-mapped files
- ⏳ Predictive caching

## Примеры использования

### Высоконагруженное приложение

```kotlin
// Конфигурация для высоких нагрузок
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

### Микросервисная архитектура

```kotlin
// Client-side evaluation для микросервисов
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    enableClientSideEvaluation = true,
    cacheRefreshInterval = 30.seconds
)

// Evaluation без network latency
val result = manager.evaluate(
    flagKey = "feature_enabled",
    entityID = serviceId,
    entityContext = mapOf("region" to region)
)
```

## Связанная документация

- [Architecture](../architecture/backend.md)
- [EvalCache Implementation](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/cache/impl/EvalCache.kt)
- [Performance Tests](https://github.com/MaxLuxs/Flagent/tree/main/backend/src/test/kotlin)
