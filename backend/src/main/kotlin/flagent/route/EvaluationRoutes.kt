package flagent.route

import flagent.api.EvalEnvironmentProvider
import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.EvaluationService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Evaluation API endpoints
 */
fun Routing.configureEvaluationRoutes(evaluationService: EvaluationService) {
    route(ApiConstants.API_BASE_PATH) {
            post("/evaluation") {
                val request = call.receive<EvaluationRequest>()
                val environmentId = EvalEnvironmentProvider.getEnvironmentId(call as Any)
                val clientId = call.request.header("X-Client-Id")?.takeIf { it.isNotBlank() }
                val result = evaluationService.evaluateFlag(
                    flagID = request.flagID,
                    flagKey = request.flagKey,
                    entityID = request.entityID,
                    entityType = request.entityType,
                    entityContext = request.entityContext?.mapValues { it.value } as? Map<String, Any>,
                    enableDebug = request.enableDebug,
                    environmentId = environmentId,
                    clientId = clientId
                )
                call.respond(mapEvalResultToResponse(result))
            }
            
            post("/evaluation/batch") {
                val request = try {
                    call.receive<EvaluationBatchRequest>()
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to ("Invalid request body: ${e.message}")))
                }
                
                val hasFlagSelector = request.flagIDs.isNotEmpty() || request.flagKeys.isNotEmpty() || request.flagTags.isNotEmpty()
                if (!hasFlagSelector) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "At least one of flagIDs, flagKeys, or flagTags is required"))
                }
                if (request.entities.isEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "entities must not be empty when flagIDs, flagKeys, or flagTags are provided"))
                }
                
                val results = mutableListOf<EvaluationResponse>()
                
                val environmentId = EvalEnvironmentProvider.getEnvironmentId(call as Any)
                val clientId = call.request.header("X-Client-Id")?.takeIf { it.isNotBlank() }
                // Evaluate by tags
                if (request.flagTags.isNotEmpty()) {
                    request.entities.forEach { entity ->
                        val tagResults = evaluationService.evaluateFlagsByTags(
                            tags = request.flagTags,
                            operator = request.flagTagsOperator,
                            entityID = entity.entityID,
                            entityType = entity.entityType,
                            entityContext = entity.entityContext?.mapValues { it.value } as? Map<String, Any>,
                            enableDebug = request.enableDebug,
                            environmentId = environmentId,
                            clientId = clientId
                        )
                        results.addAll(tagResults.map { mapEvalResultToResponse(it) })
                    }
                }
                
                // Evaluate by flag IDs
                request.flagIDs.forEach { flagID ->
                    request.entities.forEach { entity ->
                        val result = evaluationService.evaluateFlag(
                            flagID = flagID,
                            flagKey = null,
                            entityID = entity.entityID,
                            entityType = entity.entityType,
                            entityContext = entity.entityContext?.mapValues { it.value } as? Map<String, Any>,
                            enableDebug = request.enableDebug,
                            environmentId = environmentId,
                            clientId = clientId
                        )
                        results.add(mapEvalResultToResponse(result))
                    }
                }
                
                // Evaluate by flag keys
                request.flagKeys.forEach { flagKey ->
                    request.entities.forEach { entity ->
                        val result = evaluationService.evaluateFlag(
                            flagID = null,
                            flagKey = flagKey,
                            entityID = entity.entityID,
                            entityType = entity.entityType,
                            entityContext = entity.entityContext?.mapValues { it.value } as? Map<String, Any>,
                            enableDebug = request.enableDebug,
                            environmentId = environmentId,
                            clientId = clientId
                        )
                        results.add(mapEvalResultToResponse(result))
                    }
                }
                
                call.respond(EvaluationBatchResponse(evaluationResults = results))
            }
        }
}

private fun mapEvalResultToResponse(result: flagent.service.EvalResult): EvaluationResponse {
    return EvaluationResponse(
        flagID = result.flagID,
        flagKey = result.flagKey,
        flagSnapshotID = result.flagSnapshotID,
        flagTags = result.flagTags,
        segmentID = result.segmentID,
        variantID = result.variantID,
        variantKey = result.variantKey,
        variantAttachment = result.variantAttachment?.entries?.associate { 
            it.key to it.value.toString() 
        },
        evalContext = EvalContextResponse(
            entityID = result.evalContext.entityID,
            entityType = result.evalContext.entityType,
            entityContext = result.evalContext.entityContext?.mapValues { it.value.toString() }
        ),
        evalDebugLog = result.evalDebugLog?.let {
            EvalDebugLogResponse(
                msg = it.message,
                segmentDebugLogs = it.segmentDebugLogs.map { log ->
                    SegmentDebugLogResponse(
                        segmentID = log.segmentID,
                        msg = log.message
                    )
                }
            )
        },
        timestamp = result.timestamp
    )
}
