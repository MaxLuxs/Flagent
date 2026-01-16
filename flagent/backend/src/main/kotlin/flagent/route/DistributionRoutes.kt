package flagent.route

import flagent.domain.entity.Distribution
import flagent.api.model.*
import flagent.service.DistributionService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Distribution routes - operations for distributions
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureDistributionRoutes(distributionService: DistributionService) {
    route("/api/v1") {
            route("/flags/{flagId}/segments/{segmentId}/distributions") {
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                    
                    val distributions = distributionService.findDistributionsBySegmentId(segmentId)
                    call.respond(distributions.map { mapDistributionToResponse(it) })
                }
                
                put {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    val segmentId = call.parameters["segmentId"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid segment ID")
                    
                    val request = call.receive<PutDistributionsRequest>()
                    val updatedBy = call.getSubject()
                    
                    try {
                        val distributions = request.distributions.map {
                            Distribution(
                                segmentId = segmentId,
                                variantId = it.variantID,
                                variantKey = it.variantKey,
                                percent = it.percent
                            )
                        }
                        
                        distributionService.updateDistributions(flagId, segmentId, distributions, updatedBy)
                        
                        // Return updated distributions
                        val updatedDistributions = distributionService.findDistributionsBySegmentId(segmentId)
                        call.respond(updatedDistributions.map { mapDistributionToResponse(it) })
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid distributions")
                    }
                }
            }
        }
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