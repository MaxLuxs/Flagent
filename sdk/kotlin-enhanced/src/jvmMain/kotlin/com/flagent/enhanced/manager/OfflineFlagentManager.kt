package com.flagent.enhanced.manager

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.fetcher.DefaultSnapshotFetcher
import com.flagent.enhanced.model.LocalEvaluationResult
import com.flagent.enhanced.platform.createPersistentStorage
import com.flagent.enhanced.realtime.RealtimeClient
import com.flagent.enhanced.realtime.RealtimeConfig
import com.flagent.enhanced.storage.InMemorySnapshotStorage
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * JVM entry point: offline-first manager with same constructor as before (exportApi, flagApi, config, httpClient).
 * Delegates to [OfflineFlagentManagerCore]; supports [enableRealtimeUpdates] via [HttpClient].
 */
class OfflineFlagentManager(
    private val exportApi: ExportApi,
    private val flagApi: FlagApi,
    private val config: OfflineFlagentConfig = OfflineFlagentConfig(),
    private val httpClient: HttpClient? = null
) {
    private val storage = if (config.enablePersistence) {
        createPersistentStorage(
            config.storagePath ?: File(System.getProperty("user.home"), ".flagent").absolutePath
        )
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

    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any> = emptyMap(),
        enableDebug: Boolean = false
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

    suspend fun clearCache() = core.clearCache()

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
}
