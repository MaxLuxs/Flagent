package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.InfoResponse
import flagent.application.EnterprisePresence
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Info route - returns version and build information
 * Maps to pkg/handler/handler.go setupInfo
 */

fun Routing.configureInfoRoutes() {
    route(ApiConstants.API_BASE_PATH) {
        get("/info") {
            val version = System.getProperty("flagent.version") ?: "0.1.0"
            val buildTime = System.getProperty("flagent.buildTime")
            val gitCommit = System.getProperty("flagent.gitCommit")
            call.respond(
                InfoResponse(
                    version = version,
                    buildTime = buildTime,
                    gitCommit = gitCommit,
                    enterpriseEnabled = EnterprisePresence.enterpriseEnabled
                )
            )
        }
    }
}
