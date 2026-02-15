# Offline-First SDK

> [English](offline-first-sdk.md) | Русский

## Обзор

Offline-First SDK позволяет приложениям работать с feature flags даже без интернет-соединения. Флаги кэшируются локально и синхронизируются автоматически при восстановлении связи.

## Ключевые возможности

### 1. Локальное кэширование

Автоматическое кэширование флагов на устройстве для offline доступа.

**Особенности:**
- Persistent storage (SQLite, SharedPreferences, UserDefaults)
- Автоматическое кэширование при загрузке
- Оптимизация размера кэша
- TTL-based инвалидация

**Пример:**

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

### 2. Автоматическая синхронизация

Автоматическая синхронизация при восстановлении связи: background sync, incremental updates, conflict resolution, retry logic.

**Пример:** `SyncPolicy(autoSync = true, syncInterval = 5.minutes, onSyncSuccess = ..., onSyncError = ...)`.

### 3. Предиктивный кэширование

Предзагрузка флагов: preload по использованию, по пользователю, background prefetch. Пример: `PreloadStrategy.INTELLIGENT`, `preloadFlags`, `prefetchEnabled`.

### 4. Conflict Resolution

Стратегии: Server wins (по умолчанию), Client wins, Merge (custom), Last write wins. Пример: `ConflictResolution.SERVER_WINS`, `customMerge`.

## Архитектура

Application → Flagent SDK → Cache Manager → Offline Storage (SQLite/SharedPreferences/UserDefaults) → Network Sync (when online). Sync flow: app start → load from cache → if online sync → update cache → serve from cache.

## SDK Implementation

Kotlin (Android/JVM): `AndroidOfflineStorage`, `SQLiteOfflineStorage`. Swift: `iOSOfflineStorage`, UserDefaults. JavaScript: `IndexedDBOfflineStorage`.

## API

`OfflineStorage`: save, load, clear, getFlag, updateFlag. `FlagentManager`: sync, syncInBackground, enableAutoSync, disableAutoSync.

## Конфигурация

`CachePolicy` (ttl, maxCacheSize, storageLocation, preloadStrategy, preloadFlags, prefetchEnabled). `SyncPolicy` (autoSync, syncInterval, syncOnAppStart, syncOnNetworkAvailable, conflictResolution, customMerge). Env: `FLAGENT_OFFLINE_STORAGE_*`, `FLAGENT_CACHE_*`, `FLAGENT_AUTO_SYNC_*`.

## Use Cases

Mobile app offline; PWA с IndexedDB; desktop с локальным кэшем.

## Roadmap

Фаза 1: Persistent storage, local cache, TTL. Фаза 2: Auto-sync, background sync, incremental updates. Фаза 3: Conflict resolution, predictive caching, compression, encryption.

## Связанная документация

- [Real-Time Updates](real-time-updates.md)
- [Performance Optimizations](performance-optimizations.md)
- [SDK Documentation](https://github.com/MaxLuxs/Flagent/tree/main/sdk)
