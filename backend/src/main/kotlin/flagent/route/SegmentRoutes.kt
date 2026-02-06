package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.SegmentService
import flagent.service.command.CreateSegmentCommand
import flagent.service.command.PutSegmentCommand
import flagent.util.getSubject
import flagent.route.mapper.ResponseMappers.mapSegmentToResponse
import flagent.route.mapper.ResponseMappers.mapConstraintToResponse
import flagent.route.mapper.ResponseMappers.mapDistributionToResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Segment CRUD API endpoints
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
                    
                    val command = CreateSegmentCommand(
                        flagId = flagId,
                        description = request.description,
                        rolloutPercent = request.rolloutPercent
                    )
                    val segment = segmentService.createSegment(command, updatedBy)
                    
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
                        
                        val command = PutSegmentCommand(
                            description = request.description,
                            rolloutPercent = request.rolloutPercent
                        )
                        val updatedSegment = segmentService.updateSegment(segmentId, command, updatedBy)
                        
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