# ktor-flagent

Ktor плагин для функциональности Flagent - feature flags, A/B тестирования и динамической конфигурации.

## Описание

Этот плагин предоставляет Ktor функциональность для работы с feature flags, включая:
- Evaluation (оценка флагов)
- Caching (кэширование результатов)
- HTTP клиент для Flagent сервера
- Автоматическое кэширование evaluation результатов

## Установка

Добавьте зависимость в `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.flagent:ktor-flagent:0.1.6")
}
```

## Использование

### Базовая настройка

```kotlin
import io.ktor.server.application.*
import io.ktor.flagent.*

fun Application.module() {
    installFlagent {
        flagentBaseUrl = "http://localhost:18000"
        enableEvaluation = true
        enableCache = true
        cacheTtlMs = 60000 // 1 minute
    }
}
```

### Использование в routes

```kotlin
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.flagent.*

fun Application.module() {
    installFlagent {
        flagentBaseUrl = "http://localhost:18000"
    }
    
    routing {
        get("/my-feature") {
            val client = getFlagentClient()
            val result = client?.evaluate(
                EvaluationRequest(
                    flagKey = "my_feature_flag",
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
}
```

### Использование кэша

```kotlin
val cache = getFlagentCache()
val cached = cache?.get("cache_key")
```

### Endpoints плагина

Плагин автоматически создает следующие endpoints:

- `GET /flagent/health` - health check endpoint
- `POST /flagent/evaluate` - оценка одного флага
- `POST /flagent/evaluate/batch` - пакетная оценка флагов (поддерживает flagIDs, flagKeys, flagTags)

## Конфигурация

- `flagentBaseUrl` - базовый URL Flagent сервера (по умолчанию: "http://localhost:18000")
- `enableEvaluation` - включить evaluation endpoints (по умолчанию: true)
- `enableCache` - включить кэширование (по умолчанию: true)
- `cacheTtlMs` - TTL кэша в миллисекундах (по умолчанию: 60000)
- `connectTimeoutMs` - таймаут подключения (по умолчанию: 5000)
- `requestTimeoutMs` - таймаут запроса (по умолчанию: 10000)

## Функциональность

### Реализовано

- ✅ HTTP клиент для Flagent сервера
- ✅ Evaluation endpoints (single и batch)
- ✅ In-memory кэширование с TTL
- ✅ Метрики (Prometheus и StatsD)
- ✅ Валидация запросов
- ✅ Health check endpoint
- ✅ Логирование
- ✅ Обработка ошибок
- ✅ Поддержка batch evaluation с flagIDs, flagKeys, flagTags
- ✅ Extension функции для доступа к компонентам

### Особенности

- **Автоматическое кэширование** - результаты evaluation кэшируются для улучшения производительности
- **Валидация** - все запросы валидируются перед обработкой
- **Метрики** - встроенная поддержка Prometheus и StatsD метрик
- **Retry логика** - автоматические повторы при временных сбоях (через Ktor client)
- **Health check** - endpoint для проверки доступности Flagent сервера

## Статус

Плагин полностью готов к использованию. Все необходимые компоненты реализованы и протестированы.
