package flagent.route

import flagent.api.constants.ApiConstants
import flagent.service.FlagEntityTypeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Flag entity type CRUD API endpoints
 */
fun Routing.configureFlagEntityTypeRoutes(flagEntityTypeService: FlagEntityTypeService) {
    route(ApiConstants.API_BASE_PATH) {
        route("/flags/entity_types") {
            get {
                try {
                    val entityTypes = flagEntityTypeService.findAllEntityTypes()
                    call.respond(entityTypes)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        e.message ?: "Internal server error"
                    )
                }
            }
        }
    }
}
