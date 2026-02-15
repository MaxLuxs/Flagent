# Руководство по настройке производительности

> [English](tuning-guide.md) | Русский

Руководство по оптимизации производительности Flagent на основе результатов нагрузочного тестирования.

**Примечание:** Индексы и таблицы для метрик, аномалий и smart rollout применяются при использовании соответствующих функций (Core metrics или Enterprise). Evaluation и CRUD флагов актуальны для OSS всегда.

## Оптимизация БД

### Индексы

Следующие индексы создаются автоматически для оптимизации запросов:

#### Запросы метрик
```sql
-- Основные запросы по флагу и времени
CREATE INDEX idx_metric_flag_timestamp ON metric_data_points(flag_id, timestamp DESC);
CREATE INDEX idx_metric_flag_key_timestamp ON metric_data_points(flag_key, timestamp DESC);
CREATE INDEX idx_metric_type_timestamp ON metric_data_points(metric_type, timestamp DESC);

-- Запросы по вариантам
CREATE INDEX idx_metric_variant ON metric_data_points(flag_id, variant_id, timestamp DESC) 
WHERE variant_id IS NOT NULL;
```

#### Запросы аномалий
```sql
-- Поиск аномалий
CREATE INDEX idx_anomaly_flag_resolved ON anomaly_alerts(flag_id, resolved, detected_at DESC);
CREATE INDEX idx_anomaly_severity ON anomaly_alerts(severity, resolved, detected_at DESC);

-- Неразрешённые аномалии (частые запросы)
CREATE INDEX idx_anomaly_unresolved ON anomaly_alerts(resolved, detected_at DESC) 
WHERE resolved = false;
```

#### Запросы Smart Rollout
```sql
-- Конфиги rollout
CREATE INDEX idx_rollout_flag_status ON smart_rollout_configs(flag_id, status, enabled);

-- Активные rollouts (фоновые задачи)
CREATE INDEX idx_rollout_active ON smart_rollout_configs(enabled, status, last_increment_at) 
WHERE enabled = true AND status = 'ACTIVE';

-- История
CREATE INDEX idx_rollout_history ON smart_rollout_history(rollout_config_id, changed_at DESC);
```

### Мониторинг индексов

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

### Обслуживание таблиц

**Analyze** (обновить статистику для оптимизатора):
```kotlin
PerformanceOptimization.analyze()
```

**Vacuum** (PostgreSQL, освободить место):
```sql
VACUUM ANALYZE metric_data_points;
VACUUM ANALYZE anomaly_alerts;
VACUUM ANALYZE smart_rollout_configs;
```

**Auto-vacuum** (postgresql.conf):
```
autovacuum = on
autovacuum_max_workers = 3
autovacuum_naptime = 1min
autovacuum_vacuum_threshold = 50
autovacuum_analyze_threshold = 50
```

---

## Пропускная способность Evaluation

### Целевые метрики

- **Throughput:** 1000+ req/s на инстанс (цель: 2000+ req/s)
- **Задержка:** p99 < 10ms, средняя < 1ms для пути evaluation
- **Доля ошибок:** < 1%

### Основная конфигурация

**Обновление EvalCache:**
```bash
# Меньший интервал = свежие данные, выше нагрузка на БД. По умолчанию: 3s
export FLAGENT_EVALCACHE_REFRESHINTERVAL=3s

# Для высокой нагрузки можно 5-10s, если данные меняются редко
export FLAGENT_EVALCACHE_REFRESHINTERVAL=5s
```

**Пул воркеров (Netty):**
```bash
# По умолчанию = число ядер. При I/O-bound увеличьте, если CPU недогружен
export FLAGENT_WORKER_POOL_SIZE=8
```

**Пул БД (для загрузки кэша):**
```bash
export DB_POOL_SIZE=50
export DB_MIN_IDLE=10
```

### Профилирование

Включить pprof для анализа heap/потоков:
```bash
export FLAGENT_PPROF_ENABLED=true
```

Доступ:
- `GET /debug/pprof/heap` — heap dump
- `GET /debug/pprof/thread` — thread dump
- `GET /debug/pprof/profile` — CPU profile (см. ответ для JFR)

**JFR (Java Flight Recorder):**
```bash
java -XX:StartFlightRecording=filename=recording.jfr,duration=60s -jar flagent.jar
```

**Async-profiler (Linux):**
```bash
./profiler.sh -e cpu -d 30 -f flamegraph.svg <pid>
```

### Чек-лист оптимизации

