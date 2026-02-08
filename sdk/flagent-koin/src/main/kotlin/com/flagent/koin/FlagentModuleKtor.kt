package com.flagent.koin

import com.flagent.enhanced.config.FlagentConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Configuration for Ktor server-side Flagent integration.
 * Ktor plugin (ktor-flagent) provides evaluation; this module is for Koin users
 * who want to inject Flagent client in Ktor routes.
 *
 * Note: Requires ktor-flagent plugin to be installed. Use getFlagentClient() from
 * ktor-flagent for direct access, or inject via Koin if ktor-flagent adds Koin support.
 */
data class FlagentKtorConfig(
    var flagentBaseUrl: String = "http://localhost:18000",
    var enableEvaluation: Boolean = true,
    var enableCache: Boolean = true,
    var cacheTtlMs: Long = 60000
)

/**
 * Koin module for Ktor server. Uses flagentClientModule with CIO engine.
 * For server-side evaluation, prefer ktor-flagent plugin; use this when you need
 * FlagentManager/EvaluationApi injected in Ktor Application.
 */
fun flagentKtorModule(block: FlagentKtorConfig.() -> Unit = {}): Module {
    val config = FlagentKtorConfig().apply(block)
    return flagentClientModule(
        baseUrl = config.flagentBaseUrl,
        config = FlagentConfig(
            enableCache = config.enableCache,
            cacheTtlMs = config.cacheTtlMs
        ),
        useEnhanced = true,
        httpClientEngine = null
    )
}
