package flagent.frontend.api

import flagent.api.model.*
import flagent.api.model.PutSegmentReorderRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * API Client for Flagent backend
 */
class ApiClient(
    private val baseUrl: String = ""
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }
    
    private fun getApiPath(path: String): String {
        return if (baseUrl.isEmpty()) {
            "/api/v1$path"
        } else {
            "$baseUrl/api/v1$path"
        }
    }
    
    /**
     * Get all flags
     */
    suspend fun getFlags(): List<FlagResponse> {
        return client.get(getApiPath("/flags")).body()
    }
    
    /**
     * Get flag by ID
     */
    suspend fun getFlag(id: Int): FlagResponse {
        return client.get(getApiPath("/flags/$id")).body()
    }
    
    /**
     * Create flag
     */
    suspend fun createFlag(flag: CreateFlagRequest): FlagResponse {
        return client.post(getApiPath("/flags")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Update flag
     */
    suspend fun updateFlag(id: Int, flag: UpdateFlagRequest): FlagResponse {
        return client.put(getApiPath("/flags/$id")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Update flag with full PutFlagRequest
     */
    suspend fun updateFlagFull(id: Int, flag: PutFlagRequest): FlagResponse {
        return client.put(getApiPath("/flags/$id")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Delete flag
     */
    suspend fun deleteFlag(id: Int) {
        client.delete(getApiPath("/flags/$id"))
    }
    
    /**
     * Evaluate flag
     */
    suspend fun evaluate(request: EvaluationRequest): EvaluationResponse {
        return client.post(getApiPath("/evaluation")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Segments
    suspend fun getSegments(flagId: Int): List<SegmentResponse> {
        return client.get(getApiPath("/flags/$flagId/segments")).body()
    }
    
    suspend fun createSegment(flagId: Int, request: CreateSegmentRequest): SegmentResponse {
        return client.post(getApiPath("/flags/$flagId/segments")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateSegment(flagId: Int, segmentId: Int, request: PutSegmentRequest): SegmentResponse {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteSegment(flagId: Int, segmentId: Int) {
        client.delete(getApiPath("/flags/$flagId/segments/$segmentId"))
    }
    
    suspend fun reorderSegments(flagId: Int, request: PutSegmentReorderRequest) {
        client.put(getApiPath("/flags/$flagId/segments/reorder")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
    
    // Constraints
    suspend fun getConstraints(flagId: Int, segmentId: Int): List<ConstraintResponse> {
        return client.get(getApiPath("/flags/$flagId/segments/$segmentId/constraints")).body()
    }
    
    suspend fun createConstraint(flagId: Int, segmentId: Int, request: CreateConstraintRequest): ConstraintResponse {
        return client.post(getApiPath("/flags/$flagId/segments/$segmentId/constraints")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateConstraint(flagId: Int, segmentId: Int, constraintId: Int, request: PutConstraintRequest): ConstraintResponse {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId/constraints/$constraintId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteConstraint(flagId: Int, segmentId: Int, constraintId: Int) {
        client.delete(getApiPath("/flags/$flagId/segments/$segmentId/constraints/$constraintId"))
    }
    
    // Distributions
    suspend fun getDistributions(flagId: Int, segmentId: Int): List<DistributionResponse> {
        return client.get(getApiPath("/flags/$flagId/segments/$segmentId/distributions")).body()
    }
    
    suspend fun updateDistributions(flagId: Int, segmentId: Int, request: PutDistributionsRequest): List<DistributionResponse> {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId/distributions")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Variants
    suspend fun getVariants(flagId: Int): List<VariantResponse> {
        return client.get(getApiPath("/flags/$flagId/variants")).body()
    }
    
    suspend fun createVariant(flagId: Int, request: CreateVariantRequest): VariantResponse {
        return client.post(getApiPath("/flags/$flagId/variants")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateVariant(flagId: Int, variantId: Int, request: PutVariantRequest): VariantResponse {
        return client.put(getApiPath("/flags/$flagId/variants/$variantId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteVariant(flagId: Int, variantId: Int) {
        client.delete(getApiPath("/flags/$flagId/variants/$variantId"))
    }
    
    // Flag Snapshots (History)
    suspend fun getFlagSnapshots(flagId: Int, limit: Int? = null, offset: Int = 0): List<FlagSnapshotResponse> {
        val url = buildString {
            append(getApiPath("/flags/$flagId/snapshots"))
            if (limit != null) append("?limit=$limit")
            if (offset > 0) append(if (limit != null) "&offset=$offset" else "?offset=$offset")
        }
        return client.get(url).body()
    }
    
    // Deleted Flags
    suspend fun getDeletedFlags(): List<FlagResponse> {
        return client.get(getApiPath("/flags?deleted=true")).body()
    }
    
    suspend fun restoreFlag(flagId: Int): FlagResponse {
        return client.put(getApiPath("/flags/$flagId/restore")).body()
    }
    
    // Tags
    suspend fun getAllTags(): List<TagResponse> {
        return client.get(getApiPath("/tags")).body()
    }
    
    suspend fun addTagToFlag(flagId: Int, tagValue: String): TagResponse {
        return client.post(getApiPath("/flags/$flagId/tags")) {
            contentType(ContentType.Application.Json)
            setBody(CreateTagRequest(tagValue))
        }.body()
    }
    
    suspend fun removeTagFromFlag(flagId: Int, tagId: Int) {
        client.delete(getApiPath("/flags/$flagId/tags/$tagId"))
    }
    
    // Entity Types
    suspend fun getEntityTypes(): List<String> {
        return client.get(getApiPath("/flags/entity_types")).body()
    }
    
    // Batch Evaluation
    suspend fun evaluateBatch(request: EvaluationBatchRequest): EvaluationBatchResponse {
        return client.post(getApiPath("/evaluation/batch")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Set Flag Enabled
    suspend fun setFlagEnabled(flagId: Int, enabled: Boolean): FlagResponse {
        return client.put(getApiPath("/flags/$flagId/enabled")) {
            contentType(ContentType.Application.Json)
            setBody(SetFlagEnabledRequest(enabled))
        }.body()
    }
}
