package com.flagent.enhanced.manager

import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import com.flagent.enhanced.evaluator.LocalEvaluator
import com.flagent.enhanced.fetcher.SnapshotFetcher
import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.model.LocalEvaluationResult
import com.flagent.enhanced.model.LocalFlag
import com.flagent.enhanced.platform.currentTimeMs
import com.flagent.enhanced.platform.logWarn
import com.flagent.enhanced.storage.SnapshotStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Common API for offline manager (core or platform wrapper).
 * Used by kotlin-debug-ui and other commonMain consumers.
 */
interface IOfflineFlagentManager {
    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any> = emptyMap(),
        enableDebug: Boolean = false
    ): LocalEvaluationResult
    suspend fun clearCache()
    fun getFlagsList(): List<LocalFlag>
}

/**
 * Offline-first feature flag manager (KMP common).
 * JVM exposes [com.flagent.enhanced.manager.OfflineFlagentManager] with same API; iOS uses this type via factory.
 */
class OfflineFlagentManagerCore(
    private val fetcher: SnapshotFetcher,
    private val storage: SnapshotStorage,
    private val config: OfflineFlagentConfig = OfflineFlagentConfig(),
    private val realtimeRefreshTrigger: Flow<Unit>? = null
) : IOfflineFlagentManager {
    private val evaluator = LocalEvaluator()
    private var currentSnapshot: FlagSnapshot? = null
    private val snapshotMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var refreshJob: kotlinx.coroutines.Job? = null
    private var realtimeJob: kotlinx.coroutines.Job? = null
    private var isBootstrapped = false

    init {
        realtimeRefreshTrigger?.let { flow ->
            realtimeJob = scope.launch {
                flow.collect {
                    try {
                        refresh()
                    } catch (_: Exception) { /* continue with old snapshot */ }
                }
            }
        }
    }

    suspend fun bootstrap(forceRefresh: Boolean = false) = snapshotMutex.withLock {
        if (isBootstrapped && !forceRefresh) return

        if (!forceRefresh) {
            val cached = storage.load()
            if (cached != null && !cached.isExpired()) {
                currentSnapshot = cached
                isBootstrapped = true
                startAutoRefresh()
                return
            }
        }

        try {
            val snapshot = fetcher.fetchSnapshot(config.snapshotTtlMs)
            currentSnapshot = snapshot
            storage.save(snapshot)
            isBootstrapped = true
            startAutoRefresh()
        } catch (e: Exception) {
            val cached = storage.load()
            if (cached != null) {
                currentSnapshot = cached
                isBootstrapped = true
                startAutoRefresh()
                return
            }
            throw Exception("Failed to bootstrap: ${e.message}", e)
        }
    }

    override suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String,
        entityType: String?,
        entityContext: Map<String, Any>,
        enableDebug: Boolean
    ): LocalEvaluationResult {
        val snapshot = getSnapshot()
        return evaluator.evaluate(
            flagKey = flagKey,
            flagID = flagID,
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            snapshot = snapshot,
            enableDebug = enableDebug
        )
    }

    suspend fun evaluateBatch(requests: List<BatchEvaluationRequest>): List<LocalEvaluationResult> {
        val snapshot = getSnapshot()
        return evaluator.evaluateBatch(requests, snapshot)
    }

    suspend fun refresh() = snapshotMutex.withLock {
        try {
            val snapshot = fetcher.fetchSnapshot(config.snapshotTtlMs)
            currentSnapshot = snapshot
            storage.save(snapshot)
        } catch (e: Exception) {
            logWarn("Failed to refresh snapshot: ${e.message}")
            throw e
        }
    }

    private suspend fun getSnapshot(): FlagSnapshot {
        val snapshot = currentSnapshot
            ?: throw IllegalStateException("Manager not bootstrapped. Call bootstrap() first.")
        if (snapshot.isExpired() && config.autoRefresh) {
            scope.launch {
                try {
                    refresh()
                } catch (_: Exception) { }
            }
        }
        return snapshot
    }

    private fun startAutoRefresh() {
        if (!config.autoRefresh || refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            while (isActive) {
                delay(config.refreshIntervalMs)
                try {
                    refresh()
                } catch (e: Exception) {
                    logWarn("Auto-refresh failed: ${e.message}")
                }
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Set flow that triggers refresh on each emission (e.g. realtime events).
     * Used by JVM to plug in RealtimeClient after construction.
     */
    fun setRealtimeRefreshTrigger(flow: Flow<Unit>) {
        realtimeJob?.cancel()
        realtimeJob = scope.launch {
            flow.collect {
                try {
                    refresh()
                } catch (_: Exception) { }
            }
        }
    }

    fun disableRealtimeUpdates() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    fun isRealtimeEnabled(): Boolean = realtimeJob?.isActive == true

    override suspend fun clearCache() {
        snapshotMutex.withLock {
            currentSnapshot = null
            storage.clear()
            isBootstrapped = false
        }
    }

    fun shutdown() {
        stopAutoRefresh()
        realtimeJob?.cancel()
        realtimeJob = null
        scope.cancel()
    }

    fun isReady(): Boolean = isBootstrapped && currentSnapshot != null

    fun getSnapshotAge(): Long? {
        val snapshot = currentSnapshot ?: return null
        return currentTimeMs() - snapshot.fetchedAt
    }

    fun isSnapshotExpired(): Boolean = currentSnapshot?.isExpired() ?: true

    /**
     * Returns current snapshot for debug/inspection only (e.g. Debug UI).
     * Read-only; may be null if not bootstrapped.
     */
    fun getSnapshotForDebug(): FlagSnapshot? = currentSnapshot

    /**
     * Returns list of all flags from current snapshot for debug/inspection only.
     * Empty if not bootstrapped.
     */
    override fun getFlagsList(): List<LocalFlag> = currentSnapshot?.flags?.values?.toList() ?: emptyList()
}
