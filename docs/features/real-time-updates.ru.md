# Обновления в реальном времени (SSE)

> [English](real-time-updates.md) | Русский

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

**Протокол:** `ws://flags.example.com/ws`

**Пример подключения:**

```kotlin
val wsClient = FlagentWebSocketClient(
    url = "wss://flags.example.com/ws",
    apiKey = "your-api-key"
)
wsClient.subscribe("new_payment_flow") { flag ->
    println("Flag updated: ${flag.key} - enabled: ${flag.enabled}")
    localCache.update(flag)
}
wsClient.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US")
) { result -> println("Evaluation result: ${result.variantKey}") }
```

### 2. Server-Sent Events (SSE)

Односторонний поток обновлений через SSE. Endpoint: `GET /api/v1/events/stream`. Преимущества: простая реализация, авто-переподключение, HTTP-based.

**Пример:** `FlagentSSEClient` + `subscribe { event -> ... }` по типам `FLAG_UPDATED`, `FLAG_DELETED`.

### 3. Subscription Management

Подписка на конкретный флаг, на флаги по тегам, на все флаги, на изменения сегментов.

### 4. Automatic Reconnection

Exponential backoff, макс. попытки, сохранение подписок.

### 5. Event Types

`FLAG_UPDATED`, `FLAG_CREATED`, `FLAG_DELETED`, `FLAG_ENABLED`, `FLAG_DISABLED`, `SEGMENT_UPDATED`, `DISTRIBUTION_UPDATED`.

## API

WebSocket: `wss://flags.example.com/ws`. SSE: `GET /api/v1/events/stream`, заголовки `Accept: text/event-stream`, `Authorization: Bearer {token}`.

## SDK Integration

Kotlin: `FlagentWebSocketClient`, `FlagentSSEClient`. JavaScript: те же клиенты с `subscribe` / `subscribeAll`.

## Конфигурация

`WebSocketConfig`, `SSEConfig`. Переменные: `FLAGENT_WS_*`, `FLAGENT_SSE_*`.

## Use Cases

Real-time обновление флагов без перезапуска; live configuration; отладка.

## Roadmap

Фаза 1: WebSocket. Фаза 2: SSE. Фаза 3: фильтрация событий, batch updates, сжатие, аутентификация.

## Связанная документация

- [Offline-First SDK](offline-first-sdk.md)
- [API Documentation](../api/endpoints.md)
