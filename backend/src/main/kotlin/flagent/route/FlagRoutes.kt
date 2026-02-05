package flagent.route

import flagent.api.AuditLoggerRegistry
import flagent.api.constants.ApiConstants
import mu.KotlinLogging
import flagent.api.model.*
import flagent.service.FlagService
import flagent.service.command.CreateFlagCommand
import flagent.service.command.PutFlagCommand
import flagent.util.getSubject
import flagent.route.mapper.ResponseMappers.mapFlagToResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val log = KotlinLogging.logger {}

/**
 * Flag routes - CRUD operations for flags
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureFlagRoutes(flagService: FlagService) {
    route(ApiConstants.API_BASE_PATH) {
            route("/flags") {
                get {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val enabled = call.request.queryParameters["enabled"]?.toBoolean()
                    val description = call.request.queryParameters["description"]
                    val key = call.request.queryParameters["key"]
                    val descriptionLike = call.request.queryParameters["descriptionLike"]
                    val preload = call.request.queryParameters["preload"]?.toBoolean() ?: false
                    val deleted = call.request.queryParameters["deleted"]?.toBoolean() ?: false
                    val tags = call.request.queryParameters["tags"]
                    
                    val total = flagService.countFlags(
                        enabled = enabled,
                        description = description,
                        key = key,
                        descriptionLike = descriptionLike,
                        deleted = deleted,
                        tags = tags
                    )
                    val flags = flagService.findFlags(
                        limit = limit,
                        offset = offset,
                        enabled = enabled,
                        description = description,
                        key = key,
                        descriptionLike = descriptionLike,
                        preload = preload,
                        deleted = deleted,
                        tags = tags
                    )
                    call.response.headers.append("X-Total-Count", total.toString())
                    call.respond(flags.map { mapFlagToResponse(it) })
                }
                
                route("/batch/enabled") {
                    put {
                        val request = call.receive<BatchFlagEnabledRequest>()
                        val updatedBy = call.getSubject()
                        if (request.ids.isEmpty()) {
                            return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ids cannot be empty"))
                        }
                        val updated = flagService.batchSetEnabled(request.ids, request.enabled, updatedBy)
                        request.ids.forEach { id ->
                            AuditLoggerRegistry.logger?.log(call, if (request.enabled) "flag.enabled" else "flag.disabled", "flag:$id", mapOf("id" to id))
                        }
                        call.respond(updated.map { mapFlagToResponse(it) })
                    }
                }

                post {
                    val request = call.receive<CreateFlagRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val command = CreateFlagCommand(
                            key = request.key,
                            description = request.description,
                            template = request.template,
                            environmentId = request.environmentId
                        )
                        val flag = flagService.createFlag(command, updatedBy)
                        AuditLoggerRegistry.logger?.log(call, "flag.created", "flag:${flag.key}", mapOf("id" to flag.id, "key" to flag.key))
                        call.respond(HttpStatusCode.OK, mapFlagToResponse(flag))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                    } catch (e: Exception) {
                        log.warn(e) { "Flag creation failed: ${e.message}" }
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
                    }
                }
                
                route("/{flagId}") {
                    get {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val flag = flagService.getFlag(flagId)
                            ?: return@get call.respond(HttpStatusCode.NotFound, "Flag not found")
                        
                        call.respond(mapFlagToResponse(flag))
                    }
                    
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val request = call.receive<PutFlagRequest>()
                        val existingFlag = flagService.getFlag(flagId)
                            ?: return@put call.respond(HttpStatusCode.NotFound, "Flag not found")
                        
                        val updatedBy = call.getSubject()
                        
                        val command = PutFlagCommand(
                            description = request.description,
                            key = request.key,
                            dataRecordsEnabled = request.dataRecordsEnabled,
                            entityType = request.entityType,
                            notes = request.notes,
                            environmentId = request.environmentId
                        )
                        val updatedFlag = flagService.updateFlag(flagId, command, updatedBy)
                        AuditLoggerRegistry.logger?.log(call, "flag.updated", "flag:${updatedFlag.key}", mapOf("id" to flagId))
                        call.respond(mapFlagToResponse(updatedFlag))
                    }
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val deleted = flagService.getFlag(flagId)
                        flagService.deleteFlag(flagId)
                        AuditLoggerRegistry.logger?.log(call, "flag.deleted", "flag:${deleted?.key ?: flagId}", mapOf("id" to flagId))
                        call.respond(HttpStatusCode.OK)
                    }
                }
                
                route("/{flagId}/enabled") {
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val request = call.receive<SetFlagEnabledRequest>()
                        val updatedBy = call.getSubject()
                        
                        val flag = flagService.setFlagEnabled(flagId, request.enabled, updatedBy)
                            ?: return@put call.respond(HttpStatusCode.NotFound, "Flag not found")
                        AuditLoggerRegistry.logger?.log(call, if (request.enabled) "flag.enabled" else "flag.disabled", "flag:${flag.key}", mapOf("id" to flagId))
                        call.respond(mapFlagToResponse(flag))
                    }
                }
                
                route("/{flagId}/restore") {
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val flag = flagService.restoreFlag(flagId)
                            ?: return@put call.respond(HttpStatusCode.NotFound, "Flag not found")
                        
                        call.respond(mapFlagToResponse(flag))
                    }
                }
            }
        }
}

// Mappers are now centralized in flagent.route.mapper.ResponseMappers
