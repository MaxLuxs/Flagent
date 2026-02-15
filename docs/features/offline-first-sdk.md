# Offline-First SDK

> [English](offline-first-sdk.md) | [Русский](offline-first-sdk.ru.md)

## Overview

The Offline-First SDK lets applications use feature flags without an internet connection. Flags are cached locally and synced automatically when the connection is restored.

## Key features

### 1. Local caching

Flags are cached on the device for offline access.

**Features:** Persistent storage (SQLite, SharedPreferences, UserDefaults), automatic caching on load, cache size limits, TTL-based invalidation.

**Example:**

```kotlin
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = SQLiteOfflineStorage(context),
    cachePolicy = CachePolicy(
        ttl = 24.hours,
        maxCacheSize = 10.megabytes
    )
)
val result = manager.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US")
)
```

### 2. Automatic sync

When back online: background sync, incremental updates, conflict resolution, retry logic. Example: `SyncPolicy(autoSync = true, syncInterval = 5.minutes, ...)`.

### 3. Predictive caching

Preload by usage, by user, background prefetch. Example: `PreloadStrategy.INTELLIGENT`, `preloadFlags`, `prefetchEnabled`.

### 4. Conflict resolution

Strategies: Server wins (default), Client wins, Merge (custom), Last write wins.

## Architecture

Application → Flagent SDK → Cache Manager → Offline Storage → Network sync when online. Flow: start → load from cache → sync if online → update cache → serve from cache.

## SDK implementation

Kotlin (Android/JVM): `AndroidOfflineStorage`, `SQLiteOfflineStorage`. Swift: `iOSOfflineStorage`. JavaScript: `IndexedDBOfflineStorage`.

## API

`OfflineStorage`: save, load, clear, getFlag, updateFlag. `FlagentManager`: sync, syncInBackground, enableAutoSync, disableAutoSync.

## Configuration

`CachePolicy` (ttl, maxCacheSize, storageLocation, preloadStrategy, preloadFlags, prefetchEnabled). `SyncPolicy` (autoSync, syncInterval, syncOnAppStart, syncOnNetworkAvailable, conflictResolution, customMerge). Env: `FLAGENT_OFFLINE_STORAGE_*`, `FLAGENT_CACHE_*`, `FLAGENT_AUTO_SYNC_*`.

## Use cases

Mobile app offline; PWA with IndexedDB; desktop with local cache.

## Roadmap

Phase 1: Persistent storage, local cache, TTL. Phase 2: Auto-sync, background sync, incremental updates. Phase 3: Conflict resolution, predictive caching, compression, encryption.

## Related documentation

- [Real-Time Updates](real-time-updates.md)
- [Performance Optimizations](performance-optimizations.md)
- [SDK Documentation](https://github.com/MaxLuxs/Flagent/tree/main/sdk)
