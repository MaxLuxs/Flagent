package com.flagent.enhanced.entry

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.client.infrastructure.ApiClient
import com.flagent.client.infrastructure.createDefaultHttpClientEngine
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.model.LocalEvaluationResult
import io.ktor.client.engine.HttpClientEngine

actual fun createOfflineFlagentClient(
    baseUrl: String,
    config: OfflineFlagentConfig,
    httpClientEngine: HttpClientEngine?,
    authConfig: (ApiClient) -> Unit
): FlagentClient {
    val engine = httpClientEngine ?: createDefaultHttpClientEngine()
    val exportApi = ExportApi(baseUrl = baseUrl, httpClientEngine = engine)
    authConfig(exportApi)
    val flagApi = FlagApi(baseUrl = baseUrl, httpClientEngine = engine)
    authConfig(flagApi)
    val manager = OfflineFlagentManager(exportApi, flagApi, config, null)
    val delegate = object : OfflineFlagentManagerDelegate {
        override suspend fun bootstrap(forceRefresh: Boolean) = manager.bootstrap(forceRefresh)
        override suspend fun evaluate(
            flagKey: String?,
            flagID: Long?,
            entityID: String,
            entityType: String?,
            entityContext: Map<String, Any>,
            enableDebug: Boolean
        ): LocalEvaluationResult = manager.evaluate(
            flagKey = flagKey,
            flagID = flagID,
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            enableDebug = enableDebug
        )
        override suspend fun evaluateBatch(requests: List<BatchEvaluationRequest>): List<LocalEvaluationResult> =
            manager.evaluateBatch(requests)
    }
    return OfflineFlagentManagerAdapter(delegate)
}
