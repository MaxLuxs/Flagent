# API Endpoints

> [English](endpoints.md) | Русский

## Базовый путь

Все endpoint используют базовый путь `/api/v1`.

## Тенант и аутентификация

При включённой мультитенантности (enterprise или `FLAGENT_ADMIN_API_KEY`) endpoint флагов и evaluation **привязаны к тенанту**. Тенант определяется по:

- **`X-API-Key`** — API ключ тенанта (рекомендуется для server-to-server и SDK). Заголовок: `X-API-Key: <tenant-api-key>`.
- **`Authorization: Bearer <JWT>`** — если в JWT есть claim тенанта (SSO).

Без валидного контекста тенанта запросы к `/api/v1/flags`, `/api/v1/evaluation` и т.д. возвращают 401. В OSS без принудительного тенанта заголовки опциональны. См. [Admin and Tenants](../guides/admin-and-tenants.md) и [Security: Tenant API Keys](../guides/security-tenant-api-keys.md).

## Документация API

**Swagger UI:** `http://localhost:18000/docs` — просмотр endpoint, тесты из браузера, схемы request/response.

**OpenAPI 3.0:** `http://localhost:18000/api/v1/openapi.yaml`, `http://localhost:18000/api/v1/openapi.json`. Полная спецификация в `docs/api/openapi.yaml`.

## Endpoints

### Health

- `GET /api/v1/health` — проверка состояния сервиса

### Flags

- `GET /api/v1/flags` — список флагов
- `POST /api/v1/flags` — создать флаг
- `GET /api/v1/flags/{flagID}` — получить флаг
- `PUT /api/v1/flags/{flagID}` — обновить флаг
- `DELETE /api/v1/flags/{flagID}` — удалить флаг
- `PUT /api/v1/flags/{flagID}/enabled` — вкл/выкл флаг
- `PUT /api/v1/flags/{flagID}/restore` — восстановить удалённый флаг
- `GET /api/v1/flags/{flagID}/snapshots` — история снапшотов флага
- `GET /api/v1/flags/entity_types` — типы entity

### Segments

- `GET/POST/PUT/DELETE /api/v1/flags/{flagID}/segments` — CRUD сегментов
- `PUT /api/v1/flags/{flagID}/segments/reorder` — изменить порядок сегментов

### Constraints

- `GET/POST/PUT/DELETE /api/v1/flags/{flagID}/segments/{segmentID}/constraints` — CRUD ограничений. Операторы: EQ, NEQ, LT, LTE, GT, GTE, EREG, NEREG, IN, NOTIN, CONTAINS, NOTCONTAINS

### Distributions

- `GET /api/v1/flags/{flagID}/segments/{segmentID}/distributions`
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}/distributions`

### Variants

- `GET/POST/PUT/DELETE /api/v1/flags/{flagID}/variants` — CRUD вариантов

### Tags

- `GET /api/v1/tags` — все теги
- `GET/POST/DELETE /api/v1/flags/{flagID}/tags` — теги флага. Создание: body `{"value": "..."}`

### Evaluation

- `POST /api/v1/evaluation` — одиночный evaluation
- `POST /api/v1/evaluation/batch` — batch evaluation

### Export

- `GET /api/v1/export/sqlite` — экспорт в SQLite
- `GET /api/v1/export/eval_cache/json` — экспорт eval cache в JSON

## Справка

- **OpenAPI:** `docs/api/openapi.yaml`
- **Swagger UI:** `/docs` при запущенном сервере
