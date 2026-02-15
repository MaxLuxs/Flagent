package com.flagent.enhanced.platform

import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.storage.InMemorySnapshotStorage
import com.flagent.enhanced.storage.SnapshotStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual fun createPersistentStorage(path: String): SnapshotStorage =
    if (path.isBlank()) InMemorySnapshotStorage()
    else FileSnapshotStorage(File(path))

actual fun defaultStoragePath(): String = ""

private class FileSnapshotStorage(private val storageDir: File) : SnapshotStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }
    private val snapshotFile: File get() = File(storageDir, "flagent_snapshot.json")

    init {
        if (!storageDir.exists()) storageDir.mkdirs()
    }

    override suspend fun save(snapshot: FlagSnapshot) {
        try {
            snapshotFile.writeText(json.encodeToString(FlagSnapshot.serializer(), snapshot))
        } catch (_: Exception) { }
    }
    override suspend fun load(): FlagSnapshot? {
        return try {
            if (!snapshotFile.exists()) null
            else json.decodeFromString(FlagSnapshot.serializer(), snapshotFile.readText()).takeIf { !it.isExpired() }
        } catch (_: Exception) { null }
    }
    override suspend fun clear() { if (snapshotFile.exists()) snapshotFile.delete() }
    override suspend fun hasValidSnapshot(): Boolean = load() != null
}
