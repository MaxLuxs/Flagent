package flagent.api.model

import kotlinx.serialization.Serializable

/**
 * Request/Response models for Flag API
 */

@Serializable
data class CreateFlagRequest(
    val description: String,
    val key: String? = null,
    val template: String? = null,
    val environmentId: Long? = null,
    val projectId: Long? = null,
    val dependsOn: List<String>? = null
)

@Serializable
data class PutFlagRequest(
    val description: String? = null,
    val key: String? = null,
    val dataRecordsEnabled: Boolean? = null,
    val entityType: String? = null,
    val notes: String? = null,
    val environmentId: Long? = null,
    val projectId: Long? = null,
    val dependsOn: List<String>? = null
)

@Serializable
data class FlagResponse(
    val id: Int,
    val key: String,
    val description: String,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val enabled: Boolean,
    val snapshotID: Int,
    val notes: String? = null,
    val dataRecordsEnabled: Boolean,
    val entityType: String? = null,
    val environmentId: Long? = null,
    val projectId: Long? = null,
    val dependsOn: List<String> = emptyList(),
    val segments: List<SegmentResponse> = emptyList(),
    val variants: List<VariantResponse> = emptyList(),
    val tags: List<TagResponse> = emptyList(),
    val updatedAt: String? = null,
    /** True when flag is archived (soft-deleted). Use restore to revive or permanent delete to remove. */
    val archived: Boolean = false
)

@Serializable
data class SegmentResponse(
    val id: Int,
    val flagID: Int,
    val description: String? = null,
    val rank: Int,
    val rolloutPercent: Int,
    val constraints: List<ConstraintResponse> = emptyList(),
    val distributions: List<DistributionResponse> = emptyList()
)

@Serializable
data class VariantResponse(
    val id: Int,
    val flagID: Int,
    val key: String,
    val attachment: Map<String, String>? = null
)

@Serializable
data class ConstraintResponse(
    val id: Int,
    val segmentID: Int,
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class DistributionResponse(
    val id: Int,
    val segmentID: Int,
    val variantID: Int,
    val variantKey: String? = null,
    val percent: Int
)

@Serializable
data class TagResponse(
    val id: Int,
    val value: String
)

@Serializable
data class SetFlagEnabledRequest(
    val enabled: Boolean
)

@Serializable
data class BatchFlagEnabledRequest(
    val ids: List<Int>,
    val enabled: Boolean
)

@Serializable
data class CreateSegmentRequest(
    val description: String,
    val rolloutPercent: Int
)

@Serializable
data class PutSegmentRequest(
    val description: String,
    val rolloutPercent: Int
)

@Serializable
data class PutSegmentReorderRequest(
    val segmentIDs: List<Int>
)

@Serializable
data class CreateConstraintRequest(
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class PutConstraintRequest(
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class PutDistributionsRequest(
    val distributions: List<DistributionRequest>
)

@Serializable
data class DistributionRequest(
    val variantID: Int,
    val variantKey: String? = null,
    val percent: Int
)

@Serializable
data class CreateVariantRequest(
    val key: String,
    val attachment: Map<String, String>? = null
)

@Serializable
data class PutVariantRequest(
    val key: String,
    val attachment: Map<String, String>? = null
)

@Serializable
data class CreateTagRequest(
    val value: String
)

@Serializable
data class FlagSnapshotResponse(
    val id: Int,
    val updatedBy: String? = null,
    val flag: FlagResponse,
    val updatedAt: String
)
