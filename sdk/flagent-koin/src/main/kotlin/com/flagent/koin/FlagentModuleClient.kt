package com.flagent.koin

import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for Flagent client (Android, KMP, JVM).
 *
 * @param baseUrl Flagent API base URL (e.g. http://localhost:18000)
 * @param config FlagentConfig for enhanced manager (cache, etc.)
 * @param useEnhanced If true, registers FlagentManager; else only EvaluationApi
 * @param httpClientEngine Optional engine. Pass Android.create() for Android, null for CIO (JVM).
 */
fun flagentClientModule(
    baseUrl: String,
    config: FlagentConfig = FlagentConfig(),
    useEnhanced: Boolean = true,
    httpClientEngine: HttpClientEngine? = null
): Module = module {
    single {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
        when (val engine = httpClientEngine) {
            null -> HttpClient(CIO) {
                install(ContentNegotiation) { json(json) }
            }
            else -> HttpClient(engine) {
                install(ContentNegotiation) { json(json) }
            }
        }
    }
    single {
        val url = baseUrl.trimEnd('/').let {
            if (it.endsWith("/api/v1")) it else "$it/api/v1"
        }
        EvaluationApi(
            baseUrl = url,
            httpClientEngine = get<HttpClient>().engine
        )
    }
    if (useEnhanced) {
        single { FlagentManager(get(), config) }
    }
}
