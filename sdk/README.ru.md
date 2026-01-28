# Flagent SDK (клиентские библиотеки)

SDK для Flagent API — платформы feature flags, A/B тестирования и динамической конфигурации. Базовый путь API: `/api/v1`.

## Доступные SDK

| Язык | SDK | Статус | Описание |
|------|-----|--------|----------|
| Kotlin | [`kotlin/`](./kotlin/) | Доступен | Kotlin/JVM клиент (Android, JVM) |
| JavaScript/TypeScript | [`javascript/`](./javascript/) | Доступен | TypeScript/JavaScript клиент |
| Swift | [`swift/`](./swift/) | Доступен | Нативный Swift клиент (iOS, macOS) |
| Python | [`python/`](./python/) | Доступен | Async Python SDK (async/await) |
| Go | [`go/`](./go/) | Доступен | Go SDK с `context.Context` |

## Enhanced SDKs

Enhanced SDK добавляют кэширование и удобные API поверх base SDK:

| Язык | Enhanced SDK | Статус | Описание |
|------|--------------|--------|----------|
| Kotlin | [`kotlin-enhanced/`](./kotlin-enhanced/) | Доступен | Kotlin SDK с кэшем |
| Swift | [`swift-enhanced/`](./swift-enhanced/) | Доступен | Swift SDK с кэшем |
| JavaScript/TypeScript | [`javascript-enhanced/`](./javascript-enhanced/) | Доступен | TypeScript SDK с кэшем |
| Go | [`go-enhanced/`](./go-enhanced/) | Доступен | Go SDK с кэшем |

## Debug UI

Debug UI библиотеки дают визуальные инструменты для отладки evaluation в dev:

| Язык | Debug UI | Статус | Описание |
|------|----------|--------|----------|
| Kotlin | [`kotlin-debug-ui/`](./kotlin-debug-ui/) | Доступен | Compose Debug UI (Android) |
| Swift | [`swift-debug-ui/`](./swift-debug-ui/) | Доступен | SwiftUI Debug UI (iOS) |
| JavaScript/TypeScript | [`javascript-debug-ui/`](./javascript-debug-ui/) | Доступен | React Debug UI (Web) |

## Быстрый старт

### Python (async)

```python
import asyncio
from flagent import FlagentClient

async def main():
    async with FlagentClient(base_url="http://localhost:18000/api/v1") as client:
        result = await client.evaluate(
            flag_key="new_payment_flow",
            entity_id="user123",
            entity_context={"tier": "premium", "region": "RU"}
        )
        print(result.is_enabled(), result.variant_key)

asyncio.run(main())
```

### Go

```go
ctx := context.Background()
client, _ := flagent.NewClient("http://localhost:18000/api/v1")

res, _ := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey:  flagent.StringPtr("new_payment_flow"),
    EntityID: flagent.StringPtr("user123"),
    EntityContext: map[string]interface{}{
        "tier": "premium",
        "region": "RU",
    },
})

fmt.Println(res.IsEnabled(), *res.VariantKey)
```

### Kotlin (base SDK, generated)

Base Kotlin SDK генерируется из OpenAPI и имеет достаточно “генераторный” интерфейс. Для production обычно удобнее использовать `kotlin-enhanced/`.

См.:
- [`kotlin/README.ru.md`](./kotlin/README.ru.md)
- [`kotlin-enhanced/README.md`](./kotlin-enhanced/README.md)

### JavaScript/TypeScript (base SDK, generated)

См.:
- [`javascript/README.ru.md`](./javascript/README.ru.md)
- [`javascript-enhanced/README.md`](./javascript-enhanced/README.md)

### Swift (base SDK, generated)

См.:
- [`swift/README.ru.md`](./swift/README.ru.md)
- [`swift-enhanced/README.md`](./swift-enhanced/README.md)

## Совместимость API

Все base SDK генерируются из OpenAPI спецификации. Русская версия спецификации: [`docs/api/openapi.ru.yaml`](../../docs/api/openapi.ru.yaml).

