# Flagent SDK

Официальные клиентские SDK для Flagent: evaluation feature flags, A/B тесты и динамическая конфигурация в приложении.

## Обзор

### Базовые SDK (генерация из OpenAPI)

| SDK | Платформы | Установка | Документация |
|-----|-----------|----------|--------------|
| **Kotlin** | JVM, Android, KMP | Gradle: `com.flagent:kotlin-client` | [sdk/kotlin](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/kotlin) |
| **JavaScript/TS** | Браузер, Node.js | npm: `@flagent/sdk-js` | [sdk/javascript](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/javascript) |
| **Swift** | iOS, macOS | SPM / CocoaPods | [sdk/swift](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/swift) |
| **Python** | 3.8+ | pip: `flagent-sdk` | [sdk/python](https://github.com/MaxLuxs/Flagent/tree/main/sdk/python) |
| **Go** | Go 1.18+ | `go get` | [sdk/go](https://github.com/MaxLuxs/Flagent/tree/main/sdk/go) |
| **Java** | JVM, Android | Maven/Gradle | [sdk/java](https://github.com/MaxLuxs/Flagent/tree/main/sdk/java) |
| **Dart** | Flutter, Web | pub.dev | [sdk/dart](https://github.com/MaxLuxs/Flagent/tree/main/sdk/dart) |

### Enhanced SDK (кэш, offline, client-side eval где есть)

| SDK | Возможности |
|-----|-------------|
| **Kotlin Enhanced** [sdk/kotlin-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin-enhanced) | Client-side eval, SSE real-time |
| **JavaScript Enhanced** [sdk/javascript-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript-enhanced) | Кэш, удобный API |
| **Swift Enhanced** [sdk/swift-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift-enhanced) | Кэш |
| **Go Enhanced** [sdk/go-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/go-enhanced) | Client-side eval, SSE |
| **Flutter Enhanced** [sdk/flutter-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/flutter-enhanced) | Кэш |

### Сервер / DI

| SDK | Описание |
|-----|----------|
| **flagent-koin** [sdk/flagent-koin](https://github.com/MaxLuxs/Flagent/tree/main/sdk/flagent-koin) | Koin DI модуль (KMP) |
| **Spring Boot Starter** [sdk/spring-boot-starter](https://github.com/MaxLuxs/Flagent/tree/main/sdk/spring-boot-starter) | Автоконфигурация для Spring Boot |

### Debug UI (опционально)

| SDK | Платформы |
|-----|-----------|
| **Kotlin Debug UI** [sdk/kotlin-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin-debug-ui) | Compose Multiplatform (JVM/Android/iOS) |
| **Swift Debug UI** [sdk/swift-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift-debug-ui) | SwiftUI (iOS) |
| **JavaScript Debug UI** [sdk/javascript-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript-debug-ui) | React (Web) |

Android-пример (Kotlin): [samples/android](https://github.com/MaxLuxs/Flagent/tree/main/samples/android).

## Какой SDK выбрать

См. **[Какой SDK выбрать](../guides/sdk-selection.md)** — таблица и краткий гайд: Kotlin vs Kotlin Enhanced vs Ktor vs Spring Boot vs JS vs Swift vs Go, когда использовать server-side и client-side evaluation, рекомендуемые точки входа.

## Ссылки

- [Обзор SDK (установка и использование)](../sdk.html)
- [Примеры интеграции](../examples/sdk-integration.md) — Ktor, Spring Boot, Kotlin, JavaScript, Swift. Spring Boot: `com.flagent:flagent-spring-boot-starter`.
- [API Reference](../api-docs.html) — REST API (Swagger)
- [OpenAPI spec](../api/openapi.yaml) — для генерации кода или своих клиентов

## Поток evaluation

1. **Настройка** — base URL (например `http://localhost:18000`), API key или auth при необходимости.
2. **Bootstrap** (Enhanced) — опционально загрузить snapshot один раз; затем evaluation локально с опциональным обновлением по TTL.
3. **Evaluate** — вызов `evaluate(flagKey, entityId, entityType, context)` или эквивалент в SDK.
4. **Результат** — variant key, boolean или JSON в зависимости от типа флага.

## Версии

Версия проекта: корневой файл `VERSION`. Синхронизация в другие форматы: `./scripts/sync-version.sh`. См. [Versioning](../guides/versioning.md).

## Помощь

- [Getting Started](../guides/getting-started.md)
- [Configuration](../guides/configuration.md)
- [FAQ](../guides/faq.md)
- [GitHub](https://github.com/MaxLuxs/Flagent)
