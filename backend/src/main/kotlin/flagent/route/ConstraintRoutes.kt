package flagent.route

import flagent.domain.entity.Constraint
import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.ConstraintService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Constraint routes - CRUD operations for constraints
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureConstraintRoutes(constraintService: ConstraintService) {
    route(ApiConstants.API_BASE_PATH) {
            route("/flags/{flagId}/segments/{segmentId}/constraints") {
                
                
                
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                    
                    val constraints = constraintService.findConstraintsBySegmentId(segmentId)
                    call.respond(constraints.map { mapConstraintToResponse(it) })
                }
                
                post {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                    
                    val request = call.receive<CreateConstraintRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val constraint = constraintService.createConstraint(
                            Constraint(
                                segmentId = segmentId,
                                property = request.property,
                                operator = request.operator,
                                value = request.value
                            ),
                            updatedBy = updatedBy
                        )
                        
                        call.respond(HttpStatusCode.OK, mapConstraintToResponse(constraint))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid constraint")
                    }
                }
                
                route("/{constraintId}") {
                    put {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                        val constraintId = call.parameters["constraintId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid constraint ID")
                        
                        val request = call.receive<PutConstraintRequest>()
                        val existingConstraint = constraintService.getConstraint(constraintId)
                            ?: return@put call.respond(HttpStatusCode.NotFound, "Constraint not found")
                        
                        if (existingConstraint.segmentId != segmentId) {
                            return@put call.respond(HttpStatusCode.BadRequest, "Constraint does not belong to this segment")
                        }
                        
                        try {
                            val updatedBy = call.getSubject()
                            val updatedConstraint = constraintService.updateConstraint(
                                existingConstraint.copy(
                                    property = request.property,
                                    operator = request.operator,
                                    value = request.value
                                ),
                                updatedBy = updatedBy
                            )
                            
                            call.respond(mapConstraintToResponse(updatedConstraint))
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid constraint")
                        }
                    }
                    
                    delete {
                        val flagId = call.parameters["flagId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                        val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                        val constraintId = call.parameters["constraintId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid constraint ID")
                        
                        val existingConstraint = constraintService.getConstraint(constraintId)
                            ?: return@delete call.respond(HttpStatusCode.NotFound, "Constraint not found")
                        
                        if (existingConstraint.segmentId != segmentId) {
                            return@delete call.respond(HttpStatusCode.BadRequest, "Constraint does not belong to this segment")
                        }
                        
                        val updatedBy = call.getSubject()
                        constraintService.deleteConstraint(constraintId, updatedBy)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
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