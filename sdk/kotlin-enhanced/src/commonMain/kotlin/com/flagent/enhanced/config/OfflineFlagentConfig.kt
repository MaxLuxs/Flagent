package com.flagent.enhanced.config

/**
 * Configuration for Offline Flagent Manager (KMP).
 * storagePath: directory path for snapshot storage (null = in-memory only).
 */
data class OfflineFlagentConfig(
    val enablePersistence: Boolean = true,
    val storagePath: String? = null,
    val autoRefresh: Boolean = true,
    val refreshIntervalMs: Long = 60 * 1000L,
    val snapshotTtlMs: Long = 5 * 60 * 1000L,
    val enableDebugLogging: Boolean = false,
    val enableRetry: Boolean = true,
    val maxRetryAttempts: Int = 3,
    val initialRetryDelayMs: Long = 1000L
)
