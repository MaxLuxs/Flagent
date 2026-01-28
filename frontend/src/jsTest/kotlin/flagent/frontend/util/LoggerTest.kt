package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertNotNull

class LoggerTest {
    
    @Test
    fun testConsoleLoggerCreation() {
        val logger = ConsoleLogger(LogLevel.DEBUG)
        assertNotNull(logger)
    }
    
    @Test
    fun testNoopLoggerCreation() {
        val logger = NoopLogger()
        assertNotNull(logger)
    }
    
    @Test
    fun testAppLoggerDebug() {
        // Should not throw
        AppLogger.debug("Test", "Debug message")
    }
    
    @Test
    fun testAppLoggerInfo() {
        // Should not throw
        AppLogger.info("Test", "Info message")
    }
    
    @Test
    fun testAppLoggerWarn() {
        // Should not throw
        AppLogger.warn("Test", "Warning message")
    }
    
    @Test
    fun testAppLoggerError() {
        // Should not throw
        AppLogger.error("Test", "Error message")
    }
}
