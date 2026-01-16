package flagent.route

import flagent.api.model.InfoResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Info route - returns version and build information
 * Maps to pkg/handler/handler.go setupInfo
 */

fun Routing.configureInfoRoutes() {
    route("/api/v1") {
        get("/info") {
            val version = System.getProperty("flagent.version") ?: "1.0.0"
            val buildTime = System.getProperty("flagent.buildTime")
            val gitCommit = System.getProperty("flagent.gitCommit")
            
            call.respond(
                InfoResponse(
                    version = version,
                    buildTime = buildTime,
                    gitCommit = gitCommit
                )
            )
        }
    }
}
