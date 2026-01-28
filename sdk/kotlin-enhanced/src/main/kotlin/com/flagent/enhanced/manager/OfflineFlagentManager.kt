package com.flagent.enhanced.manager

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import com.flagent.enhanced.evaluator.LocalEvaluator
import com.flagent.enhanced.fetcher.SnapshotFetcher
import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.model.LocalEvaluationResult
import com.flagent.enhanced.realtime.RealtimeClient
import com.flagent.enhanced.realtime.RealtimeConfig
import com.flagent.enhanced.storage.FileSnapshotStorage
import com.flagent.enhanced.storage.InMemorySnapshotStorage
import com.flagent.enhanced.storage.SnapshotStorage
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * OfflineFlagentManager - offline-first feature flag manager.
 *
 * Provides:
 * - Client-side evaluation (no API calls for evaluation)
 * - Offline support (works without network)
 * - Auto-refresh (background snapshot updates)
 * - Fast evaluation (< 1ms typical latency)
 *
 * Architecture:
 * 1. Bootstrap: Load cached snapshot OR fetch from server
 * 2. Evaluate: Use local evaluator with cached snapshot
 * 3. Refresh: Periodically update snapshot in background
 *
 * @param exportApi Export API for fetching snapshots
 * @param flagApi Flag API for fallback
 * @param config Configuration for offline behavior
 *
 * @example
 * ```
 * val manager = OfflineFlagentManager(
 *     exportApi = exportApi,
 *     flagApi = flagApi,
 *     config = OfflineFlagentConfig(
 *         enablePersistence = true,
 *         autoRefresh = true,
 *         refreshIntervalMs = 60000
 *     )
 * )
 *
 * // Bootstrap (call once on app start)
 * manager.bootstrap()
 *
 * // Evaluate (fast, local, no network)
 * val result = manager.evaluate(
 *     flagKey = "new_feature",
 *     entityID = "user123",
 *     entityContext = mapOf("region" to "US")
 * )
 *
 * if (result.isEnabled()) {
 *     // Show new feature
 * }
 * ```
 */
