package com.flagent.enhanced.fetcher

import com.flagent.enhanced.model.FlagSnapshot

/**
 * Fetches flag snapshots from server. Platform-specific implementations
 * (e.g. JVM with kotlin-client, iOS with Ktor) implement this interface.
 */
interface SnapshotFetcher {
    /**
     * Fetch complete flag snapshot.
     * @param ttlMs TTL for the snapshot in milliseconds
     */
    suspend fun fetchSnapshot(ttlMs: Long = 300000): FlagSnapshot

    /**
     * Fetch delta since last revision. Default implementation fetches full snapshot.
     */
    suspend fun fetchDelta(lastRevision: String?, ttlMs: Long = 300000): FlagSnapshot? =
        fetchSnapshot(ttlMs)
}
