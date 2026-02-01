package com.flagent.enhanced.config

data class FlagentConfig(
    val cacheTtlMs: Long = 5 * 60 * 1000L,
    val enableCache: Boolean = true,
    val enableDebugLogging: Boolean = false
)
