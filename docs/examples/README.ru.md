# Примеры использования Flagent

## Содержание

- [Базовое использование](#базовое-использование)
- [Конфигурация](#конфигурация)
- [Ktor Plugin](#ktor-plugin)
- [Data Recording](#data-recording)

## Базовое использование

### Создание и настройка флага

```kotlin
// Создать флаг
POST /api/v1/flags
{
  "description": "New payment flow",
  "key": "new_payment_flow",
  "enabled": true
}

// Создать вариант
POST /api/v1/flags/{flagId}/variants
{
  "key": "control"
}

POST /api/v1/flags/{flagId}/variants
{
  "key": "treatment"
}

// Создать сегмент
POST /api/v1/flags/{flagId}/segments
{
  "description": "US users",
  "rolloutPercent": 100
}

// Добавить constraint
POST /api/v1/flags/{flagId}/segments/{segmentId}/constraints
{
  "property": "country",
  "operator": "EQ",
  "value": "US"
}

// Настроить распределение
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
// Одиночная evaluation
POST /api/v1/evaluation
{
  "flagID": 1,
  "entityContext": {
    "country": "US",
    "age": "25"
  }
}

// Batch evaluation (entities = контексты сущностей, flagKeys/flagIDs = какие флаги оценивать)
POST /api/v1/evaluation/batch
{
  "entities": [
    {"entityID": "user1", "entityContext": {"country": "US"}},
    {"entityID": "user2", "entityContext": {"country": "CA"}}
  ],
  "flagKeys": ["flag1", "new_payment_flow"]
}
```

## Конфигурация

### Базовая конфигурация

```kotlin
// application.conf или environment variables
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=postgresql://localhost:5432/flagent
FLAGENT_DB_USER=flagent
FLAGENT_DB_PASSWORD=password
PORT=18000
```

### Конфигурация с Kafka

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=localhost:9092
FLAGENT_RECORDER_KAFKA_TOPIC=flagent-events
```

### Конфигурация с Kinesis

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kinesis
FLAGENT_RECORDER_KINESIS_STREAM_NAME=flagent-events
FLAGENT_RECORDER_KINESIS_REGION=us-east-1
```

### Конфигурация с PubSub

```kotlin
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=pubsub
FLAGENT_RECORDER_PUBSUB_PROJECT_ID=my-project
FLAGENT_RECORDER_PUBSUB_TOPIC_NAME=flagent-events
```

### Конфигурация аутентификации

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

### Использование в Ktor приложении

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

### Использование FlagentClient

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

### Использование FlagentCache

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
    topic = "flagent-events",
    async = true
)

recorder.record(evalResult)
```

### Kinesis Recorder

```kotlin
val recorder = KinesisRecorder(
    streamName = "flagent-events",
    region = "us-east-1",
    async = true
)

recorder.record(evalResult)
```

### PubSub Recorder

```kotlin
val recorder = PubSubRecorder(
    projectId = "my-project",
    topic = "flagent-events",
    async = true
)

recorder.record(evalResult)
```

## Дополнительные примеры

См. также:
- [API Documentation](../api/endpoints.md)
- [Architecture Documentation](../architecture/backend.md)
- [Deployment Guide](../deployment.md)
