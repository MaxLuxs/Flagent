package com.flagent.koin

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Provider that creates FlagentManager, EvaluationApi, and FlagApi per baseUrl with caching.
 * Use when baseUrl can change at runtime (e.g. user-configurable in settings).
 */
class FlagentManagerProvider(
    private val httpClientEngine: HttpClientEngine? = null,
    private val defaultConfig: FlagentConfig = FlagentConfig()
) {
    private val managerCache = mutableMapOf<Pair<String, FlagentConfig>, FlagentManager>()
    private val apiCache = mutableMapOf<String, EvaluationApi>()
    private val flagApiCache = mutableMapOf<String, FlagApi>()
    private val mutex = Mutex()

    fun getManager(baseUrl: String, config: FlagentConfig = defaultConfig): FlagentManager =
        kotlinx.coroutines.runBlocking { mutex.withLock {
            val url = normalizeBaseUrl(baseUrl)
            managerCache.getOrPut(url to config) { FlagentManager(getEvaluationApiInternal(url), config) }
        } }

    fun getEvaluationApi(baseUrl: String): EvaluationApi =
        kotlinx.coroutines.runBlocking { mutex.withLock {
            getEvaluationApiInternal(normalizeBaseUrl(baseUrl))
        } }

    /**
     * Returns cached FlagApi for the given baseUrl (e.g. for debug UI flags list).
     */
    fun getFlagApi(baseUrl: String): FlagApi =
        kotlinx.coroutines.runBlocking { mutex.withLock {
            val url = normalizeBaseUrl(baseUrl)
            flagApiCache.getOrPut(url) { FlagApi(baseUrl = url, httpClientEngine = httpClientEngine ?: defaultHttpClientEngine()) }
        } }

    private fun normalizeBaseUrl(baseUrl: String): String =
        baseUrl.trimEnd('/').let {
            if (it.endsWith("/api/v1")) it else "$it/api/v1"
        }

    private fun getEvaluationApiInternal(normalizedUrl: String): EvaluationApi {
        return apiCache.getOrPut(normalizedUrl) {
            val engine = httpClientEngine ?: defaultHttpClientEngine()
            EvaluationApi(baseUrl = normalizedUrl, httpClientEngine = engine)
        }
    }
}

/**
 * Koin module that provides FlagentManagerProvider for dynamic baseUrl.
 *
 * @param httpClientEngine Platform default when null (CIO on JVM, Darwin on iOS, etc.)
 */
fun flagentManagerProviderModule(
    httpClientEngine: HttpClientEngine? = null,
    defaultConfig: FlagentConfig = FlagentConfig()
): Module = module {
    single {
        FlagentManagerProvider(httpClientEngine, defaultConfig)
    }
}
