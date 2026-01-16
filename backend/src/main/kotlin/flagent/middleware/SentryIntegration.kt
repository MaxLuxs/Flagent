package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.sentry.Sentry
import io.sentry.SentryOptions
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Sentry integration middleware
 * Maps to pkg/config/config.go setupSentry
 */
fun Application.configureSentry() {
    if (!AppConfig.sentryEnabled || AppConfig.sentryDSN.isEmpty()) {
        return
    }
    
    try {
        Sentry.init { options: SentryOptions ->
            options.dsn = AppConfig.sentryDSN
            if (AppConfig.sentryEnvironment.isNotEmpty()) {
                options.environment = AppConfig.sentryEnvironment
            }
            options.tracesSampleRate = 1.0
            options.isEnableUncaughtExceptionHandler = true
        }
        logger.info { "Sentry initialized with DSN: ${AppConfig.sentryDSN.take(20)}..." }
    } catch (e: Exception) {
        logger.error(e) { "Failed to initialize Sentry" }
    }
}
