package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.VariantService
import flagent.service.command.CreateVariantCommand
import flagent.service.command.PutVariantCommand
import flagent.util.getSubject
import flagent.route.mapper.ResponseMappers.mapVariantToResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Variant CRUD API endpoints
 */
fun Routing.configureVariantRoutes(variantService: VariantService) {
    route(ApiConstants.API_BASE_PATH) {
            route("/flags/{flagId}/variants") {
                
                
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val variants = variantService.findVariantsByFlagId(flagId)
                    call.respond(variants.map { mapVariantToResponse(it) })
                }
                
                post {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val request = call.receive<CreateVariantRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val command = CreateVariantCommand(
                            flagId = flagId,
                            key = request.key,
                            attachment = request.attachment
                        )
                        val variant = variantService.createVariant(command, updatedBy)
                        
                        call.respond(HttpStatusCode.OK, mapVariantToResponse(variant))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid variant")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                }
                
                route("/{variantId}") {
                    
                    
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val variantId = call.parameters["variantId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid variant ID")
                        
                        val request = call.receive<PutVariantRequest>()
                        val updatedBy = call.getSubject()
                        
                        try {
                            val command = PutVariantCommand(
                                flagId = flagId,
                                variantId = variantId,
                                key = request.key,
                                attachment = request.attachment
                            )
                            val variant = variantService.updateVariant(command, updatedBy)
                            
                            call.respond(HttpStatusCode.OK, mapVariantToResponse(variant))
                        } catch (e: IllegalArgumentException) {
                            val statusCode = when {
                                e.message?.contains("error finding") == true -> HttpStatusCode.NotFound
                                else -> HttpStatusCode.BadRequest
                            }
                            call.respond(statusCode, e.message ?: "Invalid variant")
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val variantId = call.parameters["variantId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid variant ID")
                        
                        try {
                            val updatedBy = call.getSubject()
                            variantService.deleteVariant(flagId, variantId, updatedBy)
                            call.respond(HttpStatusCode.OK)
                        } catch (e: IllegalArgumentException) {
                            val statusCode = when {
                                e.message?.contains("error finding") == true -> HttpStatusCode.NotFound
                                else -> HttpStatusCode.BadRequest
                            }
                            call.respond(statusCode, e.message ?: "Invalid variant")
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                }
            }
        }
}
