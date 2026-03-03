package com.flagent.openfeature

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Minimal OpenFeature-like evaluation context.
 *
 * @property targetingKey Stable key for deterministic bucketing (e.g., user ID).
 * @property attributes Additional attributes for targeting and rollout.
 */
data class EvaluationContext(
    val targetingKey: String? = null,
    val attributes: Map<String, AttributeValue> = emptyMap()
)

/**
 * Typed attribute value used in [EvaluationContext].
 *
 * This mirrors common OpenFeature attribute types and is converted into JSON
 * when calling the Flagent evaluation API.
 */
sealed interface AttributeValue {
    data class StringValue(val value: String) : AttributeValue
    data class BooleanValue(val value: Boolean) : AttributeValue
    data class IntValue(val value: Int) : AttributeValue
    data class DoubleValue(val value: Double) : AttributeValue
    data class ObjectValue(val value: JsonObject) : AttributeValue
}

/**
 * Reason for a particular flag evaluation, loosely aligned with OpenFeature.
 *
 * This is intentionally small; more detailed reasons can be added later
 * without breaking the public API.
 */
enum class ResolutionReason {
    TARGETING_MATCH,
    DEFAULT,
    ERROR,
    UNKNOWN
}

/**
 * Full evaluation result for a flag or experiment.
 *
 * @param key logical flag key used in the call
 * @param value resolved value (or default on error)
 * @param variant variant identifier (for experiments) when applicable
 * @param reason coarse-grained reason describing how the value was obtained
 * @param errorCode optional backend or transport error code
 */
data class FlagEvaluationDetails<T>(
    val key: String,
    val value: T,
    val variant: String? = null,
    val reason: ResolutionReason,
    val errorCode: String? = null
)

/**
 * High-level client interface similar to the OpenFeature client API.
 *
 * Implementations delegate all resolution logic to a [FeatureProvider],
 * which can be backed by Flagent or any other evaluation engine.
 */
interface OpenFeatureClient {

    suspend fun getBooleanDetails(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Boolean>

    suspend fun getBooleanValue(
        key: String,
        defaultValue: Boolean = false,
        context: EvaluationContext? = null
    ): Boolean = getBooleanDetails(key, defaultValue, context).value

    suspend fun getStringDetails(
        key: String,
        defaultValue: String,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<String>

    suspend fun getStringValue(
        key: String,
        defaultValue: String = "",
        context: EvaluationContext? = null
    ): String = getStringDetails(key, defaultValue, context).value

    suspend fun getIntDetails(
        key: String,
        defaultValue: Int,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Int>

    suspend fun getIntValue(
        key: String,
        defaultValue: Int = 0,
        context: EvaluationContext? = null
    ): Int = getIntDetails(key, defaultValue, context).value

    suspend fun getDoubleDetails(
        key: String,
        defaultValue: Double,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Double>

    suspend fun getDoubleValue(
        key: String,
        defaultValue: Double = 0.0,
        context: EvaluationContext? = null
    ): Double = getDoubleDetails(key, defaultValue, context).value

    suspend fun getObjectDetails(
        key: String,
        defaultValue: JsonObject,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<JsonObject>

    suspend fun getObjectValue(
        key: String,
        defaultValue: JsonObject = buildJsonObject { },
        context: EvaluationContext? = null
    ): JsonObject = getObjectDetails(key, defaultValue, context).value
}

/**
 * Provider that performs low-level flag resolution.
 *
 * A provider is responsible for talking to Flagent (or another backend)
 * and mapping raw responses into strongly typed [FlagEvaluationDetails].
 */
interface FeatureProvider {
    suspend fun resolveBoolean(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Boolean>

    suspend fun resolveString(
        key: String,
        defaultValue: String,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<String>

    suspend fun resolveInt(
        key: String,
        defaultValue: Int,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Int>

    suspend fun resolveDouble(
        key: String,
        defaultValue: Double,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<Double>

    suspend fun resolveObject(
        key: String,
        defaultValue: JsonObject,
        context: EvaluationContext? = null
    ): FlagEvaluationDetails<JsonObject>
}

/**
 * Default [OpenFeatureClient] implementation delegating to a [FeatureProvider].
 */
class DefaultOpenFeatureClient(
    private val provider: FeatureProvider
) : OpenFeatureClient {

    override suspend fun getBooleanDetails(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Boolean> =
        provider.resolveBoolean(key, defaultValue, context)

    override suspend fun getStringDetails(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ): FlagEvaluationDetails<String> =
        provider.resolveString(key, defaultValue, context)

    override suspend fun getIntDetails(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Int> =
        provider.resolveInt(key, defaultValue, context)

    override suspend fun getDoubleDetails(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ): FlagEvaluationDetails<Double> =
        provider.resolveDouble(key, defaultValue, context)

    override suspend fun getObjectDetails(
        key: String,
        defaultValue: JsonObject,
        context: EvaluationContext?
    ): FlagEvaluationDetails<JsonObject> =
        provider.resolveObject(key, defaultValue, context)
}

internal fun AttributeValue.toJsonElement(): JsonElement =
    when (this) {
        is AttributeValue.StringValue -> JsonPrimitive(value)
        is AttributeValue.BooleanValue -> JsonPrimitive(value)
        is AttributeValue.IntValue -> JsonPrimitive(value)
        is AttributeValue.DoubleValue -> JsonPrimitive(value)
        is AttributeValue.ObjectValue -> value
    }

internal fun JsonElement.asStringOrNull(): String? =
    when (val primitive = this as? JsonPrimitive) {
        null -> null
        else -> primitive.content
    }
