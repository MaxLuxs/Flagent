package flagent.api.model

import kotlinx.serialization.Serializable

/**
 * Request/Response models for Evaluation API
 */

@Serializable
data class EvaluationRequest(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: Map<String, String>? = null,
    val flagID: Int? = null,
    val flagKey: String? = null,
    val enableDebug: Boolean = false
)

@Serializable
data class EvaluationBatchRequest(
    val entities: List<EntityRequest>,
    val flagIDs: List<Int> = emptyList(),
    val flagKeys: List<String> = emptyList(),
    val flagTags: List<String> = emptyList(),
    val flagTagsOperator: String? = null, // "ANY" or "ALL"
    val enableDebug: Boolean = false
)

@Serializable
data class EntityRequest(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: Map<String, String>? = null
)

@Serializable
data class EvaluationResponse(
    val flagID: Int,
    val flagKey: String,
    val flagSnapshotID: Int,
    val flagTags: List<String>,
    val segmentID: Int? = null,
    val variantID: Int? = null,
    val variantKey: String? = null,
    val variantAttachment: Map<String, String>? = null,
    val evalContext: EvalContextResponse,
    val evalDebugLog: EvalDebugLogResponse? = null,
    val timestamp: Long
)

@Serializable
data class EvalContextResponse(
    val entityID: String? = null,
    val entityType: String? = null,
    val entityContext: Map<String, String>? = null
)

@Serializable
data class EvalDebugLogResponse(
    val msg: String = "",
    val segmentDebugLogs: List<SegmentDebugLogResponse> = emptyList()
)

@Serializable
data class SegmentDebugLogResponse(
    val segmentID: Int,
    val msg: String
)

@Serializable
data class EvaluationBatchResponse(
    val evaluationResults: List<EvaluationResponse>
)
