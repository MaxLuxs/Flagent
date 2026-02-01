package com.flagent.enhanced.storage

import com.flagent.enhanced.model.FlagSnapshot

interface SnapshotStorage {
    suspend fun save(snapshot: FlagSnapshot)
    suspend fun load(): FlagSnapshot?
    suspend fun clear()
    suspend fun hasValidSnapshot(): Boolean
}
