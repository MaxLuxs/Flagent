# API Endpoints

## Base Path

Все API endpoints имеют базовый путь `/api/v1`

## API Documentation

### Swagger UI

Интерактивная документация API доступна через Swagger UI:

- **Swagger UI**: `http://localhost:18000/docs`
- **OpenAPI спецификация (YAML)**: `http://localhost:18000/api/v1/openapi.yaml`
- **OpenAPI спецификация (JSON)**: `http://localhost:18000/api/v1/openapi.json`

Swagger UI позволяет:
- Просматривать все доступные endpoints
- Тестировать API прямо из браузера
- Просматривать схемы данных (request/response models)
- Видеть примеры запросов и ответов

### OpenAPI Specification

Полная OpenAPI 3.0 спецификация находится в файле `docs/api/openapi.yaml`.

## Endpoints

### Health Check

- `GET /api/v1/health` - проверка здоровья сервиса

### Flags

- `GET /api/v1/flags` - список флагов
- `POST /api/v1/flags` - создание флага
- `GET /api/v1/flags/{flagID}` - получение флага
- `PUT /api/v1/flags/{flagID}` - обновление флага
- `DELETE /api/v1/flags/{flagID}` - удаление флага
- `PUT /api/v1/flags/{flagID}/enabled` - включить/выключить флаг
- `PUT /api/v1/flags/{flagID}/restore` - восстановить удаленный флаг
- `GET /api/v1/flags/{flagID}/snapshots` - история снимков флага
- `GET /api/v1/flags/entity_types` - типы сущностей

### Segments

- `GET /api/v1/flags/{flagID}/segments` - список сегментов
- `POST /api/v1/flags/{flagID}/segments` - создание сегмента
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}` - обновление сегмента
- `DELETE /api/v1/flags/{flagID}/segments/{segmentID}` - удаление сегмента
- `PUT /api/v1/flags/{flagID}/segments/reorder` - изменение порядка сегментов

### Constraints

- `GET /api/v1/flags/{flagID}/segments/{segmentID}/constraints` - список ограничений
- `POST /api/v1/flags/{flagID}/segments/{segmentID}/constraints` - создание ограничения
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}/constraints/{constraintID}` - обновление ограничения
- `DELETE /api/v1/flags/{flagID}/segments/{segmentID}/constraints/{constraintID}` - удаление ограничения

### Distributions

- `GET /api/v1/flags/{flagID}/segments/{segmentID}/distributions` - список распределений
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}/distributions` - обновление распределений

### Variants

- `GET /api/v1/flags/{flagID}/variants` - список вариантов
- `POST /api/v1/flags/{flagID}/variants` - создание варианта
- `PUT /api/v1/flags/{flagID}/variants/{variantID}` - обновление варианта
- `DELETE /api/v1/flags/{flagID}/variants/{variantID}` - удаление варианта

### Tags

- `GET /api/v1/tags` - список всех тегов
- `POST /api/v1/tags` - создание тега
- `GET /api/v1/flags/{flagID}/tags` - теги флага
- `POST /api/v1/flags/{flagID}/tags` - добавление тега к флагу
- `DELETE /api/v1/flags/{flagID}/tags/{tagID}` - удаление тега у флага

### Evaluation

- `POST /api/v1/evaluation` - оценка одного флага
- `POST /api/v1/evaluation/batch` - пакетная оценка флагов

### Export

- `GET /api/v1/export/sqlite` - экспорт в SQLite файл
- `GET /api/v1/export/eval_cache/json` - экспорт кэша в JSON

## Референс

- **OpenAPI спецификация**: `docs/api/openapi.yaml`
- **Swagger UI**: доступен на `/docs` после запуска сервера
