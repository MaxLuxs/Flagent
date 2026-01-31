# Offline-First SDK

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
// Kotlin SDK
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = SQLiteOfflineStorage(context),
    cachePolicy = CachePolicy(
        ttl = 24.hours,
        maxCacheSize = 10.megabytes
    )
)

// Evaluation работает даже offline
val result = manager.evaluate(
    flagKey = "new_payment_flow",
    entityID = "user123",
    entityContext = mapOf("country" to "US")
)
```

### 2. Автоматическая синхронизация

Автоматическая синхронизация при восстановлении связи.

**Возможности:**
- Background sync
- Incremental updates
- Conflict resolution
- Retry logic

**Пример:**

```kotlin
manager.syncPolicy = SyncPolicy(
    autoSync = true,
    syncInterval = 5.minutes,
    onSyncSuccess = { flags ->
        println("Synced ${flags.size} flags")
    },
    onSyncError = { error ->
        println("Sync failed: ${error.message}")
    }
)
```

### 3. Предиктивный кэшинг

Предзагрузка флагов на основе использования.

**Стратегии:**
- Preload часто используемых флагов
- Preload флагов для текущего пользователя
- Background prefetch
- Intelligent caching

**Пример:**

```kotlin
manager.cachePolicy = CachePolicy(
    preloadStrategy = PreloadStrategy.INTELLIGENT,
    preloadFlags = listOf("new_payment_flow", "checkout_experiment"),
    prefetchEnabled = true
)
```

### 4. Conflict Resolution

Разрешение конфликтов при синхронизации.

**Стратегии:**
- Server wins (по умолчанию)
- Client wins
- Merge (custom logic)
- Last write wins

**Пример:**

```kotlin
manager.syncPolicy = SyncPolicy(
    conflictResolution = ConflictResolution.SERVER_WINS,
    customMerge = { server, local ->
        // Custom merge logic
        server.copy(
            enabled = server.enabled ?: local.enabled
        )
    }
)
```

## Архитектура

### Storage Layer

```
Application
    ↓
Flagent SDK
    ↓
Cache Manager
    ↓
Offline Storage (SQLite/SharedPreferences/UserDefaults)
    ↓
Network Sync (when online)
```

### Sync Flow

```
1. App starts
   ↓
2. Load from local cache
   ↓
3. Check network connection
   ↓
4. If online: Sync with server
   ↓
5. Update local cache
   ↓
6. Serve from cache (online/offline)
```

## SDK Implementation

### Kotlin (Android/JVM)

```kotlin
// Android
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = AndroidOfflineStorage(context),
    cachePolicy = CachePolicy(
        ttl = 24.hours,
        storageLocation = StorageLocation.DATABASE
    )
)

// JVM
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = SQLiteOfflineStorage(dbPath),
    cachePolicy = CachePolicy(
        ttl = 24.hours
    )
)
```

### Swift (iOS/macOS)

```swift
// iOS
let manager = FlagentManager(
    baseURL: "https://flags.example.com/api/v1",
    offlineStorage: iOSOfflineStorage(),
    cachePolicy: CachePolicy(
        ttl: 24.hours,
        storageLocation: .userDefaults
    )
)
```

### JavaScript (Web)

```javascript
// Web with IndexedDB
const manager = new FlagentManager({
  baseURL: 'https://flags.example.com/api/v1',
  offlineStorage: new IndexedDBOfflineStorage('flagent-cache'),
  cachePolicy: {
    ttl: 24 * 60 * 60 * 1000, // 24 hours
    maxCacheSize: 10 * 1024 * 1024 // 10 MB
  }
});
```

## API

### Offline Storage Interface

```kotlin
interface OfflineStorage {
    suspend fun save(flags: List<Flag>)
    suspend fun load(): List<Flag>?
    suspend fun clear()
    suspend fun getFlag(key: String): Flag?
    suspend fun updateFlag(flag: Flag)
}
```

### Sync API

```kotlin
interface FlagentManager {
    suspend fun sync(): SyncResult
    suspend fun syncInBackground()
    fun enableAutoSync(interval: Duration)
    fun disableAutoSync()
}
```

## Конфигурация

### Cache Policy

```kotlin
data class CachePolicy(
    val ttl: Duration = 24.hours,
    val maxCacheSize: Long = 10.megabytes,
    val storageLocation: StorageLocation = StorageLocation.DATABASE,
    val preloadStrategy: PreloadStrategy = PreloadStrategy.ALL,
    val preloadFlags: List<String> = emptyList(),
    val prefetchEnabled: Boolean = true
)

