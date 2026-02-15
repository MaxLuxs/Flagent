# Flagent SDKs

> [English](README.md) | [Русский](README.ru.md)

Official client SDKs for Flagent. Use them to evaluate feature flags, run A/B tests, and fetch dynamic configuration in your application.

## Overview

| SDK | Platforms | Install | Docs |
|-----|-----------|--------|------|
| **Kotlin** | JVM, Android | Gradle: `io.flagent:flagent-kotlin` | [SDKs page](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/kotlin) |
| **JavaScript/TS** | Browser, Node.js | npm: `@flagent/sdk-js` | [SDKs page](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/javascript) |
| **Swift** | iOS, macOS | SPM / CocoaPods | [SDKs page](../sdk.html) · [Sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/swift) |
| **Python** | 3.8+ | pip: `flagent-sdk` | [SDKs page](../sdk.html) |
| **Go** | Go 1.18+ | `go get` | [SDKs page](../sdk.html) |
| **Java** | JVM, Android | Maven/Gradle | [SDKs page](../sdk.html) |
| **Dart/Flutter** | Flutter | pub.dev | [SDKs page](../sdk.html) |

Android sample (Kotlin SDK): [samples/android](https://github.com/MaxLuxs/Flagent/tree/main/samples/android).

- **Base SDKs** — generated from OpenAPI; typed API client.
- **Enhanced SDKs** — native implementation with caching, offline support, and simpler APIs where available.
- **Debug UI** — optional Compose/UI libraries for local debugging (e.g. Kotlin, JS).

## Quick links

- [SDKs overview (install & usage per platform)](../sdk.html) — full list, dependencies, and code snippets.
- [SDK integration examples](../examples/sdk-integration.md) — Ktor, Spring Boot, Kotlin, JavaScript, Swift.
- [API Reference](../api-docs.html) — REST API (Swagger).
- [OpenAPI spec](../api/openapi.yaml) — for code generation or custom clients.

## Evaluation flow

1. **Configure** — set base URL (e.g. `http://localhost:18000`), API key or auth if required.
2. **Bootstrap** (Enhanced) — optionally load a snapshot once; then evaluate locally with optional TTL refresh.
3. **Evaluate** — call `evaluate(flagKey, entityId, entityType, context)` or SDK-specific equivalent.
4. **Use result** — variant key, boolean, or JSON value depending on flag type.

## Versions

Backend and SDK dependency versions are defined in the repo root: `gradle/libs.versions.toml`. When integrating, use the same major versions as in that file for compatibility.

## Need help?

- [Getting Started](../guides/getting-started.md)
- [Configuration](../guides/configuration.md)
- [FAQ](../guides/faq.md)
- [GitHub](https://github.com/MaxLuxs/Flagent)
