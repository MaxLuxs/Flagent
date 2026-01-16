package flagent.route

import flagent.service.FlagEntityTypeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * FlagEntityType routes - operations for flag entity types
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureFlagEntityTypeRoutes(flagEntityTypeService: FlagEntityTypeService) {
    route("/api/v1") {
            route("/flags/entity_types") {
                get {
                    try {
                        val entityTypes = flagEntityTypeService.findAllEntityTypes()
                        call.respond(entityTypes)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                }
            }
        }
}
