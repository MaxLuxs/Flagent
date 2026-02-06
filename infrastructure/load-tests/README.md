# Load Testing для AI-Powered Rollouts

Нагрузочное тестирование для проверки производительности AI-powered rollouts функциональности.

## Требования

Установите [k6](https://k6.io/):

```bash
# macOS
brew install k6

# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# Windows (with Chocolatey)
choco install k6
```

## Запуск сервера

Перед запуском тестов убедитесь, что Flagent сервер запущен (from repo root):

```bash
cd backend
./gradlew run
```

Или используйте Docker:

```bash
docker-compose up
```

## Тесты

### 1. Evaluation Load Test

Тестирует производительность Evaluation API (POST /api/v1/evaluation).

**Запуск:**
```bash
k6 run evaluation-load-test.js
```

**С custom параметрами:**
```bash
k6 run -e EVAL_VUS=2000 -e EVAL_DURATION=60s -e BASE_URL=http://localhost:18000 evaluation-load-test.js
```

**Сценарий:**
- Constant load: 200 VUs (по умолчанию), 30s
- Цель: ~2000 req/s, p99 < 100ms, error rate < 1%

**Thresholds:**
- p50 < 5ms, p95 < 50ms, p99 < 100ms
- Error rate < 1%

См. [docs/performance/benchmarks.md](../docs/performance/benchmarks.md) для деталей.

### 2. Metrics Load Test

Тестирует производительность Metrics API под нагрузкой.

**Запуск:**
```bash
k6 run load-tests/metrics-load-test.js
```

**С custom BASE_URL:**
```bash
k6 run -e BASE_URL=http://your-server:8000 load-tests/metrics-load-test.js
```

**Сценарий:**
- Ramp up: 10 → 50 → 100 users (3 минуты)
- Steady state: 100 users (3 минуты)
- Spike: 200 users (3 минуты)
- Ramp down: 0 users (1 минута)

**Типы запросов:**
- 70% - Single metric submission (POST /api/v1/metrics)
- 20% - Batch metric submission (POST /api/v1/metrics/batch)
- 10% - Read operations (GET metrics, aggregations)

**Thresholds:**
- 95% requests < 500ms
- 99% requests < 1000ms
- Error rate < 5%

**Ожидаемая производительность:**
- Single metric: ~10-50ms
- Batch (50 metrics): ~100-300ms
- Get metrics: ~50-150ms
- Get aggregation: ~100-250ms

### 3. Anomaly Detection Load Test

Тестирует производительность anomaly detection системы.

**Запуск:**
```bash
k6 run load-tests/anomaly-detection-load-test.js
```

**Сценарии (параллельные):**

1. **Metric Generation** (20 VUs, 5 минут)
   - Непрерывная генерация метрик
   - 10% аномальных данных (плохие метрики)
   - 1 batch/sec per VU

2. **Anomaly Detection** (5 VUs, 5 минут, старт +30sec)
   - Запуск детекции аномалий
   - 1 detection/5sec per VU
   - Tracking обнаруженных аномалий

3. **Alert Management** (3 VUs, 5 минут, старт +1min)
   - Получение алертов
   - Разрешение алертов
   - Mixed read/write operations

**Thresholds:**
- 95% requests < 2000ms
- 99% requests < 5000ms
- Error rate < 10%
- Detection duration p95 < 1000ms

**Ожидаемая производительность:**
- Anomaly detection: ~200-800ms per flag
- Get alerts: ~50-200ms
- Resolve alert: ~10-50ms

## Анализ результатов

### Метрики K6

После выполнения тестов k6 выведет сводку:

```
checks.........................: 95.00% ✓ 9500  ✗ 500
data_received..................: 150 MB 5.0 MB/s
data_sent......................: 120 MB 4.0 MB/s
http_req_blocked...............: avg=1.2ms  min=0s   med=1ms   max=50ms  p(90)=2ms   p(95)=3ms
http_req_connecting............: avg=500µs  min=0s   med=400µs max=20ms  p(90)=1ms   p(95)=2ms
http_req_duration..............: avg=150ms  min=5ms  med=100ms max=2s    p(90)=300ms p(95)=450ms
http_req_failed................: 2.00%  ✓ 200   ✗ 9800
http_req_receiving.............: avg=2ms    min=0s   med=1ms   max=100ms p(90)=5ms   p(95)=10ms
http_req_sending...............: avg=1ms    min=0s   med=500µs max=50ms  p(90)=2ms   p(95)=5ms
http_req_tls_handshaking.......: avg=0s     min=0s   med=0s    max=0s    p(90)=0s    p(95)=0s
http_req_waiting...............: avg=147ms  min=5ms  med=98ms  max=1.9s  p(90)=295ms p(95)=445ms
http_reqs......................: 10000  333.3/s
iteration_duration.............: avg=1.15s  min=1s   med=1.1s  max=3s    p(90)=1.3s  p(95)=1.5s
iterations.....................: 10000  333.3/s
vus............................: 100    min=10  max=200
vus_max........................: 200    min=200 max=200
```

### Ключевые метрики

**http_req_duration** - время ответа:
- avg - среднее время
- p(95) - 95-й процентиль (95% запросов быстрее)
- p(99) - 99-й процентиль

**http_req_failed** - процент ошибок:
- Должен быть < 5% для metrics API
- Должен быть < 10% для anomaly detection

**http_reqs** - requests per second (RPS):
- Показывает throughput системы

### Custom метрики

**Metrics Load Test:**
- `metric_collection_duration` - время сбора метрики
- `errors` - custom error rate

**Anomaly Detection Load Test:**
- `detection_duration` - время детекции аномалий
- `anomalies_detected` - количество обнаруженных аномалий

## Troubleshooting

### Ошибка: Connection refused
```
ERRO[0001] GoError: Get "http://localhost:8000/api/v1/metrics/1": dial tcp [::1]:8000: connect: connection refused
```

**Решение:** Убедитесь, что сервер запущен на порту 8000.

### Высокий error rate
```
http_req_failed................: 15.00%  ✗ 1500  ✓ 8500
```

**Возможные причины:**
- Сервер перегружен (увеличьте ресурсы или уменьшите нагрузку)
- Database connection pool exhausted (увеличьте pool size)
- Timeout слишком короткий (увеличьте timeout в k6)

**Решение:**
```javascript
// Увеличить timeout
export const options = {
  httpDebug: 'full', // Debug mode
  thresholds: {
    http_req_duration: ['p(95)<2000'], // Более lenient threshold
  },
};
```

### Медленные запросы
```
http_req_duration..............: avg=2.5s  p(95)=5s
```

**Возможные причины:**
- Database не оптимизирован (добавьте индексы)
- Слишком большой объем данных (увеличьте cleanup frequency)
- Недостаточно ресурсов (CPU/Memory)

**Проверьте:**
- Database query performance (EXPLAIN ANALYZE)
- JVM heap usage (если используете JVM)
- Connection pool settings

## Оптимизация производительности

### 1. Database Indexing

Убедитесь, что созданы индексы:
```sql
CREATE INDEX idx_metric_flag_timestamp ON metric_data_points(flag_id, timestamp);
CREATE INDEX idx_metric_flag_key_timestamp ON metric_data_points(flag_key, timestamp);
CREATE INDEX idx_anomaly_flag_resolved ON anomaly_alerts(flag_id, resolved);
```

### 2. Connection Pool

Увеличьте database connection pool:
```kotlin
// In Application.kt or AppConfig
HikariCP {
    maximumPoolSize = 50 // Увеличить для высокой нагрузки
    minimumIdle = 10
}
```

### 3. Caching

Добавьте кэширование для частых запросов:
- Агрегации метрик (TTL 1-5 минут)
- Anomaly detection configs (обновлять при изменении)

### 4. Batch Processing

Используйте batch API везде, где возможно:
```javascript
// Вместо 50 отдельных запросов
for (let i = 0; i < 50; i++) {
  http.post('/api/v1/metrics', metric);
}

// Один batch запрос
http.post('/api/v1/metrics/batch', metrics);
```

## Continuous Load Testing

Интегрируйте в CI/CD:

```yaml
# .github/workflows/load-test.yml
name: Load Test
on: [push]
jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start Flagent
        run: docker-compose up -d
      - name: Wait for server
        run: sleep 30
      - name: Install k6
        run: |
          sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
          echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
          sudo apt-get update
          sudo apt-get install k6
      - name: Run Load Test
        run: k6 run load-tests/metrics-load-test.js
```

## Дополнительные ресурсы

- [K6 Documentation](https://k6.io/docs/)
- [K6 Best Practices](https://k6.io/docs/testing-guides/running-large-tests/)
- [Performance Testing Guide](https://k6.io/docs/testing-guides/automated-performance-testing/)
