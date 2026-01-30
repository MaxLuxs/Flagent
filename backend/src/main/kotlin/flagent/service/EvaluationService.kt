package flagent.service

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import flagent.domain.entity.*
import flagent.domain.usecase.ConstraintEvaluationUseCase
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import flagent.recorder.DataRecordingService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Evaluation service - handles flag evaluation logic
 * Maps to pkg/handler/eval.go from original project
 */
class EvaluationService(
    private val evalCache: EvalCache,
    private val dataRecordingService: DataRecordingService? = null
) {
    private val random = Random()
    private val constraintEvaluationUseCase = ConstraintEvaluationUseCase()
    
    /**
     * Evaluate flag by ID or Key
     */
    fun evaluateFlag(
        flagID: Int?,
        flagKey: String?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult {
        val flag = when {
            flagID != null -> evalCache.getByFlagKeyOrID(flagID)
            flagKey != null -> evalCache.getByFlagKeyOrID(flagKey)
            else -> null
        }
        
        return evaluateFlagWithContext(
            flag = flag,
            flagID = flagID ?: 0,
            flagKey = flagKey ?: "",
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            enableDebug = enableDebug
        )
    }
    
    /**
     * Evaluate flags by tags
     */
    fun evaluateFlagsByTags(
        tags: List<String>,
        operator: String?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): List<EvalResult> {
        val flags = evalCache.getByTags(tags, operator)
        return flags.map { flag ->
            evaluateFlagWithContext(
                flag = flag,
                flagID = flag.id,
                flagKey = flag.key,
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext,
                enableDebug = enableDebug
            )
        }
    }
    
    /**
     * Evaluate flag with context
     */
    private fun evaluateFlagWithContext(
        flag: Flag?,
        flagID: Int,
        flagKey: String,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult {
        // Handle null flag
        if (flag == null) {
            return createBlankResult(
                flag = null,
                flagID = flagID,
                flagKey = flagKey,
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext,
                message = "flagID $flagID not found or deleted"
            )
        }
        
        // Check if flag is enabled
        if (!flag.enabled) {
            return createBlankResult(
                flag = flag,
                flagID = flagID,
                flagKey = flagKey,
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext,
                message = "flagID ${flag.id} is not enabled"
            )
        }
        
        // Check if flag has segments
        if (flag.segments.isEmpty()) {
            return createBlankResult(
                flag = flag,
                flagID = flagID,
                flagKey = flagKey,
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext,
                message = "flagID ${flag.id} has no segments"
            )
        }
        
        // Generate entityID if not provided
        val finalEntityID = entityID ?: "randomly_generated_${random.nextInt()}"
        val finalEntityType = entityType ?: flag.entityType ?: ""
        
        // Prepare flag for evaluation
        val flagEvaluation = flag.prepareEvaluation()
        
        // Evaluate segments
        val segmentDebugLogs = mutableListOf<SegmentDebugLog>()
        var variantID: Int? = null
        var segmentID: Int? = null
        
        for (segment in flag.segments) {
            segmentID = segment.id
            val segmentEvaluation = segment.prepareEvaluation()
            
            val (resultVariantID, log, evalNextSegment) = evaluateSegment(
                flagID = flag.id,
                segment = segment,
                segmentEvaluation = segmentEvaluation,
                entityID = finalEntityID,
                entityContext = entityContext,
                enableDebug = enableDebug
            )
            
            if (AppConfig.evalDebugEnabled && enableDebug) {
                segmentDebugLogs.add(log)
            }
            
            if (resultVariantID != null) {
                variantID = resultVariantID
            }
            
            if (!evalNextSegment) {
                break
            }
        }
        
        // Build result
        val variant = variantID?.let { flagEvaluation.variantsMap[it] }
        
        val result = EvalResult(
            flagID = flag.id,
            flagKey = flag.key,
            flagSnapshotID = flag.snapshotId,
            flagTags = flag.tags.map { it.value },
            segmentID = segmentID,
            variantID = variantID,
            variantKey = variant?.key,
            variantAttachment = variant?.attachment,
            evalContext = EvalContext(
                entityID = finalEntityID,
                entityType = finalEntityType,
                entityContext = entityContext?.let { mapToJsonObject(it) }
            ),
            evalDebugLog = if (enableDebug) EvalDebugLog(
                segmentDebugLogs = segmentDebugLogs
            ) else null,
            timestamp = System.currentTimeMillis()
        )
        
        // Record evaluation result if recording is enabled
        if (AppConfig.recorderEnabled && AppConfig.evalLoggingEnabled) {
            dataRecordingService?.recordAsync(result)
        }
        
        return result
    }
    
    /**
     * Evaluate segment
     * Returns: (variantID, debugLog, evalNextSegment)
     */
    private fun evaluateSegment(
        flagID: Int,
        segment: Segment,
        segmentEvaluation: Segment.SegmentEvaluation,
        entityID: String,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): Triple<Int?, SegmentDebugLog, Boolean> {
        // Check constraints if present
        if (segment.constraints.isNotEmpty()) {
            if (entityContext == null) {
                return Triple(
                    null,
                    SegmentDebugLog(
                        segmentID = segment.id,
                        message = "constraints are present in the segment_id ${segment.id}, but got invalid entity_context"
                    ),
                    true
                )
            }
            
            // Evaluate constraints using ConstraintEvaluationUseCase
            val evaluationContext = EvaluationContext(
                entityID = EntityID(entityID),
                entityType = null,
                entityContext = entityContext
            )
            
            val constraintsMatch = constraintEvaluationUseCase.evaluate(segment.constraints, evaluationContext)
            
            if (!constraintsMatch) {
                return Triple(
                    null,
                    SegmentDebugLog(
                        segmentID = segment.id,
                        message = "constraints did not match"
                    ),
                    true // Continue to next segment
                )
            }
        }
        
        // Evaluate distribution rollout
        val (variantID, debugMsg) = segmentEvaluation.distributionArray.rollout(
            entityID = entityID,
            salt = flagID.toString(),
            rolloutPercent = segment.rolloutPercent
        )
        
        val log = SegmentDebugLog(
            segmentID = segment.id,
            message = "matched all constraints. $debugMsg"
        )
        
        // If we matched, don't evaluate next segment
        return Triple(variantID, log, variantID == null)
    }
    
    private fun createBlankResult(
        flag: Flag?,
        flagID: Int,
        flagKey: String,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        message: String
    ): EvalResult {
        return EvalResult(
            flagID = flagID,
            flagKey = flagKey,
            flagSnapshotID = flag?.snapshotId ?: 0,
            flagTags = flag?.tags?.map { it.value } ?: emptyList(),
            segmentID = null,
            variantID = null,
            variantKey = null,
            variantAttachment = null,
            evalContext = EvalContext(
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext?.let { mapToJsonObject(it) }
            ),
            evalDebugLog = EvalDebugLog(
                message = message,
                segmentDebugLogs = emptyList()
            ),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Convert Map<String, Any> to JsonObject
     */
    private fun mapToJsonObject(map: Map<String, Any>): JsonObject {
        return buildJsonObject {
            map.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    is Map<*, *> -> put(key, mapToJsonObject(value as Map<String, Any>))
                    else -> put(key, value.toString())
                }
            }
        }
    }
}


/**
 * Evaluation result
 */
@Serializable
data class EvalResult(
    val flagID: Int,
    val flagKey: String,
    val flagSnapshotID: Int,
    val flagTags: List<String>,
    val segmentID: Int? = null,
    val variantID: Int? = null,
    val variantKey: String? = null,
    val variantAttachment: JsonObject? = null,
    val evalContext: EvalContext,
    val evalDebugLog: EvalDebugLog? = null,
    val timestamp: Long
)

/**
 * Evaluation context
 */
@Serializable
data class EvalContext(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: JsonObject? = null
)

/**
 * Evaluation debug log
 */
@Serializable
data class EvalDebugLog(
    val message: String = "",
    val segmentDebugLogs: List<SegmentDebugLog> = emptyList()
)

/**
 * Segment debug log
 */
@Serializable
data class SegmentDebugLog(
    val segmentID: Int,
    val message: String
)
