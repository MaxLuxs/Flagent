package flagent.route

import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.domain.entity.Variant
import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Tag
import flagent.api.model.*
import flagent.service.FlagService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Flag routes - CRUD operations for flags
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureFlagRoutes(flagService: FlagService) {
    route("/api/v1") {
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
                    call.respond(flags.map { mapFlagToResponse(it) })
                }
                
                post {
                    val request = call.receive<CreateFlagRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val flag = flagService.createFlag(
                            Flag(
                                key = request.key ?: "",
                                description = request.description,
                                enabled = false
                            ),
                            template = request.template,
                            updatedBy = updatedBy
                        )
                        
                        call.respond(HttpStatusCode.OK, mapFlagToResponse(flag))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                    } catch (e: Exception) {
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
                        
                        val updatedFlag = flagService.updateFlag(
                            existingFlag.copy(
                                description = request.description ?: existingFlag.description,
                                key = request.key ?: existingFlag.key,
                                dataRecordsEnabled = request.dataRecordsEnabled ?: existingFlag.dataRecordsEnabled,
                                entityType = request.entityType ?: existingFlag.entityType,
                                notes = request.notes ?: existingFlag.notes
                            ),
                            updatedBy = updatedBy
                        )
                        
                        call.respond(mapFlagToResponse(updatedFlag))
                    }
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        flagService.deleteFlag(flagId)
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

private fun mapFlagToResponse(flag: Flag): FlagResponse {
    return FlagResponse(
        id = flag.id,
        key = flag.key,
        description = flag.description,
        createdBy = flag.createdBy,
        updatedBy = flag.updatedBy,
        enabled = flag.enabled,
        snapshotID = flag.snapshotId,
        notes = flag.notes,
        dataRecordsEnabled = flag.dataRecordsEnabled,
        entityType = flag.entityType,
        segments = flag.segments.map { mapSegmentToResponse(it) },
        variants = flag.variants.map { mapVariantToResponse(it) },
        tags = flag.tags.map { mapTagToResponse(it) },
        updatedAt = flag.updatedAt
    )
}

private fun mapSegmentToResponse(segment: Segment): SegmentResponse {
    return SegmentResponse(
        id = segment.id,
        flagID = segment.flagId,
        description = segment.description,
        rank = segment.rank,
        rolloutPercent = segment.rolloutPercent,
        constraints = segment.constraints.map { mapConstraintToResponse(it) },
        distributions = segment.distributions.map { mapDistributionToResponse(it) }
    )
}

private fun mapVariantToResponse(variant: Variant): VariantResponse {
    return VariantResponse(
        id = variant.id,
        flagID = variant.flagId,
        key = variant.key,
        attachment = variant.attachment?.entries?.associate { it.key to it.value.toString() }
    )
}

private fun mapConstraintToResponse(constraint: Constraint): ConstraintResponse {
    return ConstraintResponse(
        id = constraint.id,
        segmentID = constraint.segmentId,
        property = constraint.property,
        operator = constraint.operator,
        value = constraint.value
    )
}

private fun mapDistributionToResponse(distribution: Distribution): DistributionResponse {
    return DistributionResponse(
        id = distribution.id,
        segmentID = distribution.segmentId,
        variantID = distribution.variantId,
        variantKey = distribution.variantKey,
        percent = distribution.percent
    )
}

private fun mapTagToResponse(tag: Tag): TagResponse {
    return TagResponse(
        id = tag.id,
        value = tag.value
    )
}
