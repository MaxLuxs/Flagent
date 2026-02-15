package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.PutDistributionsRequest
import flagent.route.mapper.ResponseMappers.mapDistributionToResponse
import flagent.service.DistributionService
import flagent.service.command.DistributionItemCommand
import flagent.service.command.PutDistributionsCommand
import flagent.util.getSubject
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Distribution CRUD API endpoints
 */
fun Routing.configureDistributionRoutes(distributionService: DistributionService) {
    route(ApiConstants.API_BASE_PATH) {
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
                    val command = PutDistributionsCommand(
                        flagId = flagId,
                        segmentId = segmentId,
                        distributions = request.distributions.map {
                            DistributionItemCommand(
                                variantID = it.variantID,
                                variantKey = it.variantKey,
                                percent = it.percent
                            )
                        }
                    )
                    distributionService.updateDistributions(command, updatedBy)

                    // Return updated distributions
                    val updatedDistributions =
                        distributionService.findDistributionsBySegmentId(segmentId)
                    call.respond(updatedDistributions.map { mapDistributionToResponse(it) })
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid distributions")
                }
            }
        }
    }
}