enum class StorageLocation {
    DATABASE,      // SQLite, CoreData
    PREFERENCES,   // SharedPreferences, UserDefaults
    MEMORY         // In-memory only
}

enum class PreloadStrategy {
    ALL,           // Preload all flags
    INTELLIGENT,   // Preload based on usage
    NONE           // No preloading
}
```

### Sync Policy

```kotlin
data class SyncPolicy(
    val autoSync: Boolean = true,
    val syncInterval: Duration = 5.minutes,
    val syncOnAppStart: Boolean = true,
    val syncOnNetworkAvailable: Boolean = true,
    val conflictResolution: ConflictResolution = ConflictResolution.SERVER_WINS,
    val customMerge: ((Flag, Flag) -> Flag)? = null
)

enum class ConflictResolution {
    SERVER_WINS,
    CLIENT_WINS,
    LAST_WRITE_WINS,
    MERGE
}
```

### Environment Variables

```bash
# Offline Storage
FLAGENT_OFFLINE_STORAGE_TYPE=sqlite
FLAGENT_OFFLINE_STORAGE_PATH=/var/lib/flagent/cache.db

# Cache Policy
FLAGENT_CACHE_TTL=24h
FLAGENT_CACHE_MAX_SIZE=10MB
FLAGENT_CACHE_PRELOAD_STRATEGY=intelligent

# Sync Policy
FLAGENT_AUTO_SYNC=true
FLAGENT_SYNC_INTERVAL=5m
FLAGENT_SYNC_ON_START=true
```

## Use Cases

### 1. Mobile App (Offline Support)

Мобильное приложение работает offline с кэшированными флагами.

```kotlin
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = AndroidOfflineStorage(context),
    cachePolicy = CachePolicy(
        ttl = 7.days,
        preloadStrategy = PreloadStrategy.ALL
    ),
    syncPolicy = SyncPolicy(
        autoSync = true,
        syncOnNetworkAvailable = true
    )
)

// Всегда работает, даже offline
val result = manager.evaluate(
    flagKey = "new_feature",
    entityID = userId
)
```

### 2. Progressive Web App

PWA с кэшированием через IndexedDB.

```javascript
const manager = new FlagentManager({
  baseURL: 'https://flags.example.com/api/v1',
  offlineStorage: new IndexedDBOfflineStorage('flagent-cache'),
  cachePolicy: {
    ttl: 24 * 60 * 60 * 1000,
    prefetchEnabled: true
  }
});

// Works offline
const result = await manager.evaluate('new_feature', {
  entityID: userId
});
```

### 3. Desktop Application

Десктопное приложение с локальным кэшем.

```kotlin
val manager = FlagentManager(
    baseUrl = "https://flags.example.com/api/v1",
    offlineStorage = SQLiteOfflineStorage(dbPath),
    cachePolicy = CachePolicy(
        ttl = 24.hours,
        maxCacheSize = 50.megabytes
    )
)
```

## Roadmap

### Фаза 1: Базовое кэширование (В планах)
- ⏳ Persistent storage
- ⏳ Local cache loading
- ⏳ TTL-based expiration

### Фаза 2: Синхронизация (В планах)
- ⏳ Auto-sync
- ⏳ Background sync
- ⏳ Incremental updates

### Фаза 3: Продвинутые возможности (В планах)
- ⏳ Conflict resolution
- ⏳ Predictive caching
- ⏳ Compression
- ⏳ Encryption

## Связанная документация

- [Real-Time Updates](./real-time-updates.md)
- [Performance Optimizations](./performance-optimizations.md)
- [SDK Documentation](https://github.com/MaxLuxs/Flagent/tree/main/sdk)
