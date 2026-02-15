package flagent.route

import flagent.api.constants.ApiConstants
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

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
