package flagent.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for AppConfig defaults and value types.
 * Env vars are process-wide; tests assert that config is consistent.
 */
class AppConfigTest {

    @Test
    fun hostIsNonBlank() {
        assertTrue(AppConfig.host.isNotBlank())
    }

    @Test
    fun portIsInValidRange() {
        assertTrue(AppConfig.port in 1..65535)
    }

    @Test
    fun dbDriverIsNonBlank() {
        assertTrue(AppConfig.dbDriver.isNotBlank())
    }

    @Test
    fun dbConnectionStrIsNonBlank() {
        assertTrue(AppConfig.dbConnectionStr.isNotBlank())
    }

    @Test
    fun logrusLevelIsNonBlank() {
        assertTrue(AppConfig.logrusLevel.isNotBlank())
    }

    @Test
    fun evalCacheRefreshTimeoutIsPositive() {
        assertTrue(AppConfig.evalCacheRefreshTimeout > kotlin.time.Duration.ZERO)
    }

    @Test
    fun evalCacheRefreshIntervalIsPositive() {
        assertTrue(AppConfig.evalCacheRefreshInterval > kotlin.time.Duration.ZERO)
    }

    @Test
    fun corsAllowedHeadersContainsExpected() {
        assertTrue(AppConfig.corsAllowedHeaders.isNotEmpty())
        assertTrue(AppConfig.corsAllowedHeaders.any { it.equals("Authorization", ignoreCase = true) })
    }

    @Test
    fun corsAllowedMethodsContainsGet() {
        assertTrue(AppConfig.corsAllowedMethods.any { it.equals("GET", ignoreCase = true) })
    }

    @Test
    fun jwtAuthPrefixWhitelistPathsContainsHealth() {
        assertTrue(
            AppConfig.jwtAuthPrefixWhitelistPaths.any { it.contains("health") }
        )
    }

    @Test
    fun middlewareVerboseLoggerExcludeURLsIsList() {
        assertTrue(AppConfig.middlewareVerboseLoggerExcludeURLs is List<*>)
    }

    @Test
    fun dbConnectionRetryAttemptsIsPositive() {
        assertTrue(AppConfig.dbConnectionRetryAttempts > 0u)
    }

    @Test
    fun evalOnlyModeIsConsistent() {
        // evalOnlyMode is true when driver is json_file or json_http
        if (AppConfig.dbDriver in listOf("json_file", "json_http")) {
            assertTrue(AppConfig.evalOnlyMode)
        }
    }
}
