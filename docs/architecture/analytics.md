# Analytics Architecture — доработки для удобной аналитики и сравнения вариантов

## Цель

Сделать аналитику **удобной** и дать возможность **удобно строить отчёты по вариантам** (варонки, сравнение A/B, воронки конверсии) без размазывания по разным экранам и без ручного склейки данных.

**General-first principle:** Analytics and funnels are **general** (event-based, Firebase-level): overview by events, DAU, funnels by event sequence. Flags and variants are **optional dimensions** (filters) on top of the same data.

## Текущее состояние (кратко)

| Слой | OSS (Core) | Enterprise |
|------|------------|------------|
| **События** | `evaluation_events`: flag_id, timestamp_ms, client_id (опционально). Нет variant_id. | — |
| **События продукта** | `analytics_events`: event_name, event_params (JSON), user_id, session_id, platform, app_version, timestamp_ms. **Нет** flag_id/variant_id. | — |
| **Метрики** | — | `metric_data_points`: flag_id, segment_id, variant_id, metric_type, metric_value, entity_id, timestamp. |
| **Агрегаты** | Core: overview по evaluations (total, time series, top flags). Analytics: overview по events (total, DAU, top events). | Insights по флагу: conversion по вариантам, z-test, recommendation. |
| **UI** | Analytics: вкладки Overview / Events / By flags. Отдельно Flag metrics (если enableMetrics). | Experiment insights на странице флага/эксперимента; Metrics dashboard. |

**Проблемы:**

1. **Нет единого места под "сравнение вариантов"** — insights живут на странице флага, события не привязаны к вариантам.
2. **Analytics events не связаны с flag/variant** — нельзя "разбить по вариантам" события (например purchase по variant_a / variant_b).
3. **Нет конструктора отчётов** — пользователь не может собрать один вид: "флаг X, метрики conversion + screen_view, группировка по вариантам, период 7d".
4. **Воронки** — нет модели и UI для "шаг 1 → шаг 2 → конверсия" с разрезом по вариантам.

---

## Implemented APIs (general analytics and crashlytics)

- **GET /api/v1/analytics/overview** — Optional query params: `platform`, `appVersion`, `eventName` to filter events. Response unchanged (totalEvents, uniqueUsers, topEvents, timeSeries, dauByDay).
- **GET /api/v1/crashes/overview** — Query: `start`, `end`, `timeBucketMs`. Response: `totalCrashes`, `timeSeries`, `byPlatform`, `byAppVersion` (each entry: `key`, `count`). Global crash dashboard without flag scope.
- **POST /api/v1/analytics/funnel** — Body: `steps` (list of `{ eventName, eventParamFilter? }`), `startMs`, `endMs`, `entityDimension` ("USER_ID" | "SESSION_ID"), optional `platform`, `appVersion`, `flagId`, `variantId`. Response: `steps` (stepIndex, eventName, reachedCount, conversionFromPrevious). Max range 90 days. Funnels are general (event sequence); flag/variant are optional filters.

---

## Предлагаемая архитектура доработок

### 1. Принципы

- **Domain-first**: доменные сущности отчётов и срезов не зависят от Ktor/Exposed.
- **Один entry point для "аналитики по вариантам"**: один раздел/конструктор, откуда строятся и сохранённые отчёты, и разовые срезы.
- **Связь событий с экспериментом**: либо явные колонки flag_id/variant_id в хранилище событий, либо стабильный контракт в event_params (рекомендуется и то, и другое для запросов).
- **Поэтапность**: сначала OSS-совместимые улучшения (срезы по вариантам на основе имеющихся данных), затем расширение схемы и воронки.

---

### 2. Слои и компоненты

#### 2.1 Domain (новое/расширение)

- **VariantBreakdown** (value object): идентификация среза — flagId, variantIds (опционально), segmentId (опционально), window (startMs, endMs).
- **MetricSource** (sealed): откуда берём метрику — `EvaluationCount`, `MetricDataPoint(metricType)`, `AnalyticsEvent(eventName)`.
- **ReportDefinition** (entity, опционально позже): сохранённый отчёт — название, VariantBreakdown, список MetricSource, groupBy (variant | time | segment), сортировка.
- **FunnelStep** (value object, фаза 2): имя события + опциональный фильтр (event_params).
- **FunnelDefinition** (entity, фаза 2): упорядоченный список FunnelStep, привязка к flag_id для разреза по вариантам.

