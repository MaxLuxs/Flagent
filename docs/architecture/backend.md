# Backend Architecture (Ktor)

> [English](backend.md) | [Русский](backend.ru.md)

## Overview

The backend is built on Ktor with Kotlin Coroutines for async I/O. It follows Clean Architecture with clear layer separation.

## Structure

```
flagent/backend/
├── application/       # Application.kt - entry point, EnterpriseConfigurator
├── config/            # Configuration from environment
├── domain/            # Domain Layer
│   ├── entity/        # Data classes (Flag, Segment, Constraint, etc.)
│   ├── repository/    # Repository interfaces (IFlagRepository, etc.)
│   ├── usecase/       # Use cases (EvaluateFlagUseCase, EvaluateBatchUseCase)
│   └── value/         # Value objects (EntityID, FlagKey, EvaluationContext)
├── repository/        # Infrastructure - Exposed tables, repository implementations
├── service/           # Application Layer - orchestrates use cases
│   ├── command/       # Command objects (CreateFlagCommand, PutSegmentCommand, etc.)
│   └── adapter/       # Adapters to shared evaluator
├── route/             # Presentation - Ktor routes (API endpoints)
│   └── mapper/        # ResponseMappers (domain → API Response)
├── middleware/        # Ktor plugins (CORS, auth, etc.)
├── cache/             # In-memory cache for evaluation
├── recorder/          # Data recording (Kafka/Kinesis/PubSub)
└── util/              # Utilities
```

## Data flow (Request → Command → Service → Repository)

```
HTTP Request (CreateFlagRequest, PutSegmentRequest, etc.)
    ↓
Route Handler
    ↓
Request → Command mapping
    ↓
Service (accepts Command, builds domain entity)
    ↓
Use Case / Repository (DB access)
    ↓
Database (Exposed)
    ↓
Domain entity → Response mapping (ResponseMappers)
    ↓
HTTP Response
```

## Evaluation flow

```
POST /api/v1/evaluation
    ↓
EvaluationRoute
    ↓
EvaluationService (delegates to EvaluateFlagUseCase + SharedFlagEvaluatorAdapter)
    ↓
EvaluateFlagUseCase → shared FlagEvaluator
    ↓
EvalCache (in-memory)
    ↓
Flag evaluation algorithm (shared module)
    ↓
Constraint evaluation (ConstraintEvaluator)
    ↓
Distribution rollout (RolloutAlgorithm)
    ↓
Return result
    ↓
DataRecorder (async)
```

## Caching

- **EvalCache**: In-memory cache of all flags
- Refreshed periodically from DB (default every 3 seconds)
- Thread-safe via coroutines
- Indexed by ID, Key, Tags

## Authentication

Supported methods: JWT (HS256, HS512, RS256), Basic Auth, Header Auth, Cookie Auth.

## Monitoring

Prometheus metrics, StatsD, structured logging.

## API documentation

**Swagger UI** at `/docs`: view endpoints, test API, view request/response schemas.

**OpenAPI 3.0**: `GET /api/v1/openapi.yaml`, `GET /api/v1/openapi.json`. Spec is loaded from `docs/api/openapi.yaml` at startup. Implementation: DocumentationRoutes.kt, `ktor-server-swagger`, Swagger UI via CDN (v5.10.3).
