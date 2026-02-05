# План: Базовые метрики в Core + Enterprise фичи

## 1. Текущее состояние

### Enterprise метрики (internal/flagent-enterprise)
- **Источник данных**: SDK отправляет метрики через POST `/metrics` (MetricDataPoint) — conversion, latency, success_rate и т.д.
- **Таблица**: `metric_data_points` — полная структура (flagId, metricType, metricValue, variantId, tenantId...)
- **API**:
  - `POST /metrics`, `POST /metrics/single` — приём метрик от клиентов
  - `GET /flags/{id}/metrics` — метрики по флагу (с фильтрами type, variantId)
  - `GET /flags/{id}/experiment-insights` — A/B конверсии, significance
  - `GET /metrics/overview` — totalEvaluations, topFlags, timeSeries (агрегация из MetricDataPoints)

### Core (backend)
- **Evaluation API**: POST `/evaluation`, `/evaluation/batch` — возвращает результат, **не записывает** счётчики
- **DataRecordingService**: отправляет EvalResult в Kafka/Kinesis/PubSub (внешние системы), не в локальную БД

---

## 2. Что перенести в Core (OSS)

### 2.1. Evaluation Events — подсчёт вызовов API

**Цель**: OSS видит "сколько раз вызывали evaluation API по каждому флагу".

| Компонент | Описание |
|-----------|----------|
| **Таблица** `evaluation_events` | `id`, `flag_id`, `timestamp_ms` — минимальная запись на каждый вызов evaluate |
| **Индексы** | `(flag_id, timestamp_ms)` для быстрой агрегации |
| **Запись** | EvaluationService после каждого `evaluateFlag` / batch — асинхронно (channel/coroutine) |

### 2.2. Core Metrics Routes

| Endpoint | Ответ | Условие |
|----------|--------|---------|
| `GET /api/v1/metrics/overview` | totalEvaluations, uniqueFlags, topFlags, timeSeries | Только когда **enterprise отсутствует** |

Формат ответа совместим с `GlobalMetricsOverviewResponse` (frontend уже умеет).

### 2.3. Core Metrics Service

- `EvaluationEventRepository` — save(event), getOverview(start, end, topLimit, timeBucketMs)
- `CoreMetricsService` — оркестрация
- Запись в `evaluation_events` — fire-and-forget, не блокирует evaluation

### 2.4. Файлы для создания/изменения (Core)

| Файл | Действие |
|------|----------|
| `backend/.../tables/Tables.kt` | Добавить `EvaluationEvents` |
| `backend/.../migration/` | Миграция создания таблицы |
| `backend/.../repository/EvaluationEventRepository.kt` | Новый |
| `backend/.../service/CoreMetricsService.kt` | Новый |
| `backend/.../route/CoreMetricsRoutes.kt` | Новый |
| `backend/.../application/Application.kt` | Регистрация routes + передача recorder в EvaluationService |
| `backend/.../service/EvaluationService.kt` | Вызов `evaluationEventRecorder.record(flagId, timestamp)` после eval |

---

## 3. Что остаётся в Enterprise

| Компонент | Назначение |
|-----------|------------|
| POST `/metrics`, `/metrics/single` | Клиентские метрики (conversion, latency, custom) |
| GET `/flags/{id}/metrics` | Полные метрики с фильтрами (metricType, variantId) |
| GET `/flags/{id}/experiment-insights` | A/B статистика, p-value, рекомендации |
| `getAggregation` | Для Anomaly Detection, Smart Rollout |
| Таблица `metric_data_points` | Enterprise-only |

### 3.1. Изменения в Enterprise

- `GET /metrics/overview` — **не регистрировать**, когда core уже отдаёт overview (при отсутствии enterprise core отдаёт; при наличии enterprise — enterprise может **переопределить** overview, объединив evaluation_events + metric_data_points, или оставить свой на основе MetricDataPoints).
- **Рекомендация**: Enterprise оставляет свой overview на основе MetricDataPoints (клиентские метрики). Core overview = API evaluation count. Разные источники — оба полезны. При наличии enterprise можно в overview показывать оба: "API evaluations" из core + "Client metrics" из enterprise. Для простоты: Enterprise продолжает использовать только MetricDataPoints для overview (текущее поведение). Core добавляет свой overview только когда enterprise отсутствует.