Инварианты: окно start < end; при сравнении вариантов — минимум один MetricSource и указан flag с вариантами.

#### 2.2 Data model и схема БД

**OSS (Core):**

- **analytics_events** (расширение):
  - Добавить колонки: `flag_id INT NULL`, `variant_id INT NULL`, индексы по (flag_id, timestamp_ms), (variant_id, timestamp_ms) для быстрых срезов.
  - Контракт: SDK при отправке события в контексте эксперимента передаёт flag_id/variant_id (или они извлекаются из event_params при ingestion).
- **evaluation_events** (уже есть или добавить): `variant_id INT NULL` — если при evaluation пишем событие, сохранять выданный variant_id для последующего разреза по вариантам.

**Enterprise:**

- **metric_data_points** — без изменений (уже есть flag_id, variant_id).
- Опционально: таблица **report_definitions** (id, name, tenant_id, payload JSON: breakdown + metric_sources + group_by), для сохранённых отчётов.

**Миграции:**

- V{n}__analytics_events_flag_variant.sql: ADD COLUMN flag_id, variant_id, индексы, backfill из event_params при необходимости.
- V{n}__evaluation_events_variant_id.sql (если ещё нет): ADD COLUMN variant_id.

#### 2.3 Application / Services

- **AnalyticsQueryService** (новый, backend):
  - Вход: VariantBreakdown, list of MetricSource, groupBy (variant | time | segment), timeBucketMs.
  - Выход: единая структура «срез»: по каждому варианту (или по времени, или по сегменту) — значения по каждой метрике (count, sum, rate — в зависимости от типа метрики).
  - Внутри: использует EvaluationEventRepository (по flag_id, variant_id), AnalyticsEventRepository (по event_name, flag_id, variant_id), в Enterprise — IMetricsRepository (getConversionStatsByVariant, агрегаты). Агрегирует в общий DTO.
- **VariantComparisonUseCase** (или внутри AnalyticsQueryService):
  - Специализация: один флаг, все варианты, выбранные метрики (evaluation count + опционально conversion из metric_data_points + опционально события по имени). Возврат: таблица вариант × метрики + опционально significance (переиспользовать ExperimentInsightsService).
- **FunnelAnalysisUseCase** (фаза 2):
  - Вход: flag_id, FunnelDefinition (шаги), окно времени.
  - Логика: по entity_id (user_id/session_id) считаем последовательность событий, считаем конверсию шаг→шаг; разрез по variant_id из analytics_events или из привязки по entity к варианту.
  - Хранение: без новой таблицы на первом шаге — вычисление on-the-fly из analytics_events (с filter по flag_id/variant_id).

Границы: AnalyticsQueryService/VariantComparisonUseCase вызываются из route layer; репозитории остаются за интерфейсами (IRepository для analytics при необходимости абстракции).

#### 2.4 API

- **GET /api/v1/analytics/variant-comparison** (или под тем же префиксом, что и текущий analytics):
  - Query: flagId, start, end, metricSources (enum: evaluations | conversion | event:&lt;name&gt;), timeBucketMs (опционально), groupBy=variant | time.
  - Response: список срезов (variantKey или bucket timestamp, метрики: evaluationCount, conversionCount, eventCounts по имени и т.д., при Enterprise — significance/pValue для conversion).
- **GET /api/v1/analytics/overview** — оставить как есть (глобальный overview).
- **GET /api/v1/analytics/events** — при наличии flag_id/variant_id в схеме: опциональные query-параметры flagId, variantId для фильтрации списка/агрегатов по событиям.
- Enterprise: **GET /api/v1/metrics/insights** по флагу — без изменений; при интеграции в «конструктор» фронт может вызывать и variant-comparison, и insights вместе.

Единый контракт ответа для variant-comparison (пример):

```json
{
  "flagId": 1,
  "flagKey": "checkout_flow",
  "windowStartMs": 1699900000000,
  "windowEndMs": 1700000000000,
  "groupBy": "variant",
  "rows": [
    {
      "variantId": 2,
      "variantKey": "control",
      "metrics": {
        "evaluations": 1000,
        "conversion": 120,
        "event:purchase": 115
      },
      "conversionRate": 0.12,
      "pValue": null
    },
    {
      "variantId": 3,
      "variantKey": "variant_b",
      "metrics": { "evaluations": 980, "conversion": 145, "event:purchase": 140 },
      "conversionRate": 0.148,
      "pValue": 0.03
    }
  ],
  "significance": { "winnerVariantId": 3, "isSignificant": true }
}
```

