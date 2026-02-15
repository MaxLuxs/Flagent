package com.flagent.enhanced.entry

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.infrastructure.ApiClient
import com.flagent.client.infrastructure.createDefaultHttpClientEngine
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.config.OfflineFlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import io.ktor.client.engine.HttpClientEngine

/**
 * Unified entry point for Flagent Kotlin SDK.
 * Use [builder] to create a [FlagentClient] (server or offline mode).
 */
object Flagent {

    fun builder(): Builder = Builder()

    class Builder {
        private var baseUrl: String = "http://localhost:18000/api/v1"
        private var httpClientEngine: HttpClientEngine? = null
        private var authConfig: (ApiClient) -> Unit = {}
        private var cacheEnable: Boolean = true
        private var cacheTtlMs: Long = 5 * 60 * 1000L
        private var mode: FlagentMode = FlagentMode.SERVER
        private var offlineConfig: OfflineFlagentConfig = OfflineFlagentConfig()

        fun baseUrl(url: String) = apply { baseUrl = url }
        fun httpClientEngine(engine: HttpClientEngine?) = apply { httpClientEngine = engine }
        fun auth(block: (ApiClient) -> Unit) = apply { authConfig = block }
        fun cache(enable: Boolean, ttlMs: Long = 5 * 60 * 1000L) = apply {
            cacheEnable = enable
            cacheTtlMs = ttlMs
        }
        fun offlineSupport(useOffline: Boolean) = apply {
            mode = if (useOffline) FlagentMode.OFFLINE else FlagentMode.SERVER
        }
        fun mode(m: FlagentMode) = apply { mode = m }
        fun offlineConfig(config: OfflineFlagentConfig) = apply { offlineConfig = config }

        fun build(): FlagentClient {
            return when (mode) {
                FlagentMode.SERVER -> {
                    val engine = httpClientEngine ?: createDefaultHttpClientEngine()
                    val api = EvaluationApi(baseUrl = baseUrl, httpClientEngine = engine)
                    authConfig(api)
                    val config = FlagentConfig(cacheTtlMs = cacheTtlMs, enableCache = cacheEnable)
                    FlagentManagerAdapter(FlagentManager(api, config))
                }
                FlagentMode.OFFLINE -> createOfflineFlagentClient(
                    baseUrl = baseUrl,
                    config = offlineConfig,
                    httpClientEngine = httpClientEngine,
                    authConfig = authConfig
                )
            }
        }
    }
}

/**
 * Platform-specific creation of offline client (ExportApi + FlagApi + OfflineFlagentManager).
 */
expect fun createOfflineFlagentClient(
    baseUrl: String,
    config: OfflineFlagentConfig,
    httpClientEngine: HttpClientEngine?,
    authConfig: (ApiClient) -> Unit
): FlagentClient
