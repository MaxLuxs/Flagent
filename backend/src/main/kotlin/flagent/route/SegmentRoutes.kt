package flagent.route

import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Segment
import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.SegmentService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Segment routes - CRUD operations for segments
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureSegmentRoutes(segmentService: SegmentService) {
    route(ApiConstants.API_BASE_PATH) {
            route("/flags/{flagId}/segments") {
                
                
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val segments = segmentService.findSegmentsByFlagId(flagId)
                    call.respond(segments.map { mapSegmentToResponse(it) })
                }
                
                post {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val request = call.receive<CreateSegmentRequest>()
                    val updatedBy = call.getSubject()
                    
                    val segment = segmentService.createSegment(
                        Segment(
                            flagId = flagId,
                            description = request.description,
                            rolloutPercent = request.rolloutPercent
                        ),
                        updatedBy = updatedBy
                    )
                    
                    call.respond(HttpStatusCode.OK, mapSegmentToResponse(segment))
                }
                
                route("/reorder") {
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        
                        val request = call.receive<PutSegmentReorderRequest>()
                        val updatedBy = call.getSubject()
                        segmentService.reorderSegments(flagId, request.segmentIDs, updatedBy)
                        call.respond(HttpStatusCode.OK)
                    }
                }
                
                route("/{segmentId}") {
                    get {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                        val segment = segmentService.getSegment(segmentId)
                            ?: return@get call.respond(HttpStatusCode.NotFound, "Segment not found")
                        if (segment.flagId != flagId) {
                            return@get call.respond(HttpStatusCode.BadRequest, "Segment does not belong to this flag")
                        }
                        call.respond(mapSegmentToResponse(segment))
                    }
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                        
                        val request = call.receive<PutSegmentRequest>()
                        val existingSegment = segmentService.getSegment(segmentId)
                            ?: return@put call.respond(HttpStatusCode.NotFound, "Segment not found")
                        
                        if (existingSegment.flagId != flagId) {
                            return@put call.respond(HttpStatusCode.BadRequest, "Segment does not belong to this flag")
                        }
                        
                        val updatedBy = call.getSubject()
                        
                        val updatedSegment = segmentService.updateSegment(
                            existingSegment.copy(
                                description = request.description,
                                rolloutPercent = request.rolloutPercent
                            ),
                            updatedBy = updatedBy
                        )
                        
                        call.respond(mapSegmentToResponse(updatedSegment))
                    }
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                        
                        val existingSegment = segmentService.getSegment(segmentId)
                            ?: return@delete call.respond(HttpStatusCode.NotFound, "Segment not found")
                        
                        if (existingSegment.flagId != flagId) {
                            return@delete call.respond(HttpStatusCode.BadRequest, "Segment does not belong to this flag")
                        }
                        
                        val updatedBy = call.getSubject()
                        segmentService.deleteSegment(segmentId, updatedBy)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
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