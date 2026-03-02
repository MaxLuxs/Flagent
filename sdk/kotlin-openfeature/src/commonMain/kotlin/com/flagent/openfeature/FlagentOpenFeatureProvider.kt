package com.flagent.openfeature

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.infrastructure.ApiClient
import com.flagent.client.infrastructure.createDefaultHttpClientEngine
import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Configuration for [FlagentOpenFeatureProvider].
 *
 * @param baseUrl Flagent API base URL, for example `http://localhost:18000/api/v1`.
 * @param authConfig Optional configuration callback for the underlying [ApiClient]
 *   (for example setting API key, basic auth or bearer token).
 */
data class FlagentOpenFeatureConfig(
    val baseUrl: String = ApiClient.BASE_URL,
    val authConfig: (ApiClient) -> Unit = {}
)

/**
 * Flagent-backed implementation of [FeatureProvider].
 *
 * This provider delegates all evaluations to the Flagent HTTP API using the
 * generated multiplatform Kotlin client (`:kotlin-client`). It calls the
 * `/evaluation` endpoint for each resolution and maps the response into
 * [FlagEvaluationDetails] in an OpenFeature-like shape.
 */
class FlagentOpenFeatureProvider(
    private val config: FlagentOpenFeatureConfig
) : FeatureProvider {

    private val api: EvaluationApi by lazy {
        val engine = createDefaultHttpClientEngine()
        val evaluationApi = EvaluationApi(baseUrl = config.baseUrl, httpClientEngine = engine)
        config.authConfig(evaluationApi)
        evaluationApi
    }

    override suspend fun resolveBoolean(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Boolean> {
        val httpResponse = api.postEvaluation(buildEvalContext(key, context))
        if (!httpResponse.success) {
            return FlagEvaluationDetails(
                key = key,
                value = defaultValue,
                variant = null,
                reason = ResolutionReason.ERROR,
                errorCode = "HTTP_${httpResponse.status}"
            )
        }

        val result = httpResponse.body()
        val enabled = result.variantKey != null
        val reason = if (enabled) ResolutionReason.TARGETING_MATCH else ResolutionReason.DEFAULT

        return FlagEvaluationDetails(
            key = key,
            value = if (enabled) true else defaultValue,
            variant = result.variantKey,
            reason = reason,
            errorCode = null
        )
    }

    override suspend fun resolveString(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ): FlagEvaluationDetails<String> {
        val httpResponse = api.postEvaluation(buildEvalContext(key, context))
        if (!httpResponse.success) {
            return FlagEvaluationDetails(
                key = key,
                value = defaultValue,
                variant = null,
                reason = ResolutionReason.ERROR,
                errorCode = "HTTP_${httpResponse.status}"
            )
        }

        val result = httpResponse.body()
        val value = extractStringValue(result) ?: defaultValue
        val reason = if (result.variantKey != null) ResolutionReason.TARGETING_MATCH else ResolutionReason.DEFAULT

        return FlagEvaluationDetails(
            key = key,
            value = value,
            variant = result.variantKey,
            reason = reason,
            errorCode = null
        )
    }

    override suspend fun resolveInt(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Int> {
        val httpResponse = api.postEvaluation(buildEvalContext(key, context))
        if (!httpResponse.success) {
            return FlagEvaluationDetails(
                key = key,
                value = defaultValue,
                variant = null,
                reason = ResolutionReason.ERROR,
                errorCode = "HTTP_${httpResponse.status}"
            )
        }

        val result = httpResponse.body()
        val value = extractNumericValue(result)?.toInt() ?: defaultValue
        val reason = if (result.variantKey != null && extractNumericValue(result) != null) {
            ResolutionReason.TARGETING_MATCH
        } else if (result.variantKey != null) {
            ResolutionReason.DEFAULT
        } else {
            ResolutionReason.DEFAULT
        }

        return FlagEvaluationDetails(
            key = key,
            value = value,
            variant = result.variantKey,
            reason = reason,
            errorCode = null
        )
    }

    override suspend fun resolveDouble(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Double> {
        val httpResponse = api.postEvaluation(buildEvalContext(key, context))
        if (!httpResponse.success) {
            return FlagEvaluationDetails(
                key = key,
                value = defaultValue,
                variant = null,
                reason = ResolutionReason.ERROR,
                errorCode = "HTTP_${httpResponse.status}"
            )
        }

        val result = httpResponse.body()
        val value = extractNumericValue(result) ?: defaultValue
        val reason = if (result.variantKey != null && extractNumericValue(result) != null) {
            ResolutionReason.TARGETING_MATCH
        } else if (result.variantKey != null) {
            ResolutionReason.DEFAULT
        } else {
            ResolutionReason.DEFAULT
        }

        return FlagEvaluationDetails(
            key = key,
            value = value,
            variant = result.variantKey,
            reason = reason,
            errorCode = null
        )
    }

    override suspend fun resolveObject(
        key: String,
        defaultValue: JsonObject,
        context: EvaluationContext?
    ): FlagEvaluationDetails<JsonObject> {
        val httpResponse = api.postEvaluation(buildEvalContext(key, context))
        if (!httpResponse.success) {
            return FlagEvaluationDetails(
                key = key,
                value = defaultValue,
                variant = null,
                reason = ResolutionReason.ERROR,
                errorCode = "HTTP_${httpResponse.status}"
            )
        }

        val result = httpResponse.body()
        val obj = result.variantAttachment ?: defaultValue
        val reason = if (result.variantKey != null) ResolutionReason.TARGETING_MATCH else ResolutionReason.DEFAULT

        return FlagEvaluationDetails(
            key = key,
            value = obj,
            variant = result.variantKey,
            reason = reason,
            errorCode = null
        )
    }

    private fun buildEvalContext(
        flagKey: String,
        context: EvaluationContext?
    ): EvalContext {
        val entityContext: JsonObject? = context
            ?.attributes
            ?.takeIf { it.isNotEmpty() }
            ?.let { attrs ->
                buildJsonObject {
                    attrs.forEach { (k, v) ->
                        put(k, v.toJsonElement())
                    }
                }
            }

        return EvalContext(
            entityID = context?.targetingKey,
            entityType = null,
            entityContext = entityContext,
            enableDebug = false,
            flagID = null,
            flagKey = flagKey,
            flagTags = null,
            flagTagsOperator = EvalContext.FlagTagsOperator.ANY
        )
    }

    private fun extractStringValue(result: EvalResult): String? {
        // Heuristics:
        // 1. If variantAttachment contains "value" primitive, prefer it.
        // 2. Fallback to variantKey.
        val attachment = result.variantAttachment
        if (attachment != null) {
            attachment["value"]?.let { element ->
                val primitive = element as? JsonPrimitive
                if (primitive != null && !primitive.isString) {
                    return primitive.content
                }
                return primitive?.content
            }
        }
        return result.variantKey
    }

    private fun extractNumericValue(result: EvalResult): Double? {
        val attachment = result.variantAttachment ?: return null
        val element: JsonElement = attachment["value"] ?: return null
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.content.toDoubleOrNull()
    }
}

