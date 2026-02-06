package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.TagService
import flagent.service.command.CreateTagCommand
import flagent.util.getSubject
import flagent.route.mapper.ResponseMappers.mapTagToResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Tag CRUD API endpoints
 */
fun Routing.configureTagRoutes(tagService: TagService) {
    route(ApiConstants.API_BASE_PATH) {
            // GET /api/v1/tags - find all tags
            route("/tags") {
                get {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val valueLike = call.request.queryParameters["value_like"]
                    
                    val tags = tagService.findAllTags(limit, offset, valueLike)
                    call.respond(tags.map { mapTagToResponse(it) })
                }
            }
            
            // GET /api/v1/flags/{flagId}/tags - find tags for flag
            // POST /api/v1/flags/{flagId}/tags - create tag and associate with flag
            // DELETE /api/v1/flags/{flagId}/tags/{tagId} - remove tag from flag
            route("/flags/{flagId}/tags") {
                
                
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    try {
                        val tags = tagService.findTagsByFlagId(flagId)
                        call.respond(tags.map { mapTagToResponse(it) })
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound, e.message ?: "Flag not found")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                }
                
                post {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val request = call.receive<CreateTagRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val command = CreateTagCommand(flagId = flagId, value = request.value)
                        val tag = tagService.createTag(command, updatedBy)
                        call.respond(HttpStatusCode.OK, mapTagToResponse(tag))
                    } catch (e: IllegalArgumentException) {
                        val statusCode = when {
                            e.message?.contains("error finding") == true -> HttpStatusCode.NotFound
                            else -> HttpStatusCode.BadRequest
                        }
                        call.respond(statusCode, e.message ?: "Invalid tag")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                }
                
                route("/{tagId}") {
                    
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val tagId = call.parameters["tagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid tag ID")
                        
                        try {
                            val updatedBy = call.getSubject()
                            tagService.deleteTag(flagId, tagId, updatedBy)
                            call.respond(HttpStatusCode.OK)
                        } catch (e: IllegalArgumentException) {
                            val statusCode = when {
                                e.message?.contains("error finding") == true -> HttpStatusCode.NotFound
                                else -> HttpStatusCode.BadRequest
                            }
                            call.respond(statusCode, e.message ?: "Invalid tag")
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                }
            }
        }
}
