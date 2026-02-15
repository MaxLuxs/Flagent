# API Endpoints

> [English](endpoints.md) | [Русский](endpoints.ru.md)

## Base Path

All API endpoints use the base path `/api/v1`.

## Tenant Context and Authentication

When multi-tenancy is enabled (e.g. with the enterprise module or `FLAGENT_ADMIN_API_KEY`), flag and evaluation endpoints are **tenant-scoped**. The server determines the tenant from:

- **`X-API-Key`** — tenant API key (recommended for server-to-server and SDKs). Send in the request header: `X-API-Key: <tenant-api-key>`.
- **`Authorization: Bearer <JWT>`** — when the JWT contains a tenant claim (e.g. SSO users).

Without a valid tenant context, requests to `/api/v1/flags`, `/api/v1/evaluation`, etc. return 401. In OSS mode without tenant enforcement, these headers are optional. See [Admin and Tenants](../guides/admin-and-tenants.md) and [Security: Tenant API Keys](../guides/security-tenant-api-keys.md).

## API Documentation

### Swagger UI

Interactive API documentation is available via Swagger UI:

- **Swagger UI**: `http://localhost:18000/docs`
- **OpenAPI specification (YAML)**: `http://localhost:18000/api/v1/openapi.yaml`
- **OpenAPI specification (JSON)**: `http://localhost:18000/api/v1/openapi.json`

Swagger UI allows you to:
- View all available endpoints
- Test the API directly from the browser
- View data schemas (request/response models)
- See request and response examples

### OpenAPI Specification

The full OpenAPI 3.0 specification is in `docs/api/openapi.yaml`.

## Endpoints

### Health Check

- `GET /api/v1/health` - service health check

### Flags

- `GET /api/v1/flags` - list flags
- `POST /api/v1/flags` - create flag
- `GET /api/v1/flags/{flagID}` - get flag
- `PUT /api/v1/flags/{flagID}` - update flag
- `DELETE /api/v1/flags/{flagID}` - delete flag
- `PUT /api/v1/flags/{flagID}/enabled` - enable/disable flag
- `PUT /api/v1/flags/{flagID}/restore` - restore deleted flag
- `GET /api/v1/flags/{flagID}/snapshots` - flag snapshot history
- `GET /api/v1/flags/entity_types` - entity types

### Segments

- `GET /api/v1/flags/{flagID}/segments` - list segments
- `POST /api/v1/flags/{flagID}/segments` - create segment
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}` - update segment
- `DELETE /api/v1/flags/{flagID}/segments/{segmentID}` - delete segment
- `PUT /api/v1/flags/{flagID}/segments/reorder` - reorder segments

### Constraints

- `GET /api/v1/flags/{flagID}/segments/{segmentID}/constraints` - list constraints
- `POST /api/v1/flags/{flagID}/segments/{segmentID}/constraints` - create constraint
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}/constraints/{constraintID}` - update constraint
- `DELETE /api/v1/flags/{flagID}/segments/{segmentID}/constraints/{constraintID}` - delete constraint

Supported operators: `EQ`, `NEQ`, `LT`, `LTE`, `GT`, `GTE`, `EREG`, `NEREG`, `IN`, `NOTIN`, `CONTAINS`, `NOTCONTAINS`

### Distributions

- `GET /api/v1/flags/{flagID}/segments/{segmentID}/distributions` - list distributions
- `PUT /api/v1/flags/{flagID}/segments/{segmentID}/distributions` - update distributions

### Variants

- `GET /api/v1/flags/{flagID}/variants` - list variants
- `POST /api/v1/flags/{flagID}/variants` - create variant
- `PUT /api/v1/flags/{flagID}/variants/{variantID}` - update variant
- `DELETE /api/v1/flags/{flagID}/variants/{variantID}` - delete variant

### Tags

- `GET /api/v1/tags` - list all tags
- `GET /api/v1/flags/{flagID}/tags` - flag tags
- `POST /api/v1/flags/{flagID}/tags` - create tag by `value` (or find existing) and attach to flag. Body: `{"value": "..."}`
- `DELETE /api/v1/flags/{flagID}/tags/{tagID}` - remove tag from flag

### Evaluation

- `POST /api/v1/evaluation` - evaluate single flag
- `POST /api/v1/evaluation/batch` - batch flag evaluation

### Export

- `GET /api/v1/export/sqlite` - export to SQLite file
- `GET /api/v1/export/eval_cache/json` - export eval cache to JSON

## Reference

- **OpenAPI specification**: `docs/api/openapi.yaml`
- **Swagger UI**: available at `/docs` when the server is running
