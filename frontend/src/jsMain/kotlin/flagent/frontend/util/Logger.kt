package flagent.frontend.util

import flagent.frontend.config.AppConfig

/**
 * Logging levels
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * Logger interface for application logging
 */
interface Logger {
    fun debug(tag: String, message: String, error: Throwable? = null)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, error: Throwable? = null)
    fun error(tag: String, message: String, error: Throwable? = null)
}

/**
 * Console logger implementation
 */
class ConsoleLogger(private val minLevel: LogLevel = LogLevel.INFO) : Logger {
    override fun debug(tag: String, message: String, error: Throwable?) {
        if (minLevel == LogLevel.DEBUG) {
            console.log("[$tag] $message", error ?: "")
        }
    }
    
    override fun info(tag: String, message: String) {
        if (minLevel <= LogLevel.INFO) {
            console.info("[$tag] $message")
        }
    }
    
    override fun warn(tag: String, message: String, error: Throwable?) {
        if (minLevel <= LogLevel.WARN) {
            console.warn("[$tag] $message", error ?: "")
        }
    }
    
    override fun error(tag: String, message: String, error: Throwable?) {
        console.error("[$tag] $message", error ?: "")
    }
}

/**
 * No-op logger for production
 */
class NoopLogger : Logger {
    override fun debug(tag: String, message: String, error: Throwable?) {}
    override fun info(tag: String, message: String) {}
    override fun warn(tag: String, message: String, error: Throwable?) {}
    override fun error(tag: String, message: String, error: Throwable?) {}
}

/**
 * Global logger instance
 */
object AppLogger {
    private val logger: Logger by lazy {
        if (AppConfig.debugMode) {
            ConsoleLogger(LogLevel.DEBUG)
        } else {
            ConsoleLogger(LogLevel.INFO)
        }
    }
    
    fun debug(tag: String, message: String, error: Throwable? = null) {
        logger.debug(tag, message, error)
    }
    
    fun info(tag: String, message: String) {
        logger.info(tag, message)
    }
    
    fun warn(tag: String, message: String, error: Throwable? = null) {
        logger.warn(tag, message, error)
    }
    
    fun error(tag: String, message: String, error: Throwable? = null) {
        logger.error(tag, message, error)
    }
}