1. **EvalCache:** Прогревайте кэш до трафика; избегайте cold start.
2. **enableDebug:** В production задайте `enableDebug=false` — отладочный путь тяжелее.
3. **Логирование:** Endpoints evaluation по умолчанию исключены из verbose logging.
4. **Сериализация:** Ответ evaluation использует kotlinx.serialization; не добавляйте лишних сериализаторов в hot path.

---

## Настройка пула соединений

### Рекомендуемые значения

**Production (200 одновременных пользователей):**
```bash
export DB_POOL_SIZE=50
export DB_MIN_IDLE=10
```

**Высокая нагрузка (500+ пользователей):**
```bash
export DB_POOL_SIZE=100
export DB_MIN_IDLE=25
```

**Разработка:**
```bash
export DB_POOL_SIZE=10
export DB_MIN_IDLE=2
```

### HikariCP

Рекомендуемые настройки (в `DatabaseConfig.kt`):

```kotlin
maximumPoolSize = 50
minimumIdle = 10
connectionTimeout = 30000
idleTimeout = 600000
maxLifetime = 1800000
validationTimeout = 5000
```

### Формула размера пула

**По CPU:** `connections = (core_count × 2) + effective_spindle_count`  
**По нагрузке:** `connections = expected_concurrent_requests / 2`

Пример: 8 ядер → 17; 200 запросов → 100. **Берите большее значение**, верхняя граница 100.

---

## Производительность запросов

### Медленные запросы (PostgreSQL)

Включить логирование: `log_min_duration_statement = 1000` в postgresql.conf.

Поиск медленных запросов — `pg_stat_statements` (по mean_time).

### Таймауты запросов

В `DatabaseConfig.QueryTimeouts`:
- **SHORT_QUERY_MS**: 1s — простые insert/select
- **MEDIUM_QUERY_MS**: 5s — агрегации
- **LONG_QUERY_MS**: 30s — сложная аналитика

---

## Хранение и очистка данных

### Автоочистка

`AiRolloutScheduler` чистит старые данные. Конфигурация:
```bash
export METRIC_RETENTION_DAYS=90
export ANOMALY_RETENTION_DAYS=180
```

### Ручная очистка

DELETE по `timestamp` / `detected_at` старше 90/180 дней (см. [tuning-guide.md](tuning-guide.md)).

### Партиционирование

Для больших объёмов — партиционирование по timestamp (PostgreSQL 10+).

---

## Кэширование

**EvalCache:** TTL 60s, интервал обновления 3s. Для агрегаций метрик — кэш на 5–15 минут. Опционально Redis для нескольких инстансов.

---

## Результаты нагрузочных тестов

**До оптимизации:** метрики ~50–400ms avg, аномалии ~150–800ms. **После** (индексы + пул): в 2–5 раз быстрее. **Цель в production:** p95 < 200ms, p99 < 500ms, ошибки < 1%, 500+ RPS на инстанс.

---

## Мониторинг

Grafana: `flagent-metrics.json`, `flagent-anomalies.json`. Метрики: пул соединений, длительность запросов, RPS, ошибки. Рекомендуемые алерты Prometheus: высокая доля ошибок, медленные запросы, исчерпание пула.

---

## Чек-листы

**Перед запуском:** индексы, пул, таймауты, retention, дашборды, алерты, нагрузочные тесты.  
**Регулярно:** еженедельно — статистика индексов и медленные запросы; ежемесячно — VACUUM ANALYZE и проверка retention; ежеквартально — повторные нагрузочные тесты и размер пула.  
**Масштабирование:** увеличить пул, read replicas, Redis, партиционирование, CDN, rate limiting по тенанту.

---

## Устранение неполадок

**Высокая загрузка CPU:** отсутствующие индексы, тяжёлые агрегации, избыток соединений.  
**Память:** размер пула, неограниченный кэш, утечки.  
**Медленные запросы:** `\d+ table_name`, `EXPLAIN ANALYZE`, индексы, упрощение запроса.  
**Исчерпание пула:** увеличить `maximumPoolSize`, проверить утечки соединений, таймаут, retry.

---

## Лучшие практики

1. Использовать индексы для запросов по времени  
2. Регулярно мониторить производительность запросов  
3. Задавать таймауты для всех запросов  
4. Политики хранения данных  
5. Batch-операции где возможно  
6. Кэшировать агрегации (TTL 5–15 мин)  
7. ANALYZE после массовых операций  
8. Нагрузочное тестирование до production  
9. Мониторинг пула соединений  
10. Заложить масштабирование с самого начала  

Подробные команды и примеры см. в [English version](tuning-guide.md).
