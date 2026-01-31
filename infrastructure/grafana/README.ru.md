# Grafana Dashboards для Flagent

> [English](README.md) | Русский

Готовые Grafana dashboards для мониторинга AI-Powered Rollouts.

## Quick Start

### 1. Запуск Grafana

```bash
# С Docker Compose
docker-compose -f grafana/docker-compose.grafana.yml up -d

# Или standalone
docker run -d \
  --name=grafana \
  -p 3000:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -v $(pwd)/grafana/provisioning:/etc/grafana/provisioning \
  -v $(pwd)/grafana/dashboards:/etc/grafana/provisioning/dashboards \
  grafana/grafana:10.0.0
```

### 2. Открыть Grafana

```
URL: http://localhost:3000
Username: admin
Password: admin (или значение из GRAFANA_ADMIN_PASSWORD)
```

### 3. Настроить Data Source

Grafana автоматически настроит PostgreSQL data source через provisioning.

**Ручная настройка (если нужно):**
1. Configuration → Data Sources → Add data source
2. Выберите PostgreSQL
3. Настройки:
   - Host: `postgres:5432` (или `localhost:5432` для local)
   - Database: `flagent`
   - User: `flagent`
   - Password: `flagent`
   - SSL Mode: `disable`
4. Save & Test

## Dashboards

### 1. Flagent - Metrics Overview

**UID:** `flagent-metrics`

**Панели:**
1. **Success Rate by Flag** - тренд success rate по флагам
2. **Error Rate by Flag** - тренд error rate (threshold: 5%)
3. **Latency by Flag** - avg, p95, max latency (threshold: 1000ms)
4. **Top 10 Flags by Metric Volume** - самые активные флаги
5. **Unresolved Anomalies** - gauge (last hour)
6. **Active Smart Rollouts** - gauge
7. **Total Metrics** - счётчик (last hour)
8. **Active Flags** - distinct flags (last hour)

**Time Range:** Last 6 hours (default)
**Refresh:** 30 seconds

**Variables:**
- None (можно добавить `$flag_id` для фильтрации)

**Alerts:**
- High Error Rate (>5%)
- High Latency (>1000ms)

### 2. Flagent - Anomaly Alerts

**UID:** `flagent-anomalies`

**Панели:**
1. **Anomalies by Severity** - pie chart (CRITICAL/HIGH/MEDIUM/LOW)
2. **Anomalies by Type** - donut chart (types distribution)
3. **Anomaly Detection Timeline** - stacked bar chart по времени
4. **Recent Anomaly Alerts** - table с последними 100 alerts
5. **Critical Anomalies (Unresolved)** - gauge
6. **Actions Taken** - pie chart (actions distribution)
7. **Resolution Rate** - gauge (resolved / total)

**Time Range:** Last 24 hours (default)
**Refresh:** 30 seconds

**Table Columns:**
- Flag - флаг с аномалией
- Type - тип аномалии
- Severity - критичность (color-coded)
- Message - описание
- Action - действие (FLAG_DISABLED, ROLLOUT_PAUSED, etc.)
- Status - OPEN/RESOLVED (color-coded)
- Detected - timestamp (relative time)

## Queries

### Metrics Queries

**Success Rate:**
```sql
SELECT
  time_bucket('5 minutes', to_timestamp(timestamp / 1000)) AS time,
  flag_key,
  AVG(metric_value) AS value
FROM metric_data_points
WHERE metric_type = 'SUCCESS_RATE'
  AND timestamp >= extract(epoch from now() - interval '$__range')::bigint * 1000
GROUP BY time, flag_key
ORDER BY time
```

**Error Rate:**
```sql
SELECT
  time_bucket('5 minutes', to_timestamp(timestamp / 1000)) AS time,
  flag_key,
  AVG(metric_value) AS value
FROM metric_data_points
WHERE metric_type = 'ERROR_RATE'
  AND timestamp >= extract(epoch from now() - interval '$__range')::bigint * 1000
GROUP BY time, flag_key
ORDER BY time
```

**Latency (p95):**
```sql
SELECT
  time_bucket('5 minutes', to_timestamp(timestamp / 1000)) AS time,
  flag_key,
  percentile_cont(0.95) WITHIN GROUP (ORDER BY metric_value) AS p95
FROM metric_data_points
WHERE metric_type = 'LATENCY_MS'
  AND timestamp >= extract(epoch from now() - interval '$__range')::bigint * 1000
GROUP BY time, flag_key
ORDER BY time
```

### Anomaly Queries

**By Severity:**
```sql
SELECT
  severity,
  COUNT(*) AS count
FROM anomaly_alerts
WHERE detected_at >= extract(epoch from now() - interval '$__range')::bigint * 1000
GROUP BY severity
ORDER BY
  CASE severity
    WHEN 'CRITICAL' THEN 1
    WHEN 'HIGH' THEN 2
    WHEN 'MEDIUM' THEN 3
    WHEN 'LOW' THEN 4
  END
```

