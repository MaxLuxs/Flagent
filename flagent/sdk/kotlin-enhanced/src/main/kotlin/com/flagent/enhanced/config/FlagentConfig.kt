package com.flagent.enhanced.config

/**
 * Configuration for Flagent Enhanced SDK.
 * 
 * Controls caching behavior and debug settings for the enhanced SDK.
 * 
 * @property cacheTtlMs Cache time-to-live in milliseconds. Default: 5 minutes (300000 ms)
 * @property enableCache Enable caching of evaluation results. Default: true
 * @property enableDebugLogging Enable debug logging. Default: false
 */
data class FlagentConfig(
    /**
     * Cache TTL for evaluation results in milliseconds.
     * Default: 5 minutes (300000 ms)
     */
    val cacheTtlMs: Long = 5 * 60 * 1000L,
    
    /**
     * Enable caching of evaluation results.
     * When disabled, all evaluations go directly to API without caching.
     * Default: true
     */
    val enableCache: Boolean = true,
    
    /**
     * Enable debug logging for development and troubleshooting.
     * Default: false
     */
    val enableDebugLogging: Boolean = false
)