package flagent.middleware

import flagent.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.sentry.Sentry
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Error handling and recovery middleware
 */
fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            logger.warn(cause) { "Bad request: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Bad request"))
            )
        }
        
        exception<IllegalStateException> { call, cause ->
            logger.error(cause) { "Internal server error: ${cause.message}" }
            if (AppConfig.sentryEnabled) {
                Sentry.captureException(cause)
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
        
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception: ${cause.message}" }
            if (AppConfig.sentryEnabled) {
                Sentry.captureException(cause)
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}