**Timeline:**
```sql
SELECT
  time_bucket('1 hour', to_timestamp(detected_at / 1000)) AS time,
  severity,
  COUNT(*) AS value
FROM anomaly_alerts
WHERE detected_at >= extract(epoch from now() - interval '$__range')::bigint * 1000
GROUP BY time, severity
ORDER BY time
```

## Customization

### Добавить переменную для фильтрации

1. Dashboard Settings → Variables → Add variable
2. Настройки:
   - Name: `flag_id`
   - Type: Query
   - Data source: Flagent Postgres
   - Query: `SELECT DISTINCT flag_id FROM metric_data_points ORDER BY flag_id`
3. Save

4. Обновить queries:
```sql
WHERE flag_id = $flag_id
```

### Добавить alert rule

1. Edit panel → Alert tab
2. Create alert rule
3. Условие:
   - WHEN `avg()` OF `query(A, 5m, now)`
   - IS ABOVE `0.05` (5% error rate)
4. Notification channel: Slack/Email

### Экспорт dashboard

```bash
# Export dashboard JSON
curl -H "Authorization: Bearer <api-key>" \
  http://localhost:3000/api/dashboards/uid/flagent-metrics \
  | jq '.dashboard' > my-dashboard.json

# Import dashboard
curl -X POST -H "Authorization: Bearer <api-key>" \
  -H "Content-Type: application/json" \
  -d @my-dashboard.json \
  http://localhost:3000/api/dashboards/db
```

## Troubleshooting

### Dashboard не загружается

**Причина:** Data source не настроен

**Решение:**
```bash
# Check data source
curl http://localhost:3000/api/datasources

# Test connection
curl -X POST http://localhost:3000/api/datasources/<id>/health
```

### Queries возвращают пустые результаты

**Причина:** Нет данных в таблицах

**Решение:**
```sql
-- Check if data exists
SELECT COUNT(*) FROM metric_data_points;
SELECT COUNT(*) FROM anomaly_alerts;

-- Submit test metrics
curl -X POST http://localhost:8000/api/v1/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "flagId": 1,
    "flagKey": "test_flag",
    "metricType": "SUCCESS_RATE",
    "metricValue": 0.95,
    "timestamp": '$(date +%s000)'
  }'
```

### PostgreSQL connection failed

**Причина:** Неправильные credentials или host

**Решение:**
```bash
# Check connection from Grafana container
docker exec -it flagent-grafana sh
apk add postgresql-client
psql -h postgres -U flagent -d flagent -c "SELECT 1"

# Update data source if needed
# Configuration → Data Sources → Flagent Postgres → Edit
```

### Time_bucket function not found

**Причина:** TimescaleDB extension не установлена

**Решение:**

**Option 1: Install TimescaleDB (рекомендуется)**
```sql
CREATE EXTENSION IF NOT EXISTS timescaledb;
```

**Option 2: Replace time_bucket с date_trunc**
```sql
-- Replace this:
time_bucket('5 minutes', to_timestamp(timestamp / 1000))

-- With this:
date_trunc('minute', to_timestamp(timestamp / 1000))
```

## Best Practices

1. **Regular refresh** - set dashboard refresh to 30s-1min для real-time мониторинга
2. **Alert thresholds** - настроить alerts для critical metrics
3. **Data retention** - очищать старые metrics (>90 days)
4. **Snapshot backups** - периодически сохранять dashboard JSON
5. **Access control** - ограничить доступ к production dashboards

## Advanced Features

### Template Variables

Добавьте variables для динамической фильтрации:

**Flag Filter:**
```sql
SELECT DISTINCT flag_key FROM metric_data_points ORDER BY flag_key
```

**Time Range Picker:**
- Use built-in `$__timeFrom()` and `$__timeTo()` functions

**Environment Filter:**
```sql
SELECT DISTINCT environment FROM flags ORDER BY environment
```

### Annotations

Отображение deployment events на графиках:

```sql
SELECT
  to_timestamp(created_at / 1000) AS time,
  'Deployment' AS title,
  version AS text
FROM deployments
WHERE created_at >= extract(epoch from $__timeFrom())::bigint * 1000
  AND created_at <= extract(epoch from $__timeTo())::bigint * 1000
ORDER BY time
```

### Dashboard Links

Связать dashboards между собой:
1. Dashboard Settings → Links → Add dashboard link
2. Указать UID целевого dashboard
3. Include time range & variables

## Resources

- [Grafana Documentation](https://grafana.com/docs/)
- [PostgreSQL Data Source](https://grafana.com/docs/grafana/latest/datasources/postgres/)
- [Dashboard Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)
- [Alerting Guide](https://grafana.com/docs/grafana/latest/alerting/)
