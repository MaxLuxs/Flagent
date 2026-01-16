package flagent.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Health check routes
 * Maps to pkg/handler/handler.go setupHealth
 */
fun Routing.configureHealthRoutes() {
    route("/api/v1") {
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
    }
}
