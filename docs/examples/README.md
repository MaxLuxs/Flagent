# Flagent Usage Examples

> [English](README.md) | [Русский](README.ru.md)

**In this section:**
- **API & Ktor** — REST API, flags, segments, evaluation (below)
- **SDK Integration** — [Ktor, Spring Boot, Kotlin, JavaScript, Swift](sdk-integration.md) — code samples for each platform

## Table of Contents

- [Basic Usage](#basic-usage)
- [Configuration](#configuration)
- [Ktor Plugin](#ktor-plugin)
- [Data Recording](#data-recording)

## Basic Usage

### Creating and Configuring a Flag

```kotlin
// Create flag
POST /api/v1/flags
{
  "description": "New payment flow",
  "key": "new_payment_flow",
  "enabled": true
}

// Create variant
POST /api/v1/flags/{flagId}/variants
{
  "key": "control"
}

POST /api/v1/flags/{flagId}/variants
{
  "key": "treatment"
}

// Create segment
POST /api/v1/flags/{flagId}/segments
{
  "description": "US users",
  "rolloutPercent": 100
}

// Add constraint
POST /api/v1/flags/{flagId}/segments/{segmentId}/constraints
{
  "property": "country",
  "operator": "EQ",
  "value": "US"
}

// Configure distribution
PUT /api/v1/flags/{flagId}/segments/{segmentId}/distributions
{
  "distributions": [
    {
      "variantID": 1,
      "variantKey": "control",
      "percent": 50
    },
    {
      "variantID": 2,
      "variantKey": "treatment",
      "percent": 50
    }
  ]
}
```

### Evaluation

```kotlin
// Single evaluation
POST /api/v1/evaluation
{
  "flagID": 1,
  "entityContext": {
    "country": "US",
    "age": "25"
  }
}

// Batch evaluation (entities = entity contexts, flagKeys/flagIDs = which flags to evaluate)
POST /api/v1/evaluation/batch
{
  "entities": [
    {"entityID": "user1", "entityContext": {"country": "US"}},
    {"entityID": "user2", "entityContext": {"country": "CA"}}
  ],
  "flagKeys": ["flag1", "new_payment_flow"]
}
```

## Configuration

### Basic Configuration

```kotlin
// application.conf or environment variables
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent?user=flagent&password=password
PORT=18000
```

### Kafka Configuration

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=localhost:9092
FLAGENT_RECORDER_KAFKA_TOPIC=flagent-records
```

### Kinesis Configuration

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kinesis
FLAGENT_RECORDER_KINESIS_STREAM_NAME=flagent-records
FLAGENT_RECORDER_KINESIS_REGION=us-east-1
```

### PubSub Configuration

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=pubsub
FLAGENT_RECORDER_PUBSUB_PROJECT_ID=my-project
FLAGENT_RECORDER_PUBSUB_TOPIC_NAME=flagent-records
```

### Authentication Configuration

```kotlin
// JWT Authentication
FLAGENT_JWT_AUTH_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=your-secret-key
FLAGENT_JWT_AUTH_SIGNING_METHOD=HS256

// Basic Authentication
FLAGENT_BASIC_AUTH_ENABLED=true
FLAGENT_BASIC_AUTH_USERNAME=admin
FLAGENT_BASIC_AUTH_PASSWORD=password

// Cookie Authentication
FLAGENT_COOKIE_AUTH_ENABLED=true
FLAGENT_COOKIE_AUTH_USER_FIELD=CF_Authorization
FLAGENT_COOKIE_AUTH_USER_FIELD_JWT_CLAIM=email
```

## Ktor Plugin

### Usage in Ktor Application

```kotlin
import io.ktor.flagent.*
import io.ktor.server.application.*

fun Application.module() {
    install(FlagentPlugin) {
        baseUrl = "http://localhost:18000/api/v1"
        cacheEnabled = true
        cacheTTL = 60_000 // 60 seconds
    }
    
    routing {
        get("/feature") {
            val flags = call.flagent.manager()
            if (flags.isEnabled("new_payment_flow")) {
                call.respond("New payment flow enabled")
            } else {
                call.respond("Legacy payment flow")
            }
        }
    }
}
```

### Using FlagentClient

```kotlin
val client = FlagentClient(
    baseUrl = "http://localhost:18000/api/v1",
    httpClient = HttpClient()
)

// Evaluate flag
val result = client.evaluate(
    flagID = 1,
    entityContext = mapOf("country" to "US")
)

// Use result
when (result.variantKey) {
    "control" -> useLegacyFlow()
    "treatment" -> useNewFlow()
}
```

### Using FlagentCache

```kotlin
val cache = FlagentCache(
    client = client,
    ttl = 60_000,
    refreshInterval = 30_000
)

// Get cached flag
val flag = cache.getFlag(flagID = 1)

// Check if enabled
if (flag?.enabled == true) {
    // Use feature
}
```

## Data Recording

### Kafka Recorder

```kotlin
val recorder = KafkaRecorder(
    brokers = listOf("localhost:9092"),
    topic = "flagent-records",
    async = true
)

recorder.record(evalResult)
```

### Kinesis Recorder

```kotlin
val recorder = KinesisRecorder(
    streamName = "flagent-records",
    region = "us-east-1",
    async = true
)

recorder.record(evalResult)
```

### PubSub Recorder

```kotlin
val recorder = PubSubRecorder(
    projectId = "my-project",
    topic = "flagent-records",
    async = true
)

recorder.record(evalResult)
```

## See Also

- [API Documentation](../api/endpoints.md)
- [Architecture Documentation](../architecture/backend.md)
- [Deployment Guide](../guides/deployment.md)
