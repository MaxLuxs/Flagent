# Flagent Spring Boot Sample

Minimal Spring Boot application using the Flagent Spring Boot Starter for feature flag evaluation.

## Features

- **Single evaluation** - Evaluate a flag via `FlagentEvaluationFacade`
- **EntityContext** - Constraint-based targeting (country, tier)
- **Batch evaluation** - Evaluate multiple flags for multiple entities
- **Caching** - Optional in-memory cache (Caffeine)

## Requirements

- JDK 21+
- Flagent backend running on `http://localhost:18000`

## Setup

### 1. Start Flagent Backend

From repo root:

```bash
./gradlew :backend:run
```

### 2. Run Sample

From repo root:

```bash
./gradlew :sample-spring-boot:bootRun
```

The sample starts on `http://localhost:8080`.

## Configuration

See `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

flagent:
  base-url: http://localhost:18000
  cache:
    enabled: true
    ttl-ms: 60000
```

Override `flagent.base-url` if Flagent runs elsewhere.

## Usage

### Single Evaluation

```bash
# Basic
curl "http://localhost:8080/eval?flagKey=my_flag&entityId=user-1"

# With entityContext for constraint-based targeting
curl "http://localhost:8080/eval?flagKey=my_flag&entityId=user-1&country=US&tier=premium"
```

### Batch Evaluation (POST)

```bash
curl -X POST http://localhost:8080/eval/batch \
  -H "Content-Type: application/json" \
  -d '{
    "entities": [
      {"entityID": "user1", "entityType": "user", "entityContext": {"country": "US", "tier": "premium"}},
      {"entityID": "user2", "entityType": "user", "entityContext": {"country": "EU"}}
    ],
    "flagKeys": ["my_flag", "experiment_1"]
  }'
```

### Batch Demo (GET)

Convenience endpoint with pre-defined entities:

```bash
curl "http://localhost:8080/eval/batch-demo?flagKey=my_flag"
```

## Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/eval` | GET | Single evaluation. Params: flagKey, entityId, entityType, country, tier |
| `/eval/batch` | POST | Batch evaluation (JSON body) |
| `/eval/batch-demo` | GET | Batch demo with sample entities |

## Constraint-Based Targeting

When your flag has segments with constraints (e.g., `country` EQ `US`), pass `country` and `tier` as query params. The evaluator matches the segment and returns the assigned variant.

## Project Structure

```
spring-boot/
├── src/main/java/com/flagent/sample/
│   ├── EvalController.java      # Evaluation endpoints
│   └── SpringBootSampleApplication.java
├── src/main/resources/
│   └── application.yml
├── build.gradle.kts
└── README.md
```

## Troubleshooting

- **Connection refused**: Ensure Flagent backend is running at `http://localhost:18000`
- **404 on evaluation**: Create a flag in Flagent UI first (e.g., key `my_flag`)

## License

Apache 2.0 - See parent project license
