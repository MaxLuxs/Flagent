package flagent.middleware

import flagent.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Recovery middleware - handles unhandled exceptions
 */
fun Application.configureRecovery() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception: ${cause.message}" }
            
            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (cause.message ?: "Bad request"))
                    )
                }
                is IllegalStateException -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (cause.message ?: "Internal server error"))
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error")
                    )
                }
            }
        }
    }
}
