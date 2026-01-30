package com.flagent.enhanced.storage

import com.flagent.enhanced.model.FlagSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

/**
 * SnapshotStorage - persists flag snapshots to disk for offline-first support.
 *
 * Provides:
 * - Persistent storage of flag configurations
 * - Offline evaluation support
 * - Fast app startup (no initial fetch needed)
 *
 * @param storageDir Directory for storing snapshots
 *
 * @example
 * ```
 * val storage = SnapshotStorage(File("/path/to/cache"))
 * storage.save(snapshot)
 * val cached = storage.load()
 * ```
 */
interface SnapshotStorage {
    /**
     * Save snapshot to persistent storage.
     */
    suspend fun save(snapshot: FlagSnapshot)
    
    /**
     * Load snapshot from persistent storage.
     *
     * @return Cached snapshot or null if not found/invalid
     */
    suspend fun load(): FlagSnapshot?
    
    /**
     * Clear all cached snapshots.
     */
    suspend fun clear()
    
    /**
     * Check if cached snapshot exists and is valid.
     */
    suspend fun hasValidSnapshot(): Boolean
}

/**
 * File-based snapshot storage implementation.
 *
 * Stores snapshots as JSON files on disk.
 */
class FileSnapshotStorage(
    private val storageDir: File
) : SnapshotStorage {
    
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }
    
    private val snapshotFile: File
        get() = File(storageDir, "flagent_snapshot.json")
    
    init {
        // Ensure storage directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }
    
    override suspend fun save(snapshot: FlagSnapshot) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(snapshot)
            snapshotFile.writeText(jsonString)
        } catch (e: Exception) {
            // Log error but don't throw - storage failure shouldn't break evaluation
            System.err.println("Failed to save snapshot: ${e.message}")
        }
    }
    
    override suspend fun load(): FlagSnapshot? = withContext(Dispatchers.IO) {
        try {
            if (!snapshotFile.exists()) {
                return@withContext null
            }
            
            val jsonString = snapshotFile.readText()
            val snapshot = json.decodeFromString<FlagSnapshot>(jsonString)
            
            // Check if snapshot is expired
            if (snapshot.isExpired()) {
                return@withContext null
            }
            
            snapshot
        } catch (e: Exception) {
            // If loading fails, return null - will trigger fresh fetch
            System.err.println("Failed to load snapshot: ${e.message}")
            null
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            if (snapshotFile.exists()) {
                snapshotFile.delete()
            }
        } catch (e: Exception) {
            System.err.println("Failed to clear snapshot: ${e.message}")
        }
    }
    
    override suspend fun hasValidSnapshot(): Boolean {
        val snapshot = load()
        return snapshot != null && !snapshot.isExpired()
    }
}

/**
 * In-memory snapshot storage (for testing or non-persistent scenarios).
 */
class InMemorySnapshotStorage : SnapshotStorage {
    private var cachedSnapshot: FlagSnapshot? = null
    
    override suspend fun save(snapshot: FlagSnapshot) {
        cachedSnapshot = snapshot
    }
    
    override suspend fun load(): FlagSnapshot? {
        val snapshot = cachedSnapshot
        return if (snapshot?.isExpired() == true) {
            null
        } else {
            snapshot
        }
    }
    
    override suspend fun clear() {
        cachedSnapshot = null
    }
    
    override suspend fun hasValidSnapshot(): Boolean {
        val snapshot = cachedSnapshot
        return snapshot != null && !snapshot.isExpired()
    }
}
