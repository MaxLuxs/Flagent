# Flagent SDKs

> [English](README.md) | [Русский](README.ru.md)

Official client SDKs for Flagent. Use them to evaluate feature flags, run A/B tests, and fetch dynamic configuration in your application.

## Overview

### Base SDKs (OpenAPI-generated)

| SDK | Platforms | Install | Docs |
|-----|-----------|--------|------|
| **Kotlin** | JVM, Android, KMP | Gradle: `com.flagent:kotlin-client` | [sdk/kotlin](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/kotlin) |
| **JavaScript/TS** | Browser, Node.js | npm: `@flagent/sdk-js` | [sdk/javascript](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/javascript) |
| **Swift** | iOS, macOS | SPM / CocoaPods | [sdk/swift](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/swift) |
| **Python** | 3.8+ | pip: `flagent-sdk` | [sdk/python](https://github.com/MaxLuxs/Flagent/tree/main/sdk/python) |
| **Go** | Go 1.18+ | `go get` | [sdk/go](https://github.com/MaxLuxs/Flagent/tree/main/sdk/go) |
| **Java** | JVM, Android | Maven/Gradle | [sdk/java](https://github.com/MaxLuxs/Flagent/tree/main/sdk/java) |
| **Dart** | Flutter, Web | pub.dev | [sdk/dart](https://github.com/MaxLuxs/Flagent/tree/main/sdk/dart) |

### Enhanced SDKs (caching, offline, client-side eval where available)

| SDK | Features |
|-----|----------|
| **Kotlin Enhanced** [sdk/kotlin-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin-enhanced) | Client-side eval, SSE real-time |
| **JavaScript Enhanced** [sdk/javascript-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript-enhanced) | Caching, convenient API |
| **Swift Enhanced** [sdk/swift-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift-enhanced) | Caching |
| **Go Enhanced** [sdk/go-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/go-enhanced) | Client-side eval, SSE |
| **Flutter Enhanced** [sdk/flutter-enhanced](https://github.com/MaxLuxs/Flagent/tree/main/sdk/flutter-enhanced) | Caching |

### Other server / DI

| SDK | Description |
|-----|-------------|
| **flagent-koin** [sdk/flagent-koin](https://github.com/MaxLuxs/Flagent/tree/main/sdk/flagent-koin) | Koin DI module (KMP) |
| **Spring Boot Starter** [sdk/spring-boot-starter](https://github.com/MaxLuxs/Flagent/tree/main/sdk/spring-boot-starter) | Auto-configuration for Spring Boot |

### Debug UI (optional)

| SDK | Platforms |
|-----|-----------|
| **Kotlin Debug UI** [sdk/kotlin-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin-debug-ui) | Compose Multiplatform (JVM/Android/iOS) |
| **Swift Debug UI** [sdk/swift-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift-debug-ui) | SwiftUI (iOS) |
| **JavaScript Debug UI** [sdk/javascript-debug-ui](https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript-debug-ui) | React (Web) |

Android sample (Kotlin SDK): [samples/android](https://github.com/MaxLuxs/Flagent/tree/main/samples/android).

## Which SDK to use

See **[Which SDK to choose](../guides/sdk-selection.md)** for a table and short guide: Kotlin vs Kotlin Enhanced vs Ktor vs Spring Boot vs JS vs Swift vs Go, when to use server-side vs client-side evaluation, and recommended entry points (`Flagent.builder()`, `Flagent.create()`, `NewFlagent()`).

## Quick links

- [SDKs overview (install & usage per platform)](../sdk.html) — full list, dependencies, and code snippets.
- [SDK integration examples](../examples/sdk-integration.md) — Ktor, Spring Boot, Kotlin, JavaScript, Swift. Spring Boot: `com.flagent:flagent-spring-boot-starter`.
- [API Reference](../api-docs.html) — REST API (Swagger).
- [OpenAPI spec](../api/openapi.yaml) — for code generation or custom clients.

## Evaluation flow

1. **Configure** — set base URL (e.g. `http://localhost:18000`), API key or auth if required.
2. **Bootstrap** (Enhanced) — optionally load a snapshot once; then evaluate locally with optional TTL refresh.
3. **Evaluate** — call `evaluate(flagKey, entityId, entityType, context)` or SDK-specific equivalent.
4. **Use result** — variant key, boolean, or JSON value depending on flag type.

## Versions

Project version: root file `VERSION`. Sync to other formats (npm, pip, Go, etc.) via `./scripts/sync-version.sh`. See [Versioning](../guides/versioning.md).

## Need help?

- [Getting Started](../guides/getting-started.md)
- [Configuration](../guides/configuration.md)
- [FAQ](../guides/faq.md)
- [GitHub](https://github.com/MaxLuxs/Flagent)
