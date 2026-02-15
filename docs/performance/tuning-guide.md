# Performance Tuning Guide

Руководство по оптимизации производительности Flagent на основе результатов load testing.

**Note:** Metrics, anomaly, and smart rollout indices/tables apply when using the corresponding features (Core metrics or Enterprise). Evaluation and flag CRUD are always relevant for OSS.

## Database Optimization

### Indices

Автоматически создаются следующие индексы для оптимизации запросов:

#### Metrics Queries
```sql
-- Primary metrics queries (by flag and time)
CREATE INDEX idx_metric_flag_timestamp ON metric_data_points(flag_id, timestamp DESC);
CREATE INDEX idx_metric_flag_key_timestamp ON metric_data_points(flag_key, timestamp DESC);
CREATE INDEX idx_metric_type_timestamp ON metric_data_points(metric_type, timestamp DESC);

-- Variant-specific queries
CREATE INDEX idx_metric_variant ON metric_data_points(flag_id, variant_id, timestamp DESC) 
WHERE variant_id IS NOT NULL;
```

#### Anomaly Queries
```sql
-- Anomaly lookups
CREATE INDEX idx_anomaly_flag_resolved ON anomaly_alerts(flag_id, resolved, detected_at DESC);
CREATE INDEX idx_anomaly_severity ON anomaly_alerts(severity, resolved, detected_at DESC);

-- Unresolved anomalies (hot queries)
CREATE INDEX idx_anomaly_unresolved ON anomaly_alerts(resolved, detected_at DESC) 
WHERE resolved = false;
```

#### Smart Rollout Queries
```sql
-- Rollout config lookups
CREATE INDEX idx_rollout_flag_status ON smart_rollout_configs(flag_id, status, enabled);

-- Active rollouts (background job queries)
CREATE INDEX idx_rollout_active ON smart_rollout_configs(enabled, status, last_increment_at) 
WHERE enabled = true AND status = 'ACTIVE';

-- History queries
CREATE INDEX idx_rollout_history ON smart_rollout_history(rollout_config_id, changed_at DESC);
```

### Index Monitoring

Проверка использования индексов (PostgreSQL):

```sql
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan as scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
AND tablename LIKE '%metric%' OR tablename LIKE '%anomaly%' OR tablename LIKE '%rollout%'
ORDER BY idx_scan DESC;
```

**В коде:**
```kotlin
val stats = PerformanceOptimization.getIndexStats()
stats.forEach { stat ->
    println("${stat.table}.${stat.index}: ${stat.scans} scans")
}
```

### Table Maintenance

**Analyze tables** (обновить статистику для query optimizer):
```kotlin
PerformanceOptimization.analyze()
```

**Vacuum** (PostgreSQL, освободить место):
```sql
VACUUM ANALYZE metric_data_points;
VACUUM ANALYZE anomaly_alerts;
VACUUM ANALYZE smart_rollout_configs;
```

**Auto-vacuum settings** (postgresql.conf):
```
autovacuum = on
autovacuum_max_workers = 3
autovacuum_naptime = 1min
autovacuum_vacuum_threshold = 50
autovacuum_analyze_threshold = 50
```

---

## Evaluation Throughput

### Target Metrics

- **Throughput:** 1000+ req/s per instance (goal: 2000+ req/s)
- **Latency:** p99 < 10ms, mean < 1ms for evaluation-only path
- **Error rate:** < 1%

### Key Configuration

**EvalCache refresh:**
```bash
# Shorter interval = fresher data, more DB load. Default: 3s
export FLAGENT_EVALCACHE_REFRESHINTERVAL=3s

# For high-throughput evaluation, consider 5-10s if data changes infrequently
export FLAGENT_EVALCACHE_REFRESHINTERVAL=5s
```

**Worker pool (Netty):**
```bash
# Default = CPU cores. For I/O-bound evaluation, increase if CPU is underutilized
export FLAGENT_WORKER_POOL_SIZE=8
```

**Database pool (for cache fetcher):**
```bash
export DB_POOL_SIZE=50
export DB_MIN_IDLE=10
```

### Profiling

Enable pprof endpoints for heap/thread analysis:
```bash
export FLAGENT_PPROF_ENABLED=true
```

