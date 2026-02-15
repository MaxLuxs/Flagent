package com.flagent.enhanced.manager

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.fetcher.DefaultSnapshotFetcher
import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.model.LocalEvaluationResult
import com.flagent.enhanced.model.LocalFlag
import com.flagent.enhanced.platform.createPersistentStorage
import com.flagent.enhanced.platform.defaultStoragePath
import com.flagent.enhanced.realtime.RealtimeClient
import com.flagent.enhanced.realtime.RealtimeConfig
import com.flagent.enhanced.storage.InMemorySnapshotStorage
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.map

/**
 * JVM entry point: offline-first manager with same constructor as before (exportApi, flagApi, config, httpClient).
 * Delegates to [OfflineFlagentManagerCore]; supports [enableRealtimeUpdates] via [HttpClient].
 */
class OfflineFlagentManager(
    private val exportApi: ExportApi,
    private val flagApi: FlagApi,
    private val config: OfflineFlagentConfig = OfflineFlagentConfig(),
    private val httpClient: HttpClient? = null
) : IOfflineFlagentManager {
    private val storage = if (config.enablePersistence) {
        val path = config.storagePath ?: defaultStoragePath()
        if (path.isBlank()) InMemorySnapshotStorage() else createPersistentStorage(path)
    } else {
        InMemorySnapshotStorage()
    }

    private val core = OfflineFlagentManagerCore(
        fetcher = DefaultSnapshotFetcher(exportApi, flagApi),
        storage = storage,
        config = config,
        realtimeRefreshTrigger = null
    )

    private var realtimeClient: RealtimeClient? = null

    suspend fun bootstrap(forceRefresh: Boolean = false) = core.bootstrap(forceRefresh)

    override suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String,
        entityType: String?,
        entityContext: Map<String, Any>,
        enableDebug: Boolean
    ): LocalEvaluationResult = core.evaluate(
        flagKey = flagKey,
        flagID = flagID,
        entityID = entityID,
        entityType = entityType,
        entityContext = entityContext,
        enableDebug = enableDebug
    )

    suspend fun evaluateBatch(requests: List<BatchEvaluationRequest>) = core.evaluateBatch(requests)

    suspend fun refresh() = core.refresh()

    fun stopAutoRefresh() = core.stopAutoRefresh()

    override suspend fun clearCache() = core.clearCache()

    fun enableRealtimeUpdates(
        baseUrl: String,
        flagKeys: List<String>? = null,
        flagIDs: List<Long>? = null
    ) {
        val client = httpClient
            ?: throw IllegalStateException("httpClient must be provided to enable realtime updates")
        if (realtimeClient != null) return
        realtimeClient = RealtimeClient(
            httpClient = client,
            baseUrl = baseUrl,
            config = RealtimeConfig(autoReconnect = true)
        )
        core.setRealtimeRefreshTrigger(realtimeClient!!.events.map { })
        realtimeClient!!.connect(flagKeys, flagIDs)
    }

    fun disableRealtimeUpdates() {
        realtimeClient?.shutdown()
        realtimeClient = null
        core.disableRealtimeUpdates()
    }

    fun isRealtimeEnabled(): Boolean = core.isRealtimeEnabled()

    fun shutdown() {
        realtimeClient?.shutdown()
        realtimeClient = null
        core.shutdown()
    }

    fun isReady(): Boolean = core.isReady()

    fun getSnapshotAge(): Long? = core.getSnapshotAge()

    fun isSnapshotExpired(): Boolean = core.isSnapshotExpired()

    /**
     * Returns current snapshot for debug/inspection only (e.g. Debug UI).
     */
    fun getSnapshotForDebug(): FlagSnapshot? = core.getSnapshotForDebug()

    /**
     * Returns list of all flags from current snapshot for debug/inspection only.
     */
    override fun getFlagsList(): List<LocalFlag> = core.getFlagsList()
}
