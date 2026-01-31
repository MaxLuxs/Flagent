package flagent.service

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import flagent.recorder.DataRecordingService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Evaluation service - delegates to EvaluateFlagUseCase (shared evaluator).
 * Maps to pkg/handler/eval.go from original project.
 */
class EvaluationService(
    private val evalCache: EvalCache,
    private val evaluateFlagUseCase: EvaluateFlagUseCase,
    private val dataRecordingService: DataRecordingService? = null
) {
    private val random = Random()

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
     * Evaluate flag with context - delegates to EvaluateFlagUseCase (shared evaluator).
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

        val finalEntityID = entityID?.takeIf { it.isNotBlank() } ?: "randomly_generated_${random.nextInt()}"
        val finalEntityType = entityType ?: flag.entityType ?: ""

        val context = EvaluationContext(
            entityID = EntityID(finalEntityID),
            entityType = finalEntityType.takeIf { it.isNotEmpty() },
            entityContext = entityContext
        )

        val useCaseResult = evaluateFlagUseCase.invoke(flag, context, enableDebug)

        val variant = useCaseResult.variantID?.let { vid -> flag.variants.find { it.id == vid } }
        val variantAttachmentJson = variant?.attachment?.let { map ->
            buildJsonObject { map.forEach { put(it.key, it.value) } }
        }

        val blankMessage = when {
            useCaseResult.variantID != null -> null
            !flag.enabled -> "flagID ${flag.id} is not enabled"
            flag.segments.isEmpty() -> "flagID ${flag.id} has no segments"
            else -> useCaseResult.debugLogs.firstOrNull()?.message ?: ""
        }
        val evalDebugLogValue = when {
            blankMessage != null -> EvalDebugLog(
                message = blankMessage,
                segmentDebugLogs = if (enableDebug) useCaseResult.debugLogs.map { log ->
                    SegmentDebugLog(segmentID = log.segmentID, message = log.message)
                } else emptyList()
            )
            enableDebug -> EvalDebugLog(
                segmentDebugLogs = useCaseResult.debugLogs.map { log ->
                    SegmentDebugLog(segmentID = log.segmentID, message = log.message)
                }
            )
            else -> null
        }

        val result = EvalResult(
            flagID = flag.id,
            flagKey = flag.key,
            flagSnapshotID = flag.snapshotId,
            flagTags = flag.tags.map { it.value },
            segmentID = useCaseResult.segmentID,
            variantID = useCaseResult.variantID,
            variantKey = variant?.key,
            variantAttachment = variantAttachmentJson,
            evalContext = EvalContext(
                entityID = finalEntityID,
                entityType = finalEntityType,
                entityContext = entityContext?.let { mapToJsonObject(it) }
            ),
            evalDebugLog = evalDebugLogValue,
            timestamp = System.currentTimeMillis()
        )

        if (AppConfig.recorderEnabled && AppConfig.evalLoggingEnabled) {
            dataRecordingService?.recordAsync(result)
        }

        return result
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
 * Evaluation result (service/API layer - uses JsonObject for variantAttachment until domain uses Map)
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

@Serializable
data class EvalContext(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: JsonObject? = null
)

@Serializable
data class EvalDebugLog(
    val message: String = "",
    val segmentDebugLogs: List<SegmentDebugLog> = emptyList()
)

@Serializable
data class SegmentDebugLog(
    val segmentID: Int,
    val message: String
)