---

## 4. Frontend

| Изменение | Описание |
|-----------|----------|
| `AppConfig.Features.enableMetrics` | Убрать привязку к `isEnterprise`. `enableMetrics = (ENV_FEATURE_METRICS ?: true)` — метрики доступны и в OSS |
| Analytics | Overview + By flags с Evaluations, клик → FlagMetrics работают в OSS |

---

## 5. Новые Enterprise-фичи (компенсация)

Чтобы Enterprise оставался ценным после переноса базовых метрик в OSS:

### 5.1. Cohort Analysis
- Сравнение метрик по сегментам пользователей (region, tier, cohort по дате регистрации)
- UI: выбор dimension (например, `entityContext.region`), группировка topFlags по dimension

### 5.2. Funnel Analytics
- Воронка: флаг A → флаг B → флаг C (конверсия по шагам)
- Настройка funnel (список flagIds по порядку), отображение drop-off по шагам

### 5.3. A/B Power Calculator
- "Сколько пользователей нужно для 80% мощности при MDE 5%?"
- Ввод: baseline conversion, MDE, alpha, power → sample size

### 5.4. Predictive Experiment Duration
- "Когда эксперимент достигнет significance?" — прогноз на основе текущего trend
- Использует текущие conversion rates + sample size

### 5.5. Custom Dimensions
- MetricDataPoint с произвольными dimensions (не только variantId): `dimensions: { "campaign": "summer", "region": "EU" }`
- Агрегация и фильтрация по dimensions в UI

### 5.6. Scheduled Reports (Export)
- Экспорт метрик в CSV/Excel по расписанию (daily/weekly)
- Отправка на email или в Slack

### 5.7. Real-time Metrics Stream
- WebSocket/SSE для live-обновления дашборда метрик
- Полезно для мониторинга во время релизов

### 5.8. Multi-variate Analysis
- Сравнение >2 вариантов с поправкой на множественные сравнения (Bonferroni, Holm)
- Визуализация confidence intervals для всех пар

---

## 6. Порядок внедрения

1. **Core**: таблица `evaluation_events`, репозиторий, сервис, routes, запись из EvaluationService
2. **Frontend**: enableMetrics не зависит от isEnterprise
3. **Тесты**: CoreMetricsService, EvaluationEventRepository, CoreMetricsRoutes
4. **Enterprise**: при необходимости — расширить overview (merge API evals + client metrics) или оставить как есть
5. **Enterprise**: добавить 1–2 фичи из раздела 5 (например, Cohort Analysis + Power Calculator)

---

## 7. Схема данных Core

```sql
CREATE TABLE evaluation_events (
    id BIGSERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL,
    timestamp_ms BIGINT NOT NULL
);
CREATE INDEX idx_eval_events_flag_time ON evaluation_events(flag_id, timestamp_ms);
CREATE INDEX idx_eval_events_timestamp ON evaluation_events(timestamp_ms);
```

Для SQLite (OSS dev):
```sql
CREATE TABLE evaluation_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    flag_id INTEGER NOT NULL,
    timestamp_ms INTEGER NOT NULL
);
CREATE INDEX idx_eval_events_flag_time ON evaluation_events(flag_id, timestamp_ms);
```

---

## 8. Retention / Cleanup

- Core: периодическая очистка `evaluation_events` через `EvaluationEventsCleanupJob`:
  - `FLAGENT_EVALUATION_EVENTS_RETENTION_DAYS` (default: 90)
  - `FLAGENT_EVALUATION_EVENTS_CLEANUP_ENABLED` (default: true)
  - `FLAGENT_EVALUATION_EVENTS_CLEANUP_INTERVAL` (default: 24h)
- Enterprise: уже есть `deleteOlderThan` для MetricDataPoints
