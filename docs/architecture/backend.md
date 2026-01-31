# Архитектура Backend (Ktor)

## Обзор

Backend построен на Ktor с использованием Kotlin Coroutines для асинхронности.

## Структура

```
flagent/
├── application/     # Application.kt - точка входа
├── config/          # Конфигурация из environment
├── entity/          # Data classes (модели)
├── repository/      # Exposed таблицы и DAO
├── service/         # Бизнес-логика
├── route/           # Ktor routes (API endpoints)
├── middleware/      # Ktor plugins (CORS, auth, etc)
├── cache/           # In-memory кэш для evaluation
├── recorder/        # Data recording (Kafka/Kinesis/PubSub)
└── util/            # Утилиты
```

## Поток данных

```
HTTP Request
    ↓
Middleware (CORS, Auth, Logging)
    ↓
Route Handler
    ↓
Service (бизнес-логика)
    ↓
Repository (доступ к БД)
    ↓
Database (Exposed)
```

## Evaluation Flow

```
POST /api/v1/evaluation
    ↓
EvaluationRoute
    ↓
EvaluationService
    ↓
EvalCache (in-memory)
    ↓
Flag evaluation algorithm
    ↓
Segment matching
    ↓
Constraint evaluation
    ↓
Distribution rollout
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
