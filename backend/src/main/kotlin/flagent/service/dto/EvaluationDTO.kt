package flagent.service.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Evaluation request DTO
 */
@Serializable
data class EvaluationRequestDTO(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: Map<String, String>? = null,
    val flagID: Int? = null,
    val flagKey: String? = null,
    val enableDebug: Boolean = false
)

/**
 * Evaluation response DTO
 */
@Serializable
data class EvaluationResponseDTO(
    val flagID: Int,
    val flagKey: String,
    val flagSnapshotID: Int,
    val flagTags: List<String>,
    val segmentID: Int? = null,
    val variantID: Int? = null,
    val variantKey: String? = null,
    val variantAttachment: JsonObject? = null,
    val evalContext: EvalContextDTO,
    val evalDebugLog: EvalDebugLogDTO? = null,
    val timestamp: Long
)

/**
 * Evaluation context DTO
 */
@Serializable
data class EvalContextDTO(
    val entityID: String?,
    val entityType: String?,
    val entityContext: Map<String, String>?
)

/**
 * Evaluation debug log DTO
 */
@Serializable
data class EvalDebugLogDTO(
    val message: String = "",
    val segmentDebugLogs: List<SegmentDebugLogDTO> = emptyList()
)

/**
 * Segment debug log DTO
 */
@Serializable
data class SegmentDebugLogDTO(
    val segmentID: Int,
    val message: String
)
