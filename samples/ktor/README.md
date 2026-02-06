# Flagent Ktor Plugin Sample

Sample Ktor application demonstrating the usage of the Flagent Ktor plugin for feature flagging and A/B testing.

## Features

- **Flagent Plugin Integration** - Full integration with Flagent Ktor plugin
- **Feature Flag Endpoints** - Custom endpoints demonstrating flag evaluation
- **Batch Evaluation** - Batch evaluation example
- **Cache Usage** - Cache information endpoint
- **Plugin Endpoints** - Automatic endpoints provided by the plugin
- **Error Handling** - Proper error handling with StatusPages

## Requirements

- JDK 17+
- Gradle 8.0+
- Flagent backend server running (default: `http://localhost:18000`)

## Setup

### 1. Start Flagent Backend

Before running the sample, ensure the Flagent backend server is running (from repo root):

```bash
./gradlew :backend:run
```

The server will start on `http://localhost:18000` by default.

### 2. Configure Base URL (Optional)

By default, the sample uses `http://localhost:18000`. You can override this with an environment variable:

```bash
export FLAGENT_BASE_URL=http://localhost:18000
```

### 3. Build and Run

From repo root:

```bash
./gradlew :sample-ktor:build
./gradlew :sample-ktor:runSample
```

Or from `samples/ktor`:

```bash
./gradlew build runSample
```

The sample server will start on `http://localhost:8080`.

## Usage

### Available Endpoints

#### Custom Sample Endpoints

- `GET /` - API information and available endpoints
- `GET /health` - Health check
- `GET /feature/{flagKey}?entityID=user123&entityType=user` - Evaluate a single flag
- `POST /feature-batch` - Batch evaluation example
- `GET /cache/info` - Cache information

#### Flagent Plugin Endpoints

The plugin automatically creates these endpoints:

- `GET /flagent/health` - Flagent health check
- `POST /flagent/evaluate` - Single flag evaluation
- `POST /flagent/evaluate/batch` - Batch flag evaluation

### Examples

#### Single Flag Evaluation

```bash
# Basic evaluation
curl "http://localhost:8080/feature/my_feature_flag?entityID=user123"

# With entityContext for constraint-based targeting (country, tier, etc.)
curl "http://localhost:8080/feature/my_feature_flag?entityID=user123&entityContext=%7B%22country%22%3A%22US%22%2C%22tier%22%3A%22premium%22%7D"
# Decoded: entityContext={"country":"US","tier":"premium"}

# Using plugin endpoint
curl -X POST http://localhost:8080/flagent/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "flagKey": "my_feature_flag",
    "entityID": "user123",
    "entityType": "user",
    "entityContext": {"country": "US", "tier": "premium"}
  }'
```

Response:
```json
{
  "enabled": true,
  "variant": "enabled",
  "message": "Feature my_feature_flag = enabled"
}
```

**Constraint-based evaluation:** When your flag has segments with constraints (e.g., `country` EQ `US`), pass `entityContext` with matching attributes. The evaluator will match the segment and return the assigned variant.

#### Batch Evaluation

```bash
curl -X POST http://localhost:8080/feature-batch
```

Response:
```json
{
  "results": [...],
  "count": 3
}
```

#### Health Check

```bash
curl http://localhost:8080/flagent/health
```

## Plugin Configuration

The sample configures the Flagent plugin in `Application.kt`:

```kotlin
installFlagent {
    flagentBaseUrl = "http://localhost:18000"
    enableEvaluation = true
    enableCache = true
    cacheTtlMs = 60000 // 1 minute
}
```

### Configuration Options

- `flagentBaseUrl` - Base URL of Flagent server (default: "http://localhost:18000")
- `enableEvaluation` - Enable evaluation endpoints (default: true)
- `enableCache` - Enable caching (default: true)
- `cacheTtlMs` - Cache TTL in milliseconds (default: 60000)
- `connectTimeoutMs` - Connection timeout (default: 5000)
- `requestTimeoutMs` - Request timeout (default: 10000)

## Using the Plugin in Your Routes

### Access Flagent Client

```kotlin
routing {
    get("/my-feature") {
        val client = getFlagentClient()
        val result = client?.evaluate(
            EvaluationRequest(
                flagKey = "my_feature",
                entityID = "user123"
            )
        )
        
        if (result?.variantKey == "enabled") {
            call.respond("Feature enabled")
        } else {
            call.respond("Feature disabled")
        }
    }
}
```

### Access Flagent Cache

```kotlin
routing {
    get("/cache") {
        val cache = getFlagentCache()
        val value = cache?.get("my-key")
        call.respond(mapOf("value" to value))
    }
}
```

## Project Structure

```
ktor/
├── src/main/kotlin/flagent/sample/
│   └── Application.kt        # Main application with routes
├── build.gradle.kts         # Build configuration
├── settings.gradle.kts      # Project settings
└── README.md                # This file
```

## Troubleshooting

### Connection Issues

- Ensure Flagent backend is running
- Check base URL is correct (`http://localhost:18000`)
- Verify network connectivity

### Build Issues

- Ensure JDK 17+ is configured
- Run `./gradlew clean build` to rebuild from scratch
- Check ktor-flagent plugin is correctly included in `settings.gradle.kts`

### Plugin Dependencies

The sample uses **local ktor-flagent plugin** from the same repository:
- `../../ktor-flagent` - Ktor Flagent plugin

This is included as a Gradle subproject in `settings.gradle.kts`. This allows you to:
- Make changes to the plugin and see them immediately in the sample app
- Develop plugin and sample app together without publishing to Maven
- Test plugin changes before releasing

**Note**: If the plugin fails to compile, fix it first as the sample app depends on it.

## License

Apache 2.0 - See parent project license
