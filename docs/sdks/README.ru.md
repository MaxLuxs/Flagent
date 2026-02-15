# Flagent SDK

> [English](README.md) | Русский

Официальные клиентские SDK для Flagent: evaluation feature flags, A/B тесты и динамическая конфигурация в приложении.

## Обзор

| SDK | Платформы | Установка | Документация |
|-----|-----------|----------|--------------|
| **Kotlin** | JVM, Android | Gradle: `io.flagent:flagent-kotlin` | [SDKs](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/kotlin) |
| **JavaScript/TS** | Браузер, Node.js | npm: `@flagent/sdk-js` | [SDKs](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/javascript) |
| **Swift** | iOS, macOS | SPM / CocoaPods | [SDKs](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/swift) |
| **Python** | 3.8+ | pip: `flagent-sdk` | [SDKs](../sdk.html) |
| **Go** | Go 1.18+ | `go get` | [SDKs](../sdk.html) |
| **Java** | JVM, Android | Maven/Gradle | [SDKs](../sdk.html) |
| **Dart/Flutter** | Flutter | pub.dev | [SDKs](../sdk.html) |

Android-пример (Kotlin): [samples/android](https://github.com/MaxLuxs/Flagent/tree/main/samples/android).

- **Base SDK** — генерируются из OpenAPI; типизированный API-клиент.
- **Enhanced SDK** — нативная реализация с кэшем, offline и упрощённым API где доступно.
- **Debug UI** — опциональные библиотеки для локальной отладки (Kotlin, JS).

## Ссылки

- [Обзор SDK (установка и использование)](../sdk.html)
- [Примеры интеграции](../examples/sdk-integration.md) — Ktor, Spring Boot, Kotlin, JavaScript, Swift
- [API Reference](../api-docs.html) — REST API (Swagger)
- [OpenAPI spec](../api/openapi.yaml) — для генерации кода или своих клиентов

## Поток evaluation

1. **Настройка** — base URL (например `http://localhost:18000`), API key или auth при необходимости.
2. **Bootstrap** (Enhanced) — опционально загрузить snapshot один раз; затем evaluation локально с опциональным обновлением по TTL.
3. **Evaluate** — вызов `evaluate(flagKey, entityId, entityType, context)` или эквивалент в SDK.
4. **Результат** — variant key, boolean или JSON в зависимости от типа флага.

## Версии

Версии backend и SDK заданы в корне репозитория: `gradle/libs.versions.toml`. Для совместимости используйте те же мажорные версии.

## Помощь

- [Getting Started](../guides/getting-started.md)
- [Configuration](../guides/configuration.md)
- [FAQ](../guides/faq.md)
- [GitHub](https://github.com/MaxLuxs/Flagent)
