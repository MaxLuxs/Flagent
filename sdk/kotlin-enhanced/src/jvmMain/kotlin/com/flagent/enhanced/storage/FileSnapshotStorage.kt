package com.flagent.enhanced.storage

import com.flagent.enhanced.model.FlagSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * File-based snapshot storage (JVM). Uses [createPersistentStorage] from common for KMP.
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
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    override suspend fun save(snapshot: FlagSnapshot) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(snapshot)
            snapshotFile.writeText(jsonString)
        } catch (e: Exception) {
            System.err.println("Failed to save snapshot: ${e.message}")
        }
    }

    override suspend fun load(): FlagSnapshot? = withContext(Dispatchers.IO) {
        try {
            if (!snapshotFile.exists()) return@withContext null
            val jsonString = snapshotFile.readText()
            val snapshot = json.decodeFromString<FlagSnapshot>(jsonString)
            if (snapshot.isExpired()) return@withContext null
            snapshot
        } catch (e: Exception) {
            System.err.println("Failed to load snapshot: ${e.message}")
            null
        }
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            if (snapshotFile.exists()) snapshotFile.delete()
        } catch (e: Exception) {
            System.err.println("Failed to clear snapshot: ${e.message}")
        }
    }

    override suspend fun hasValidSnapshot(): Boolean {
        val snapshot = load()
        return snapshot != null && !snapshot.isExpired()
    }
}
