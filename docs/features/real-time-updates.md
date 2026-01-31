# Real-Time Updates (SSE)

## Обзор

Flagent поддерживает real-time обновления флагов через Server-Sent Events (SSE). Это позволяет клиентам автоматически получать обновления при изменении флагов без polling или перезапуска приложения.

**Текущая реализация:** SSE доступен в [Kotlin Enhanced](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/REALTIME_UPDATES.md) и [Go Enhanced](https://github.com/MaxLuxs/Flagent/blob/main/sdk/go-enhanced/REALTIME_UPDATES.md) SDK. WebSocket — в планах.

## Ключевые возможности

### 1. WebSocket Support

Двусторонняя связь через WebSocket для real-time обновлений и взаимодействия.

**Преимущества:**
- Низкая латентность
- Двусторонняя связь
- Эффективное использование ресурсов
- Поддержка множественных соединений

**Протокол:**

```
ws://flags.example.com/ws
```

**Пример подключения:**

```kotlin
// Kotlin SDK
val wsClient = FlagentWebSocketClient(
    url = "wss://flags.example.com/ws",
    apiKey = "your-api-key"
)

// Подписка на обновления флага
wsClient.subscribe("new_payment_flow") { flag ->
    println("Flag updated: ${flag.key} - enabled: ${flag.enabled}")
    // Обновить локальный кэш
    localCache.update(flag)
}

// Отправка evaluation requests
wsClient.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US")
) { result ->
    println("Evaluation result: ${result.variantKey}")
}
```

### 2. Server-Sent Events (SSE)

Односторонний поток обновлений через SSE для простых use cases.

**Преимущества:**
- Простая реализация
- Автоматическое переподключение
- HTTP-based (легче за firewall)
- Низкий overhead

**Протокол:**

```
GET /api/v1/events/stream
```

**Пример использования:**

```kotlin
// Kotlin SDK
val sseClient = FlagentSSEClient(
    url = "https://flags.example.com/api/v1/events/stream",
    apiKey = "your-api-key"
)

sseClient.subscribe { event ->
    when (event.type) {
        EventType.FLAG_UPDATED -> {
            val flag = event.data as Flag
            println("Flag updated: ${flag.key}")
            localCache.update(flag)
        }
        EventType.FLAG_DELETED -> {
            val flagKey = event.data as String
            println("Flag deleted: $flagKey")
            localCache.remove(flagKey)
        }
    }
}
```

### 3. Subscription Management

Гибкая система подписок для управления обновлениями.

**Типы подписок:**
- Подписка на конкретный флаг
- Подписка на флаги по тегам
- Подписка на все флаги
- Подписка на изменения сегментов

**Пример:**

```kotlin
// Подписка на конкретный флаг
wsClient.subscribeFlag("new_payment_flow") { flag ->
    // Handle update
}

// Подписка на флаги по тегам
wsClient.subscribeTags(listOf("payment", "experiment")) { flags ->
    flags.forEach { flag ->
        localCache.update(flag)
    }
}

// Подписка на все изменения
wsClient.subscribeAll { event ->
    // Handle all flag changes
}
```

### 4. Automatic Reconnection

Автоматическое переподключение при разрыве соединения.

**Возможности:**
- Exponential backoff
- Максимальное количество попыток
- Сохранение подписок
- Обработка ошибок

**Пример конфигурации:**

```kotlin
val wsClient = FlagentWebSocketClient(
    url = "wss://flags.example.com/ws",
    reconnectConfig = ReconnectConfig(
        maxAttempts = 10,
        initialDelay = 1.second,
        maxDelay = 60.seconds,
        multiplier = 2.0
    )
)
```

### 5. Event Types

Различные типы событий для разных use cases.

**Типы событий:**
- `FLAG_UPDATED` - Флаг обновлен
- `FLAG_CREATED` - Флаг создан
- `FLAG_DELETED` - Флаг удален
- `FLAG_ENABLED` - Флаг включен
- `FLAG_DISABLED` - Флаг выключен
- `SEGMENT_UPDATED` - Сегмент обновлен
- `DISTRIBUTION_UPDATED` - Распределение обновлено

**Пример обработки:**

```kotlin
wsClient.onEvent { event ->
    when (event.type) {
        EventType.FLAG_UPDATED -> handleFlagUpdated(event.data as Flag)
        EventType.FLAG_ENABLED -> handleFlagEnabled(event.flagKey)
        EventType.FLAG_DISABLED -> handleFlagDisabled(event.flagKey)
        EventType.SEGMENT_UPDATED -> handleSegmentUpdated(event.data as Segment)
    }
}
```

## API

### WebSocket API

**Endpoint:**
```
wss://flags.example.com/ws
```

**Протокол сообщений:**

```json
// Client -> Server: Subscribe
{
  "type": "subscribe",
  "flags": ["flag1", "flag2"],
  "tags": ["payment"],
  "subscribeAll": false
}

// Server -> Client: Flag Update
{
  "type": "flag_updated",
  "flag": {
    "key": "new_payment_flow",
    "enabled": true,
    "segments": [...]
  }
}

// Client -> Server: Evaluation Request
{
  "type": "evaluate",
  "flagKey": "new_payment_flow",
  "entityID": "user123",
  "entityContext": {
    "country": "US"
  }
}

// Server -> Client: Evaluation Result
{
  "type": "evaluation_result",
  "requestId": "req-123",
  "result": {
    "flagID": 1,
    "flagKey": "new_payment_flow",
    "variantID": 2,
    "variantKey": "treatment"
  }
}
```

### SSE API

**Endpoint:**
```
GET /api/v1/events/stream
```

**Headers:**
```
Accept: text/event-stream
Authorization: Bearer {token}
```

**Response format:**
```
event: flag_updated
data: {"key": "new_payment_flow", "enabled": true}

event: flag_created
data: {"key": "new_feature", "enabled": false}

event: flag_deleted
data: {"key": "old_feature"}
```

## SDK Integration

### Kotlin SDK

```kotlin
// WebSocket client
val wsClient = FlagentWebSocketClient(
    url = "wss://flags.example.com/ws",
    apiKey = "your-api-key"
)

// Подписка на обновления
wsClient.subscribe("new_payment_flow") { flag ->
    updateLocalFlag(flag)
}

// SSE client
val sseClient = FlagentSSEClient(
    url = "https://flags.example.com/api/v1/events/stream",
    apiKey = "your-api-key"
)

sseClient.subscribe { event ->
    handleEvent(event)
}
```

### JavaScript SDK

```javascript
// WebSocket client
const wsClient = new FlagentWebSocketClient({
  url: 'wss://flags.example.com/ws',
  apiKey: 'your-api-key'
});

// Subscribe to flag updates
wsClient.subscribe('new_payment_flow', (flag) => {
  updateLocalFlag(flag);
});

// SSE client
const sseClient = new FlagentSSEClient({
  url: 'https://flags.example.com/api/v1/events/stream',
  apiKey: 'your-api-key'
});

sseClient.subscribe((event) => {
  handleEvent(event);
});
```

## Конфигурация

### Server Configuration

```kotlin
data class WebSocketConfig(
    val enabled: Boolean = true,
    val port: Int = 8080,
    val path: String = "/ws",
    val pingInterval: Duration = 30.seconds,
    val maxConnections: Int = 10000
)

data class SSEConfig(
    val enabled: Boolean = true,
    val path: String = "/api/v1/events/stream",
    val keepAliveInterval: Duration = 30.seconds,
    val maxClients: Int = 10000
)
```

### Environment Variables

```bash
# WebSocket
FLAGENT_WS_ENABLED=true
FLAGENT_WS_PORT=8080
FLAGENT_WS_PATH=/ws
FLAGENT_WS_PING_INTERVAL=30s

# SSE
FLAGENT_SSE_ENABLED=true
FLAGENT_SSE_PATH=/api/v1/events/stream
FLAGENT_SSE_KEEP_ALIVE=30s
```

## Use Cases

### 1. Real-time Flag Updates

Автоматическое обновление флагов в приложении без перезапуска.

```kotlin
wsClient.subscribe("new_payment_flow") { flag ->
    if (flag.enabled) {
        enableNewPaymentFlow()
    } else {
        disableNewPaymentFlow()
    }
}
```

### 2. Live Configuration

Динамическое изменение конфигурации без деплоя.

```kotlin
wsClient.subscribeAll { event ->
    when (event.type) {
        EventType.FLAG_UPDATED -> {
            val flag = event.data as Flag
            updateConfiguration(flag)
        }
    }
}
```

### 3. Debugging

Real-time мониторинг изменений флагов для debugging.

```kotlin
wsClient.subscribe("debug_flag") { flag ->
    logger.debug("Flag updated: ${flag.key} - ${flag.enabled}")
    // Send to monitoring system
    monitoringSystem.recordFlagChange(flag)
}
```

## Roadmap

### Фаза 1: WebSocket Support (В планах)
- ⏳ WebSocket server
- ⏳ Client SDK
- ⏳ Subscription management
- ⏳ Reconnection logic

### Фаза 2: SSE Support (В планах)
- ⏳ SSE endpoint
- ⏳ Client SDK
- ⏳ Event streaming
- ⏳ Keep-alive

### Фаза 3: Продвинутые возможности (В планах)
- ⏳ Event filtering
- ⏳ Batch updates
- ⏳ Compression
- ⏳ Authentication

## Связанная документация

- [Offline-First SDK](./offline-first-sdk.md)
- [Advanced Analytics](./advanced-analytics.md)
- [API Documentation](../api/endpoints.md)
