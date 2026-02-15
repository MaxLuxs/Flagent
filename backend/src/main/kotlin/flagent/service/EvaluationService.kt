package flagent.service

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import flagent.integration.firebase.FirebaseAnalyticsReporter
import flagent.recorder.DataRecordingService
import flagent.recorder.EvaluationEventRecorder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Evaluation service - orchestrates flag evaluation
 */
@Suppress("UNCHECKED_CAST")
class EvaluationService(
    private val evalCache: EvalCache,
    private val evaluateFlagUseCase: EvaluateFlagUseCase,
    private val dataRecordingService: DataRecordingService? = null,
    private val firebaseAnalyticsReporter: FirebaseAnalyticsReporter? = null,
    private val evaluationEventRecorder: EvaluationEventRecorder? = null
) {
    private val random = Random()

    /**
     * Evaluate flag by ID or Key.
     * @param environmentId when set (enterprise), only flags with matching environmentId or null (global) are returned
     */
    suspend fun evaluateFlag(
        flagID: Int?,
        flagKey: String?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean,
        environmentId: Long? = null,
        clientId: String? = null
    ): EvalResult {
        val flag = when {
            flagID != null -> evalCache.getByFlagKeyOrID(flagID)
            flagKey != null -> evalCache.getByFlagKeyOrID(flagKey)
            else -> null
        }
        val filteredFlag = filterByEnvironment(flag, environmentId)

        return evaluateFlagWithContext(
            flag = filteredFlag,
            flagID = flagID ?: 0,
            flagKey = flagKey ?: "",
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            enableDebug = enableDebug,
            clientId = clientId
        )
    }

    /**
     * Evaluate flags by tags.
     * @param environmentId when set (enterprise), only flags with matching environmentId or null (global) are returned
     */
    suspend fun evaluateFlagsByTags(
        tags: List<String>,
        operator: String?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean,
        environmentId: Long? = null,
        clientId: String? = null
    ): List<EvalResult> {
        val flags = evalCache.getByTags(tags, operator)
        return flags.map { flag ->
            val filteredFlag = filterByEnvironment(flag, environmentId)
            evaluateFlagWithContext(
                flag = filteredFlag,
                flagID = flag.id,
                flagKey = flag.key,
                entityID = entityID,
                entityType = entityType,
                entityContext = entityContext,
                enableDebug = enableDebug,
                clientId = clientId
            )
        }
    }

    private fun filterByEnvironment(flag: Flag?, environmentId: Long?): Flag? {
        if (flag == null) return null
        if (environmentId == null) return flag
        if (flag.environmentId == null) return flag
        return if (flag.environmentId == environmentId) flag else null
    }

    /**
     * Evaluate flag with context - delegates to EvaluateFlagUseCase (shared evaluator).
     */
    private suspend fun evaluateFlagWithContext(
        flag: Flag?,
        flagID: Int,
        flagKey: String,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean,
        clientId: String? = null
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
        if (AppConfig.firebaseAnalyticsEnabled) {
            firebaseAnalyticsReporter?.recordAsync(result)
        }
        evaluationEventRecorder?.record(flag.id, result.timestamp, clientId)

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
