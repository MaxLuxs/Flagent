# Flagent Swift Enhanced SDK

Enhanced Swift client library for Flagent API with caching and a unified entry point.

## Installation

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

Add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/MaxLuxs/Flagent.git", from: "0.1.6"),
]
```

## Usage (recommended)

Create a client with `Flagent.builder()`, then use `evaluate`, `isEnabled`, or `evaluateBatch`:

```swift
import FlagentEnhanced
import FlagentClient

let client = Flagent.builder()
    .baseURL("https://your-api.example.com/api/v1")
    .bearerToken("your-token")           // optional
    .cache(enable: true, ttlMs: 300_000) // optional: 5 min
    .build()

// Single flag evaluation
let result = try await client.evaluate(
    flagKey: "new_feature",
    entityID: "user123",
    entityType: "user"
)

// Convenience: boolean check (true when a variant is returned)
let enabled = try await client.isEnabled(
    flagKey: "new_feature",
    entityID: "user123"
)

// Batch evaluation
let entities = [EvaluationEntity(entityID: "user123", entityType: "user")]
let results = try await client.evaluateBatch(
    flagKeys: ["flag_a", "flag_b"],
    entities: entities
)
```

### Builder options

- **`baseURL(_:)`** — API base URL (required).
- **`header(name:value:)`** — Custom header (e.g. API key).
- **`bearerToken(_:)`** — Sets `Authorization: Bearer <token>`.
- **`credential(_:)`** — HTTP basic auth.
- **`cache(enable:ttlMs:)`** — In-memory cache and TTL in milliseconds (default: enabled, 5 min).
- **`mode(_:)`** — `.server` (default) or `.offline` (client-side evaluation from exported snapshot).

> **Note:** The builder configures the global Flagent API (base path, headers). Only one base URL is effective at a time.
>
> **Offline mode:** Use `.mode(.offline)` for client-side evaluation. The client fetches a snapshot (Export API or FlagAPI fallback) on first `evaluate` or after `bootstrap()`. You can call `(client as? FlagentManagerAdapter) …` and access the underlying `OfflineFlagentManager` to call `bootstrap()` explicitly before use.

## Offline (client-side) mode

For evaluation without a server round-trip per request, use `.mode(.offline)`:

```swift
let client = Flagent.builder()
    .baseURL("https://your-api.example.com/api/v1")
    .mode(.offline)
    .build()

// First evaluate/isEnabled triggers bootstrap (fetches snapshot); then evaluation is local
let enabled = try await client.isEnabled(flagKey: "feature_x", entityID: "user1")
```

Offline client fetches a snapshot once from `GET /export/eval_cache/json` (or falls back to `GET /flags?preload=true`), then evaluates flags locally (constraints + rollout). Same API: `evaluate`, `isEnabled`, `evaluateBatch`. To bootstrap explicitly before any call, create `OfflineFlagentManager` directly and call `bootstrap()` (see advanced usage).

## Advanced: direct FlagentManager

If you need full control (e.g. you set `FlagentClientAPI.basePath` yourself), you can still use `FlagentManager` and `FlagentConfig`:

```swift
import FlagentEnhanced
import FlagentClient

// Configure base client first
FlagentClientAPI.basePath = "https://your-api.example.com/api/v1"

let config = FlagentConfig(cacheTtlMs: 300, enableCache: true)
let manager = FlagentManager(config: config)

let result = try await manager.evaluate(
    flagKey: "new_feature",
    entityID: "user123",
    entityType: "user"
)
```

For a unified API including `isEnabled`, wrap the manager in `FlagentManagerAdapter(manager:)` to get a `FlagentClient`.

## Features

- **Unified entry point** — `Flagent.builder()...build()` returns a single client.
- **evaluate / isEnabled / evaluateBatch** — No need to use EvaluationAPI directly.
- **Caching** — Configurable TTL, in-memory.
- **Auth** — Bearer token, custom headers, or basic credential.
