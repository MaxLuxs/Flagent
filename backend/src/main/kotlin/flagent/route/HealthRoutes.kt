package flagent.route

import flagent.api.constants.ApiConstants

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Health check endpoints
 */
fun Routing.configureHealthRoutes() {
    route(ApiConstants.API_BASE_PATH) {
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
    }
}
