package flagent.route

import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.service.EvaluationService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Evaluation routes
 * Maps to pkg/handler/eval.go from original project
 */
fun Routing.configureEvaluationRoutes(evaluationService: EvaluationService) {
    route(ApiConstants.API_BASE_PATH) {
            post("/evaluation") {
                val request = call.receive<EvaluationRequest>()
                
                val result = evaluationService.evaluateFlag(
                    flagID = request.flagID,
                    flagKey = request.flagKey,
                    entityID = request.entityID,
                    entityType = request.entityType,
                    entityContext = request.entityContext?.mapValues { it.value } as? Map<String, Any>,
                    enableDebug = request.enableDebug
                )
                
                call.respond(mapEvalResultToResponse(result))
            }
            
            post("/evaluation/batch") {
                val request = call.receive<EvaluationBatchRequest>()
                val results = mutableListOf<EvaluationResponse>()
                
                // Evaluate by tags
                if (request.flagTags.isNotEmpty()) {
                    request.entities.forEach { entity ->
                        val tagResults = evaluationService.evaluateFlagsByTags(
                            tags = request.flagTags,
                            operator = request.flagTagsOperator,
                            entityID = entity.entityID,
                            entityType = entity.entityType,
                            entityContext = entity.entityContext?.mapValues { it.value } as? Map<String, Any>,
                            enableDebug = request.enableDebug
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
                            enableDebug = request.enableDebug
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
                            enableDebug = request.enableDebug
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