#### 2.5 Frontend

- **Единый раздел "Analytics"** с подразделами:
  - **Overview** / **Events** / **By flags** — текущие вкладки (оставить).
  - **Variant comparison** (новая вкладка или отдельная страница): выбор флага → выбор метрик (evaluations, conversion, события по имени из списка) → период → кнопка «Построить». Результат: таблица вариант × метрики + мини-карточка significance (если есть insights API).
- **Конструктор отчётов** (упрощённый): те же поля (флаг, метрики, период, groupBy), без сохранения — просто единая форма и один ответ от GET variant-comparison.
- **Сохранённые отчёты** (фаза 2): если появится report_definitions — список сохранённых, открытие подставляет параметры в конструктор и дергает variant-comparison.
- **Воронки** (фаза 2): отдельный подраздел — выбор флага, добавление шагов воронки (имя события + опционально параметр), период; вывод — конверсия по шагам в разрезе по вариантам (данные от FunnelAnalysisUseCase).

Связка с ExperimentInsightsCard: на странице флага оставить текущую карточку; на странице Analytics → Variant comparison показывать расширенную таблицу и при наличии Enterprise — тот же блок significance/recommendation (переиспользовать компонент или общий API).

---

### 3. План поэтапной реализации

| Этап | Что делаем | Риск/совместимость |
|------|------------|---------------------|
| **1. Schema & ingestion** | Добавить flag_id, variant_id в analytics_events; в evaluation_events — variant_id если ещё нет. Миграции + backfill из event_params (где есть flag_key/variant_key). Контракт SDK: при отправке analytics event передавать flag_id/variant_id когда событие в контексте эксперимента. | OSS: обратная совместимость — новые колонки nullable, старый код не ломается. |
| **2. Backend variant-comparison** | AnalyticsQueryService + GET /api/v1/analytics/variant-comparison. Агрегация: evaluations по flag+variant (из evaluation_events), события по event_name+flag_id+variant_id (из analytics_events), в Enterprise — conversion из metric_data_points + вызов ExperimentInsightsService для significance. | Зависимость от этапа 1 для среза событий по вариантам. |
| **3. Frontend Variant comparison** | Новая вкладка/страница, форма (флаг, метрики, период), вызов API, таблица вариант × метрики + significance. | — |
| **4. (Опционально) Saved reports** | Таблица report_definitions, API save/load, подстановка в конструктор. | Можно отложить. |
| **5. (Фаза 2) Funnels** | FunnelDefinition, FunnelAnalysisUseCase, API funnel result, UI — шаги воронки и конверсия по вариантам. | Требует однозначной привязки событий к entity и варианту. |

---

### 4. Границы модулей и зависимостей

- **backend (Core)**: domain-модели для VariantBreakdown, MetricSource (без Enterprise-типов); AnalyticsQueryService может зависеть от репозиториев evaluation_events и analytics_events; вариант для OSS — без conversion/significance, только evaluations + events.
- **backend (Enterprise)**: расширение AnalyticsQueryService или отдельный VariantComparisonUseCase, использующий IMetricsRepository и ExperimentInsightsService; эндпоинт variant-comparison регистрируется в Enterprise, если нужно, или в Core с условной подгрузкой insights.
- **frontend**: один модуль/страница Analytics; общие модели (VariantComparisonResponse); переиспользование ExperimentInsightsCard там, где нужен блок significance.

---

### 5. Наблюдаемость и лимиты

- Логировать тяжёлые запросы variant-comparison (флаг, период, объём выборки).
- При больших окнах (например > 90 дней) можно ограничивать или предупреждать (rate limit / max range в конфиге).
- Метрики: время ответа GET variant-comparison, количество запросов в минуту.

---

### 6. Итоговый чеклист архитектуры

- [x] Domain-first: VariantBreakdown, MetricSource, отчёт — доменные сущности без Ktor/Exposed.
- [x] Зависимости вниз: route → service/use case → repository.
- [x] Расширение схемы с обратной совместимостью (nullable flag_id, variant_id).
- [x] Единая точка входа для «сравнения вариантов» (API + UI конструктор).
- [ ] Эволюция схемы: миграции с backfill и индексами.
- [x] Расширяемость: добавление нового MetricSource (например ещё один тип события) без слома существующих полей.

После утверждения архитектуры следующий шаг — детализация API (точные имена полей и коды ошибок) и реализация этапов 1–3.
