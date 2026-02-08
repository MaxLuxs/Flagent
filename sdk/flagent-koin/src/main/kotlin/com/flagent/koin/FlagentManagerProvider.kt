package com.flagent.koin

import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

private data class CachedClients(
    val evaluationApi: EvaluationApi,
    val manager: FlagentManager
)

/**
 * Provider that creates FlagentManager and EvaluationApi per baseUrl with caching.
 * Use when baseUrl can change at runtime (e.g. user-configurable in settings).
 */
class FlagentManagerProvider(
    private val httpClientEngine: HttpClientEngine? = null,
    private val defaultConfig: FlagentConfig = FlagentConfig()
) {
    private val managerCache = mutableMapOf<Pair<String, FlagentConfig>, FlagentManager>()
    private val apiCache = mutableMapOf<String, EvaluationApi>()

    fun getManager(baseUrl: String, config: FlagentConfig = defaultConfig): FlagentManager {
        val normalizedUrl = baseUrl.trimEnd('/').let {
            if (it.endsWith("/api/v1")) it else "$it/api/v1"
        }
        return synchronized(managerCache) {
            managerCache[normalizedUrl to config] ?: run {
                val api = getEvaluationApiInternal(normalizedUrl)
                val manager = FlagentManager(api, config)
                managerCache[normalizedUrl to config] = manager
                manager
            }
        }
    }

    fun getEvaluationApi(baseUrl: String): EvaluationApi {
        val normalizedUrl = baseUrl.trimEnd('/').let {
            if (it.endsWith("/api/v1")) it else "$it/api/v1"
        }
        return getEvaluationApiInternal(normalizedUrl)
    }

    private fun getEvaluationApiInternal(normalizedUrl: String): EvaluationApi {
        return synchronized(apiCache) {
            apiCache[normalizedUrl] ?: run {
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                }
                val httpClient = when (val engine = httpClientEngine) {
                    null -> HttpClient(CIO) { install(ContentNegotiation) { json(json) } }
                    else -> HttpClient(engine) { install(ContentNegotiation) { json(json) } }
                }
                val api = EvaluationApi(
                    baseUrl = normalizedUrl,
                    httpClientEngine = httpClient.engine
                )
                apiCache[normalizedUrl] = api
                api
            }
        }
    }
}

/**
 * Koin module that provides FlagentManagerProvider for dynamic baseUrl.
 * Use in Android when baseUrl comes from user settings.
 *
 * @param httpClientEngine Pass Android.create() for Android, null for CIO
 */
fun flagentManagerProviderModule(
    httpClientEngine: HttpClientEngine? = null,
    defaultConfig: FlagentConfig = FlagentConfig()
): Module = module {
    single {
        FlagentManagerProvider(httpClientEngine, defaultConfig)
    }
}
