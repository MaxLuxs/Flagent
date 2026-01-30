package flagent.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseConfigTest {

    @Test
    fun hikariConfigContainsRequiredKeys() {
        val config = DatabaseConfig.hikariConfig
        assertTrue(config.containsKey("maximumPoolSize"))
        assertTrue(config.containsKey("minimumIdle"))
        assertTrue(config.containsKey("connectionTimeout"))
        assertTrue(config.containsKey("idleTimeout"))
        assertTrue(config.containsKey("maxLifetime"))
        assertTrue(config.containsKey("connectionTestQuery"))
    }

    @Test
    fun getPoolSizeReturnsOneForSqlite() {
        // When dbDriver is sqlite3 (default), pool size must be 1
        if (AppConfig.dbDriver == "sqlite3") {
            assertEquals(1, DatabaseConfig.getPoolSize())
        }
    }

    @Test
    fun getPoolSizeReturnsValueFromHikariConfigForNonSqlite() {
        // When dbDriver is not sqlite3, pool size comes from hikariConfig
        val config = DatabaseConfig.hikariConfig
        val expectedMax = config["maximumPoolSize"] as Int
        if (AppConfig.dbDriver != "sqlite3") {
            assertEquals(expectedMax, DatabaseConfig.getPoolSize())
        }
    }

    @Test
    fun getRecommendedPoolSizeReturnsPositiveValue() {
        val size = DatabaseConfig.getRecommendedPoolSize(
            coreCount = 4,
            expectedConcurrentRequests = 100
        )
        assertTrue(size > 0)
        assertTrue(size <= 100)
    }

    @Test
    fun getRecommendedPoolSizeCapsAt100() {
        val size = DatabaseConfig.getRecommendedPoolSize(
            coreCount = 64,
            expectedConcurrentRequests = 10_000
        )
        assertEquals(100, size)
    }

    @Test
    fun queryTimeoutsArePositive() {
        assertTrue(DatabaseConfig.QueryTimeouts.SHORT_QUERY_MS > 0)
        assertTrue(DatabaseConfig.QueryTimeouts.MEDIUM_QUERY_MS > 0)
        assertTrue(DatabaseConfig.QueryTimeouts.LONG_QUERY_MS > 0)
    }

    @Test
    fun retryConfigHasExpectedValues() {
        assertTrue(DatabaseConfig.RetryConfig.MAX_RETRIES > 0)
        assertTrue(DatabaseConfig.RetryConfig.INITIAL_DELAY_MS > 0)
        assertTrue(DatabaseConfig.RetryConfig.MAX_DELAY_MS >= DatabaseConfig.RetryConfig.INITIAL_DELAY_MS)
        assertTrue(DatabaseConfig.RetryConfig.BACKOFF_MULTIPLIER >= 1.0)
    }
}
