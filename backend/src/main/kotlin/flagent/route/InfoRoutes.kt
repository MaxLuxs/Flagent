package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.InfoResponse
import flagent.application.EnterprisePresence
import flagent.application.LicenseState
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Info endpoints - version and build information
 */

fun Routing.configureInfoRoutes() {
    route(ApiConstants.API_BASE_PATH) {
        get("/info") {
            val version = System.getProperty("flagent.version") ?: "0.1.6"
            val buildTime = System.getProperty("flagent.buildTime")
            val gitCommit = System.getProperty("flagent.gitCommit")
            call.respond(
                InfoResponse(
                    version = version,
                    buildTime = buildTime,
                    gitCommit = gitCommit,
                    enterpriseEnabled = EnterprisePresence.enterpriseEnabled,
                    licenseValid = LicenseState.licenseValid
                )
            )
        }
    }
}