class OfflineFlagentManager(
    private val exportApi: ExportApi,
    private val flagApi: FlagApi,
    private val config: OfflineFlagentConfig = OfflineFlagentConfig(),
    private val httpClient: HttpClient? = null
) {
    private val evaluator = LocalEvaluator()
    private val fetcher = SnapshotFetcher(exportApi, flagApi)
    private val storage: SnapshotStorage = if (config.enablePersistence) {
        FileSnapshotStorage(config.storageDir ?: File(System.getProperty("user.home"), ".flagent"))
    } else {
        InMemorySnapshotStorage()
    }
    
    private var currentSnapshot: FlagSnapshot? = null
    private val snapshotMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var refreshJob: Job? = null
    private var realtimeClient: RealtimeClient? = null
    
    private var isBootstrapped = false

    /**
     * Bootstrap the manager.
     *
     * Loads cached snapshot if available, otherwise fetches from server.
     * Call this once on app start before evaluating flags.
     *
     * @param forceRefresh Force fetch from server even if cached snapshot is valid
     * @throws Exception if bootstrap fails and no cached snapshot available
     */
    suspend fun bootstrap(forceRefresh: Boolean = false) = snapshotMutex.withLock {
        if (isBootstrapped && !forceRefresh) {
            return
        }

        // Try to load cached snapshot first
        if (!forceRefresh) {
            val cached = storage.load()
            if (cached != null && !cached.isExpired()) {
                currentSnapshot = cached
                isBootstrapped = true
                
                // Start background refresh if enabled
                startAutoRefresh()
                
                return
            }
        }

        // Fetch fresh snapshot
        try {
            val snapshot = fetcher.fetchSnapshot(config.snapshotTtlMs)
            currentSnapshot = snapshot
            
            // Save to storage for offline use
            storage.save(snapshot)
            
            isBootstrapped = true
            
            // Start background refresh if enabled
            startAutoRefresh()
        } catch (e: Exception) {
            // If fetch fails but we have expired cache, use it anyway
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

    /**
     * Evaluate a flag locally.
     *
     * Uses cached snapshot for evaluation - no API call is made.
     * Requires bootstrap() to be called first.
     *
     * @param flagKey Flag key to evaluate
     * @param flagID Flag ID (optional, used if key not found)
     * @param entityID Entity ID for consistent bucketing
     * @param entityType Entity type (optional)
     * @param entityContext Context for constraint matching
     * @param enableDebug Enable debug logging
     * @return Evaluation result
     * @throws IllegalStateException if manager is not bootstrapped
     */
    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any> = emptyMap(),
        enableDebug: Boolean = false
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

    /**
     * Batch evaluate multiple flags.
     *
     * @param requests List of evaluation requests
     * @return List of evaluation results
     */
    suspend fun evaluateBatch(
        requests: List<BatchEvaluationRequest>
    ): List<LocalEvaluationResult> {
        val snapshot = getSnapshot()
        return evaluator.evaluateBatch(requests, snapshot)
    }

    /**
     * Manually refresh snapshot from server.
     *
     * Updates cached snapshot with fresh data from server.
     * Evaluations will continue to work during refresh.
     *
     * @throws Exception if refresh fails
     */
    suspend fun refresh() = snapshotMutex.withLock {
        try {
            val snapshot = fetcher.fetchSnapshot(config.snapshotTtlMs)
            currentSnapshot = snapshot
            storage.save(snapshot)
        } catch (e: Exception) {
            // Log error but don't throw - evaluation can continue with old snapshot
            System.err.println("Failed to refresh snapshot: ${e.message}")
            throw e
        }
    }

    /**
     * Get current snapshot.
     *
     * @throws IllegalStateException if not bootstrapped
     */
    private suspend fun getSnapshot(): FlagSnapshot {
        val snapshot = currentSnapshot
            ?: throw IllegalStateException("Manager not bootstrapped. Call bootstrap() first.")
        
        // Check if snapshot is expired
        if (snapshot.isExpired() && config.autoRefresh) {
            // Try to refresh in background
            scope.launch {
                try {
                    refresh()
                } catch (e: Exception) {
                    // Continue with expired snapshot if refresh fails
                }
            }
        }
        
        return snapshot
    }

    /**
     * Start automatic background refresh.
     */
    private fun startAutoRefresh() {
        if (!config.autoRefresh || refreshJob?.isActive == true) {
            return
        }
        
        refreshJob = scope.launch {
            while (isActive) {
                delay(config.refreshIntervalMs)
                
                try {
                    refresh()
                } catch (e: Exception) {
                    // Log but continue - evaluation works with cached snapshot
                    System.err.println("Auto-refresh failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Stop automatic refresh.
     */
    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Clear all cached data.
     */
    suspend fun clearCache() {
        snapshotMutex.withLock {
            currentSnapshot = null
            storage.clear()
            isBootstrapped = false
        }
    }

    /**
     * Enable real-time updates via SSE.
     *
     * When enabled, snapshot is automatically refreshed when flags are updated.
     * Requires httpClient to be provided in constructor.
     *
     * @param baseUrl Base URL of Flagent server (e.g., "http://localhost:18000")
     * @param flagKeys Optional list of flag keys to filter updates
     * @param flagIDs Optional list of flag IDs to filter updates
     * @throws IllegalStateException if httpClient not provided
     */
    fun enableRealtimeUpdates(
        baseUrl: String,
        flagKeys: List<String>? = null,
        flagIDs: List<Long>? = null
    ) {
        val client = httpClient
            ?: throw IllegalStateException("httpClient must be provided to enable realtime updates")
        
        if (realtimeClient != null) {
            return // Already enabled
        }
        
        realtimeClient = RealtimeClient(
            httpClient = client,
            baseUrl = baseUrl,
            config = RealtimeConfig(autoReconnect = true)
        )
        
        // Subscribe to flag update events
        scope.launch {
            realtimeClient?.events?.collect { event ->
                // Trigger snapshot refresh on flag update
                try {
                    refresh()
                } catch (e: Exception) {
                    // Continue with old snapshot if refresh fails
                }
            }
        }
        
        // Connect to SSE endpoint
        realtimeClient?.connect(flagKeys, flagIDs)
    }
    
    /**
     * Disable real-time updates.
     */
    fun disableRealtimeUpdates() {
        realtimeClient?.shutdown()
        realtimeClient = null
    }
    
    /**
     * Check if real-time updates are enabled.
     */
    fun isRealtimeEnabled(): Boolean = realtimeClient != null

    /**
     * Shutdown the manager.
     *
     * Stops auto-refresh and cleans up resources.
     */
    fun shutdown() {
        stopAutoRefresh()
        disableRealtimeUpdates()
        scope.cancel()
    }

    /**
     * Check if manager is ready for evaluation.
     */
    fun isReady(): Boolean = isBootstrapped && currentSnapshot != null

    /**
     * Get snapshot age in milliseconds.
     */
    fun getSnapshotAge(): Long? {
        val snapshot = currentSnapshot ?: return null
        return System.currentTimeMillis() - snapshot.fetchedAt
    }

    /**
     * Check if snapshot is expired.
     */
    fun isSnapshotExpired(): Boolean {
        return currentSnapshot?.isExpired() ?: true
    }
}