Then access:
- `GET /debug/pprof/heap` — heap dump
- `GET /debug/pprof/thread` — thread dump
- `GET /debug/pprof/profile` — CPU profile (see response for JFR instructions)

**JFR (Java Flight Recorder) for CPU profiling:**
```bash
java -XX:StartFlightRecording=filename=recording.jfr,duration=60s -jar flagent.jar
```

**Async-profiler (Linux):**
```bash
# Attach to running JVM
./profiler.sh -e cpu -d 30 -f flamegraph.svg <pid>
```

### Optimization Checklist

1. **EvalCache:** Ensure cache is warmed before traffic; avoid cold start
2. **enableDebug:** Set `enableDebug=false` in production — debug path is significantly heavier
3. **Logging:** Evaluation endpoints are excluded from verbose logging by default. To add more paths:
   ```bash
   export FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_EXCLUDE_URLS=/api/v1/evaluation,/api/v1/evaluation/batch,/health
   ```
4. **Serialization:** Evaluation response uses kotlinx.serialization; ensure no custom serializers add overhead in hot path

---

## Connection Pool Tuning

### Recommended Settings

**Production (200 concurrent users):**
```bash
export DB_POOL_SIZE=50
export DB_MIN_IDLE=10
```

**High Load (500+ concurrent users):**
```bash
export DB_POOL_SIZE=100
export DB_MIN_IDLE=25
```

**Development:**
```bash
export DB_POOL_SIZE=10
export DB_MIN_IDLE=2
```

### HikariCP Configuration

Оптимальные настройки для HikariCP (уже настроены в `DatabaseConfig.kt`):

```kotlin
maximumPoolSize = 50           // Max connections
minimumIdle = 10               // Always ready connections
connectionTimeout = 30000      // 30s timeout
idleTimeout = 600000           // 10 min idle timeout
maxLifetime = 1800000          // 30 min connection lifetime
validationTimeout = 5000       // 5s validation timeout
```

### Connection Pool Sizing Formula

**CPU-based:**
```
connections = (core_count × 2) + effective_spindle_count
```

**Load-based (web apps):**
```
connections = expected_concurrent_requests / 2
```

**Example:**
- 8 CPU cores → `(8 × 2) + 1 = 17 connections`
- 200 concurrent requests → `200 / 2 = 100 connections`
- **Recommended: take the higher value**, cap at 100

**В коде:**
```kotlin
val recommended = DatabaseConfig.getRecommendedPoolSize(
    coreCount = 8,
    expectedConcurrentRequests = 200
)
// Returns: 100
```

---

## Query Performance

### Slow Query Detection (PostgreSQL)

Enable slow query logging:
```sql
-- postgresql.conf
log_min_duration_statement = 1000  # Log queries > 1s
```

Find slow queries:
```sql
SELECT
    query,
    calls,
    total_time / 1000 as total_seconds,
    mean_time / 1000 as avg_seconds,
    max_time / 1000 as max_seconds
FROM pg_stat_statements
WHERE query LIKE '%metric%' OR query LIKE '%anomaly%'
ORDER BY mean_time DESC
LIMIT 20;
```

### Query Timeouts

Настроенные timeouts в `DatabaseConfig.QueryTimeouts`:
- **SHORT_QUERY_MS**: 1s - simple inserts/selects
- **MEDIUM_QUERY_MS**: 5s - aggregations
- **LONG_QUERY_MS**: 30s - complex analytics

**Применение:**
```kotlin
transaction {
    queryTimeout = DatabaseConfig.QueryTimeouts.MEDIUM_QUERY_MS.toInt()
    // ... query
}
```

---

## Data Retention & Cleanup

### Automated Cleanup

`AiRolloutScheduler` автоматически чистит старые данные:

```kotlin
// In AiRolloutScheduler
private suspend fun cleanupOldData() {
    val retentionDays = 90 // 90 days retention
    val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
    
    metricsCollectionService.cleanupOldMetrics(cutoffTime)
}
```

**Конфигурация retention:**
```bash
export METRIC_RETENTION_DAYS=90  # Default: 90 days
export ANOMALY_RETENTION_DAYS=180 # Default: 180 days
```

