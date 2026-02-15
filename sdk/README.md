# Flagent SDK Clients

> [English](README.md) | [Русский](README.ru.md)

Client libraries (SDK) for Flagent API - a feature flagging, A/B testing and dynamic configuration microservice.

## Available SDKs

| Language | SDK | Status | Description |
|----------|-----|--------|-------------|
| Kotlin | [kotlin/](./kotlin/) | ✅ Available | Kotlin/JVM client library (Android, JVM) |
| Java | [java/](./java/) | ✅ Available | From Java via Kotlin Enhanced (`buildBlocking()`) |
| JavaScript/TypeScript | [javascript/](./javascript/) | ✅ Available | TypeScript/JavaScript client library |
| Swift | [swift/](./swift/) | ✅ Available | Native Swift client library (iOS, macOS) |
| Python | [python/](./python/) | ✅ Available | Python client library with async/await support |
| Go | [go/](./go/) | ✅ Available | Go client library with context support |
| Dart | [dart/](./dart/) | ✅ Available | Dart client library (Flutter, iOS, Android, Web) |

## Enhanced SDKs

Enhanced SDKs provide caching, convenient API, and optional client-side evaluation and real-time updates:

| Language | Enhanced SDK | Status | Description |
|----------|--------------|--------|-------------|
| Kotlin | [kotlin-enhanced/](./kotlin-enhanced/) | ✅ Available | Caching, **client-side evaluation**, **SSE real-time updates** |
| Swift | [swift-enhanced/](./swift-enhanced/) | ✅ Available | Enhanced Swift SDK with caching |
| JavaScript/TypeScript | [javascript-enhanced/](./javascript-enhanced/) | ✅ Available | Enhanced TypeScript SDK with caching |
| Go | [go-enhanced/](./go-enhanced/) | ✅ Available | Caching, **client-side evaluation**, **SSE real-time updates** |
| Dart/Flutter | [flutter-enhanced/](./flutter-enhanced/) | ✅ Available | Enhanced Dart SDK with caching |

## Debug UI Libraries

Debug UI libraries provide visual debugging interfaces for development:

| Language | Debug UI | Status | Description |
|----------|----------|--------|-------------|
| Kotlin | [kotlin-debug-ui/](./kotlin-debug-ui/) | ✅ Available | Compose Debug UI for Android |
| Swift | [swift-debug-ui/](./swift-debug-ui/) | ✅ Available | SwiftUI Debug UI for iOS |
| JavaScript/TypeScript | [javascript-debug-ui/](./javascript-debug-ui/) | ✅ Available | React Debug UI for Web |

## Quick Start

### Basic SDK Usage

#### Kotlin SDK

See [Kotlin SDK README](./kotlin/README.md) for installation and usage.

#### Enhanced Kotlin SDK

See [Kotlin Enhanced SDK README](./kotlin-enhanced/README.md) for installation and usage with caching.

#### Java (Kotlin SDK)

See [Java SDK README](./java/README.md) — use Kotlin Enhanced with `buildBlocking()` for a blocking client.

#### Swift SDK

See [Swift SDK README](./swift/README.md) for installation and usage.

#### Enhanced Swift SDK

See [Swift Enhanced SDK README](./swift-enhanced/README.md) for installation and usage with caching.

#### JavaScript/TypeScript SDK

See [JavaScript SDK README](./javascript/README.md) for installation and usage.

#### Enhanced JavaScript/TypeScript SDK

See [JavaScript Enhanced SDK README](./javascript-enhanced/README.md) for installation and usage with caching.

#### Python SDK

See [Python SDK README](./python/README.md) for installation and usage.

#### Go SDK

See [Go SDK README](./go/README.md) for installation and usage.

#### Enhanced Go SDK

See [Go Enhanced SDK README](./go-enhanced/README.md) for installation and usage with caching.

#### Dart SDK

See [Dart SDK README](./dart/README.md) for installation and usage.

#### Enhanced Flutter/Dart SDK

See [Flutter Enhanced SDK README](./flutter-enhanced/README.md) for installation and usage with caching.

#### React Native

See [REACT_NATIVE.md](./REACT_NATIVE.md) — use JavaScript SDK (`@flagent/client` + `@flagent/enhanced-client`).

### Debug UI Usage

#### Kotlin Debug UI

See [Kotlin Debug UI README](./kotlin-debug-ui/README.md) for installation and usage.

#### Swift Debug UI

See [Swift Debug UI README](./swift-debug-ui/README.md) for installation and usage.

#### JavaScript Debug UI

See [JavaScript Debug UI README](./javascript-debug-ui/README.md) for installation and usage.

## Architecture

### SDK Structure

1. **Base SDK** (generated from OpenAPI)
   - HTTP client with types
   - Low-level API access
   - Generated automatically

2. **Enhanced SDK** (native implementation)
   - Caching layer
   - Manager with convenient API
   - Offline support
   - **Kotlin Enhanced & Go Enhanced:** client-side evaluation (local evaluator) and real-time updates (SSE)
   - Logging

3. **Debug UI Library** (optional)
   - Visual debugging interface
   - Flag list and details
   - Local overrides
   - Evaluation logs

### Dependencies

```
Base SDK (generated)
    ↓
Enhanced SDK (caching, manager)
    ↓
Debug UI (optional)
```

Users can use:
- Only Base SDK (minimum dependencies)
- Base + Enhanced SDK (caching, convenient API)
- Base + Enhanced + Debug UI (full feature set)

## API Compatibility

All SDKs are generated from the OpenAPI specification (available at `http://localhost:18000/api/v1/openapi.yaml` when server is running) and are compatible with Flagent API version 0.1.x.

## Generation

Base SDKs are automatically generated from the OpenAPI specification using [OpenAPI Generator](https://openapi-generator.tech/).

To regenerate base SDKs after updating the OpenAPI specification:

```bash
# Generate Kotlin SDK
cd sdk/kotlin
./generate.sh

# Generate JavaScript SDK
cd sdk/javascript
./generate.sh

# Generate Swift SDK
cd sdk/swift
./generate.sh

# Generate Dart SDK
cd sdk/dart
./generate.sh

# Generate Go SDK
cd sdk/go
./generate.sh

# Generate Python SDK
cd sdk/python
./generate.sh
```

Or use the Makefile from `sdk/` directory:

```bash
cd sdk
make generate
```

Note: Go and Python base SDKs are generated from OpenAPI; the `client` layer (FlagentClient, convenient methods) is a thin wrapper over the generated API.

Enhanced SDKs and Debug UI libraries are manually implemented and depend on the base SDKs.

## Platform Support

### Kotlin SDK (JVM)
- ✅ JVM (server applications)
- ✅ Android (via JVM)

### Swift SDK (Native iOS)
- ✅ iOS (native Swift)
- ✅ macOS

**For Android and iOS:** 
- Android: use Kotlin SDK
- iOS: use Swift SDK (native)

### JavaScript/TypeScript SDK
- ✅ Node.js
- ✅ Browser
- ✅ React Native (see [REACT_NATIVE.md](./REACT_NATIVE.md))

### Dart SDK (Flutter)
- ✅ Flutter (iOS, Android, Web)

## Testing

See [TESTING.md](./TESTING.md) for instructions on testing SDKs.

## Reference

All base SDKs are generated from the OpenAPI specification and follow standard client library patterns. Enhanced SDKs and Debug UI libraries provide additional functionality on top of the base SDKs.