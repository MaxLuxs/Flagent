# Архитектура Backend (Ktor)

## Обзор

Backend построен на Ktor с использованием Kotlin Coroutines для асинхронности.
Следует принципам Clean Architecture с разделением на слои.

## Структура

```
flagent/backend/
├── application/       # Application.kt - точка входа, EnterpriseConfigurator
├── config/            # Конфигурация из environment
├── domain/            # Domain Layer
│   ├── entity/        # Data classes (Flag, Segment, Constraint, etc.)
│   ├── repository/    # Интерфейсы репозиториев (IFlagRepository, etc.)
│   ├── usecase/       # Use cases (EvaluateFlagUseCase, EvaluateBatchUseCase)
│   └── value/         # Value objects (EntityID, FlagKey, EvaluationContext)
├── repository/        # Infrastructure - Exposed таблицы, реализации репозиториев
├── service/           # Application Layer - оркестрация use cases
│   ├── command/       # Command objects (CreateFlagCommand, PutSegmentCommand, etc.)
│   └── adapter/       # Adapters к shared evaluator
├── route/             # Presentation - Ktor routes (API endpoints)
│   └── mapper/        # ResponseMappers (domain → API Response)
├── middleware/        # Ktor plugins (CORS, auth, etc)
├── cache/             # In-memory кэш для evaluation
├── recorder/          # Data recording (Kafka/Kinesis/PubSub)
└── util/              # Утилиты
```

## Поток данных (Request → Command → Service → Repository)

```
HTTP Request (CreateFlagRequest, PutSegmentRequest, etc.)
    ↓
Route Handler
    ↓
Request → Command mapping (CreateFlagCommand, PutSegmentCommand, etc.)
    ↓
Service (принимает Command, строит domain entity внутри)
    ↓
Use Case / Repository (доступ к БД)
    ↓
Database (Exposed)
    ↓
Domain entity → Response mapping (ResponseMappers)
    ↓
HTTP Response
```

## Evaluation Flow

```
POST /api/v1/evaluation
    ↓
EvaluationRoute
    ↓
EvaluationService (делегирует в EvaluateFlagUseCase + SharedFlagEvaluatorAdapter)
    ↓
EvaluateFlagUseCase → shared FlagEvaluator
    ↓
EvalCache (in-memory)
    ↓
Flag evaluation algorithm (в shared module)
    ↓
Constraint evaluation (ConstraintEvaluator)
    ↓
Distribution rollout (RolloutAlgorithm)
    ↓
Return result
    ↓
DataRecorder (async)
```

## Кэширование

- **EvalCache**: In-memory кэш всех флагов
- Обновляется периодически из БД (по умолчанию каждые 3 секунды)
- Thread-safe через coroutines
- Индексация: ID, Key, Tags

## Аутентификация

Поддерживаемые методы:
- JWT (HS256, HS512, RS256)
- Basic Auth
- Header Auth
- Cookie Auth

## Мониторинг

- Prometheus metrics
- StatsD
- Logging (structured)

## API Документация

### Swagger UI

Интерактивная документация API доступна через Swagger UI на endpoint `/docs`:
- Просмотр всех API endpoints
- Интерактивное тестирование API
- Просмотр схем данных (request/response models)

### OpenAPI Спецификация

OpenAPI 3.0 спецификация доступна через:
- `GET /api/v1/openapi.yaml` - спецификация в YAML формате
- `GET /api/v1/openapi.json` - спецификация в JSON формате

Спецификация загружается из файла `docs/api/openapi.yaml` при старте приложения.

### Реализация

- **DocumentationRoutes.kt**: Routes для Swagger UI и OpenAPI спецификации
- **Зависимости**: `ktor-server-swagger`, Jackson YAML для конвертации
- **Swagger UI**: Встроен через CDN (версия 5.10.3)