### Manual Cleanup

```sql
-- Delete metrics older than 90 days
DELETE FROM metric_data_points 
WHERE timestamp < (extract(epoch from now() - interval '90 days') * 1000);

-- Delete resolved anomalies older than 180 days
DELETE FROM anomaly_alerts 
WHERE resolved = true 
AND detected_at < (extract(epoch from now() - interval '180 days') * 1000);
```

### Partitioning (Advanced)

Для больших объемов данных (millions of metrics) рекомендуется partitioning по timestamp:

```sql
-- Create partitioned table (PostgreSQL 10+)
CREATE TABLE metric_data_points_partitioned (
    -- same columns as metric_data_points
) PARTITION BY RANGE (timestamp);

-- Create monthly partitions
CREATE TABLE metric_data_points_2024_01 
PARTITION OF metric_data_points_partitioned
FOR VALUES FROM (1704067200000) TO (1706745600000);

-- Auto-create partitions with pg_partman extension
```

---

## Caching Strategy

### In-Memory Cache

**EvalCache** уже реализован для evaluation:
- TTL: 60 seconds
- Refresh interval: 3 seconds
- Cache size: unlimited (production: add LRU eviction)

**Metrics Aggregation Cache** (рекомендация):
```kotlin
// Cache aggregated metrics for 5 minutes
val cache = ConcurrentHashMap<String, CachedAggregation>()

data class CachedAggregation(
    val aggregation: MetricAggregation,
    val timestamp: Long,
    val ttlMs: Long = 300_000 // 5 minutes
) {
    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttlMs
}
```

### Redis Cache (Optional)

Для distributed caching (multi-instance deployment):

```kotlin
// Redis для shared cache между инстансами
val redis = RedisClient.create("redis://localhost:6379")

// Cache metrics aggregations
fun getCachedAggregation(flagId: Int, metricType: String): MetricAggregation? {
    val key = "metrics:agg:$flagId:$metricType"
    val cached = redis.get(key)
    return cached?.let { Json.decodeFromString(it) }
}
```

---

## Evaluation Throughput

Evaluation API (`POST /api/v1/evaluation`) is the hot path. Optimize for low latency and high throughput (target: ~2000 req/s).

**Environment variables:**
- `FLAGENT_EVALCACHE_REFRESHINTERVAL` — cache refresh interval (default 3s). Shorter = fresher data, more DB load.
- `FLAGENT_WORKER_POOL_SIZE` — Netty worker threads (default: CPU cores). Increase for high concurrency (e.g., 8–16 on multi-core).
- `FLAGENT_PPROF_ENABLED` — enable pprof for profiling (default: false). Use with async-profiler or JFR to find bottlenecks.
- `FLAGENT_DB_DBCONNECTIONSTR` — ensure HikariCP pool size is adequate (see Connection Pool Tuning).

**Tips:**
- EvalCache serves evaluation from memory; DB is hit only on refresh.
- `enableDebug=false` avoids extra debug logging overhead (evaluation endpoints are excluded from verbose middleware).
- Profile first: run `evaluation-load-test.js`, then enable pprof to identify hot paths.
- See [benchmarks.md](benchmarks.md) for load test instructions.

---

## Load Test Results & Benchmarks

### Baseline Performance (без оптимизаций)

**Metrics API:**
- Single metric: ~50ms avg, ~150ms p95
- Batch (50 metrics): ~300ms avg, ~800ms p95
- Get metrics: ~200ms avg, ~500ms p95
- Aggregation: ~400ms avg, ~1000ms p95

**Anomaly Detection:**
- Detection: ~800ms avg, ~2000ms p95
- Get alerts: ~150ms avg, ~400ms p95

### After Optimization (индексы + pool tuning)

**Metrics API:**
- Single metric: ~10-20ms avg, ~50ms p95 (**5x faster**)
- Batch (50 metrics): ~100-150ms avg, ~300ms p95 (**2x faster**)
- Get metrics: ~50-100ms avg, ~200ms p95 (**2-4x faster**)
- Aggregation: ~100-200ms avg, ~400ms p95 (**2-4x faster**)

