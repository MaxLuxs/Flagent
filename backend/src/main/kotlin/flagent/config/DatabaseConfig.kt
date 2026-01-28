package flagent.config

/**
 * Database connection pool configuration
 * 
 * Based on load test results and production requirements:
 * - Max pool size: 50 connections (supports 200 concurrent users)
 * - Min idle: 10 connections (for baseline performance)
 * - Connection timeout: 30s
 * - Idle timeout: 10 minutes
 * - Max lifetime: 30 minutes
 */
object DatabaseConfig {
    
    // HikariCP Configuration
    val hikariConfig = mapOf(
        // Pool sizing
        "maximumPoolSize" to (System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 50),
        "minimumIdle" to (System.getenv("DB_MIN_IDLE")?.toIntOrNull() ?: 10),
        
        // Timeouts
        "connectionTimeout" to 30000L, // 30 seconds
        "idleTimeout" to 600000L, // 10 minutes
        "maxLifetime" to 1800000L, // 30 minutes
        "validationTimeout" to 5000L, // 5 seconds
        
        // Performance
        "cachePrepStmts" to true,
        "prepStmtCacheSize" to 250,
        "prepStmtCacheSqlLimit" to 2048,
        "useServerPrepStmts" to true,
        
        // Monitoring
        "leakDetectionThreshold" to 60000L, // 1 minute
        "registerMbeans" to true,
        
        // Health checks
        "connectionTestQuery" to "SELECT 1",
        "keepaliveTime" to 300000L // 5 minutes
    )
    
    /**
     * Get pool size based on environment
     */
    fun getPoolSize(): Int {
        return when (AppConfig.dbDriver) {
            "sqlite3" -> 1 // SQLite doesn't support concurrent writes
            else -> hikariConfig["maximumPoolSize"] as Int
        }
    }
    
    /**
     * Get recommended pool size based on expected load
     * 
     * Formula: connections = ((core_count * 2) + effective_spindle_count)
     * For web apps: connections = (concurrent_requests / 2) to (concurrent_requests)
     */
    fun getRecommendedPoolSize(
        coreCount: Int = Runtime.getRuntime().availableProcessors(),
        expectedConcurrentRequests: Int = 200
    ): Int {
        val cpuBased = (coreCount * 2) + 1
        val loadBased = expectedConcurrentRequests / 2
        
        // Return the higher of the two, capped at 100
        return maxOf(cpuBased, loadBased).coerceAtMost(100)
    }
    
    /**
     * Database query timeouts
     */
    object QueryTimeouts {
        const val SHORT_QUERY_MS = 1000L // 1 second - metrics insert, simple selects
        const val MEDIUM_QUERY_MS = 5000L // 5 seconds - aggregations
        const val LONG_QUERY_MS = 30000L // 30 seconds - complex analytics
    }
    
    /**
     * Database retry configuration
     */
    object RetryConfig {
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY_MS = 100L
        const val MAX_DELAY_MS = 5000L
        const val BACKOFF_MULTIPLIER = 2.0
    }
}
