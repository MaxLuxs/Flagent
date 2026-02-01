package com.flagent.enhanced.storage

import com.flagent.enhanced.model.FlagSnapshot

class InMemorySnapshotStorage : SnapshotStorage {
    private var cachedSnapshot: FlagSnapshot? = null

    override suspend fun save(snapshot: FlagSnapshot) {
        cachedSnapshot = snapshot
    }

    override suspend fun load(): FlagSnapshot? {
        val snapshot = cachedSnapshot
        return if (snapshot?.isExpired() == true) null else snapshot
    }

    override suspend fun clear() {
        cachedSnapshot = null
    }

    override suspend fun hasValidSnapshot(): Boolean {
        val snapshot = cachedSnapshot
        return snapshot != null && !snapshot.isExpired()
    }
}