**Anomaly Detection:**
- Detection: ~200-400ms avg, ~800ms p95 (**2-4x faster**)
- Get alerts: ~30-60ms avg, ~150ms p95 (**2-5x faster**)

### Target Performance (production)

- p95 < 200ms для всех API endpoints
- p99 < 500ms
- Error rate < 1%
- Throughput: 500+ RPS per instance

---

## Monitoring & Observability

### Grafana Dashboards

Используйте готовые dashboards:
- `grafana/dashboards/flagent-metrics.json` - Metrics visualization
- `grafana/dashboards/flagent-anomalies.json` - Anomaly alerts

**Запуск:**
```bash
docker-compose -f grafana/docker-compose.grafana.yml up -d
# Open http://localhost:3000 (admin/admin)
```

### Key Metrics to Monitor

**Database:**
- Connection pool usage (`hikaricp_connections_active`)
- Query duration (`query_duration_ms`)
- Slow queries count
- Table sizes

**API:**
- Request duration (p50, p95, p99)
- Error rate
- Throughput (RPS)

**AI Rollouts:**
- Active rollouts count
- Anomalies detected (by severity)
- Metrics collection rate
- Scheduler job duration

### Alerting Rules

**Prometheus alerts** (рекомендация):
```yaml
groups:
  - name: flagent
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          
      # Slow queries
      - alert: SlowQueries
        expr: query_duration_p95 > 1000
        for: 5m
        annotations:
          summary: "Slow database queries detected"
          
      # Connection pool exhaustion
      - alert: ConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        annotations:
          summary: "Connection pool almost exhausted"
```

---

## Production Checklist

### Pre-Launch

- [ ] Database indices created (`PerformanceOptimization.apply()`)
- [ ] Connection pool configured (default 10 in Database.kt; modify `maximumPoolSize` for higher load)
- [ ] Query timeouts configured
- [ ] Data retention policies set
- [ ] Monitoring dashboards deployed
- [ ] Alerting rules configured
- [ ] Load tests passed (p95 < 500ms, error rate < 1%)

### Regular Maintenance

- [ ] Weekly: Check index usage stats
- [ ] Weekly: Review slow query logs
- [ ] Monthly: Analyze tables (`VACUUM ANALYZE`)
- [ ] Monthly: Review data retention and cleanup
- [ ] Quarterly: Re-run load tests
- [ ] Quarterly: Review and adjust connection pool size

### Scaling Checklist

When scaling up:
1. Increase connection pool size (modify `maximumPoolSize` in `Database.kt`)
2. Add read replicas for read-heavy workloads
3. Enable Redis caching for multi-instance deployments
4. Consider database partitioning for large tables
5. Add CDN for static assets
6. Implement rate limiting per tenant

---

## Troubleshooting

### High CPU Usage

**Причины:**
- Missing indices → add missing indices
- Complex aggregations → add caching
- Too many connections → reduce pool size

**Решение:**
```sql
-- Find expensive queries
SELECT query, total_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;
```

### High Memory Usage

**Причины:**
- Connection pool too large
- Cache size unlimited
- Memory leaks

**Решение:**
```bash
# Reduce pool size: modify maximumPoolSize in backend/src/main/kotlin/flagent/repository/Database.kt

# Monitor JVM heap (if using JVM)
jstat -gc <pid> 1000
```

### Slow Queries

**Решение:**
1. Check if indices exist: `\d+ table_name`
2. Explain query: `EXPLAIN ANALYZE SELECT ...`
3. Add missing indices
4. Optimize query (add WHERE clauses, reduce JOIN complexity)

### Connection Pool Exhaustion

**Решение:**
1. Increase pool size: modify `maximumPoolSize` in `Database.kt` (default: 10)
2. Check for connection leaks (monitor `hikaricp_connections_active`)
3. Reduce connection timeout
4. Implement connection retry logic

---

## Best Practices

1. **Always use indices** for timestamp range queries
2. **Monitor query performance** regularly
3. **Set appropriate timeouts** for all queries
4. **Implement data retention** policies
5. **Use batch operations** where possible
6. **Cache aggregations** (5-15 minute TTL)
7. **Analyze tables** after bulk operations
8. **Test under load** before production
9. **Monitor connection pool** usage
10. **Plan for scale** from day one
