package com.flagent.enhanced.config

import java.io.File

/**
 * Configuration for Offline Flagent Manager.
 *
 * Controls offline-first behavior including persistence, auto-refresh, and caching.
 *
 * @property enablePersistence Enable persistent storage of snapshots (default: true)
 * @property storageDir Directory for storing snapshots (default: ~/.flagent)
 * @property autoRefresh Enable automatic background refresh (default: true)
 * @property refreshIntervalMs Interval between automatic refreshes in ms (default: 60 seconds)
 * @property snapshotTtlMs TTL for snapshot before considered stale in ms (default: 5 minutes)
 * @property enableDebugLogging Enable debug logging (default: false)
 *
 * @example
 * ```
 * val config = OfflineFlagentConfig(
 *     enablePersistence = true,
 *     autoRefresh = true,
 *     refreshIntervalMs = 60000 // 1 minute
 * )
 * ```
 */
data class OfflineFlagentConfig(
    /**
     * Enable persistent storage of flag snapshots.
     * When enabled, snapshots are saved to disk for offline use.
     * Default: true
     */
    val enablePersistence: Boolean = true,

    /**
     * Directory for storing flag snapshots.
     * Default: ~/.flagent (user home directory)
     */
    val storageDir: File? = null,

    /**
     * Enable automatic background refresh of snapshots.
     * When enabled, snapshots are refreshed periodically in the background.
     * Default: true
     */
    val autoRefresh: Boolean = true,

    /**
     * Interval between automatic refreshes in milliseconds.
     * Default: 60 seconds (60000 ms)
     * Recommended: 30-300 seconds depending on update frequency needs
     */
    val refreshIntervalMs: Long = 60 * 1000L,

    /**
     * Time-to-live for snapshots before considered stale in milliseconds.
     * Stale snapshots trigger background refresh but continue working.
     * Default: 5 minutes (300000 ms)
     * Recommended: 5-30 minutes
     */
    val snapshotTtlMs: Long = 5 * 60 * 1000L,

    /**
     * Enable debug logging for troubleshooting.
     * Default: false
     */
    val enableDebugLogging: Boolean = false,

    /**
     * Retry failed fetches with exponential backoff.
     * Default: true
     */
    val enableRetry: Boolean = true,

    /**
     * Maximum number of retry attempts for failed fetches.
     * Default: 3
     */
    val maxRetryAttempts: Int = 3,

    /**
     * Initial retry delay in milliseconds (doubles on each retry).
     * Default: 1 second (1000 ms)
     */
    val initialRetryDelayMs: Long = 1000L
)
