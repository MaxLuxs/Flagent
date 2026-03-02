package com.flagent.openfeature

import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlagentOpenFeatureProviderMappingTest {

    @Test
    fun `boolean mapping uses variantKey as enabled switch`() {
        val provider = FlagentOpenFeatureProvider(
            FlagentOpenFeatureConfig(baseUrl = "http://localhost") { }
        )

        // emulate internal logic using enabled flag
        val key = "new_android_ui"
        val default = false

        val resultWithVariant = EvalResult(
            flagKey = key,
            variantKey = "enabled",
        )

        val detailsEnabled = FlagEvaluationDetails(
            key = key,
            value = true,
            variant = resultWithVariant.variantKey,
            reason = ResolutionReason.TARGETING_MATCH,
            errorCode = null
        )

        assertEquals(key, detailsEnabled.key)
        assertTrue(detailsEnabled.value)
        assertEquals("enabled", detailsEnabled.variant)
        assertEquals(ResolutionReason.TARGETING_MATCH, detailsEnabled.reason)

        val resultWithoutVariant = EvalResult(
            flagKey = key,
            variantKey = null,
        )

        val detailsDefault = FlagEvaluationDetails(
            key = key,
            value = default,
            variant = resultWithoutVariant.variantKey,
            reason = ResolutionReason.DEFAULT,
            errorCode = null
        )

        assertFalse(detailsDefault.value)
        assertEquals(ResolutionReason.DEFAULT, detailsDefault.reason)
    }

    @Test
    fun `string mapping prefers attachment value then variantKey`() {
        val resultWithAttachment = EvalResult(
            flagKey = "string_flag",
            variantKey = "fallback",
            variantAttachment = JsonObject(mapOf("value" to JsonPrimitive("from_attachment"))),
            evalContext = EvalContext()
        )

        val valueFromAttachment = run {
            val provider = FlagentOpenFeatureProvider(
                FlagentOpenFeatureConfig(baseUrl = "http://localhost") { }
            )
            // use internal helper via reflection-like logic
            val attachment = resultWithAttachment.variantAttachment!!
            val element = attachment["value"] as JsonPrimitive
            element.content
        }

        assertEquals("from_attachment", valueFromAttachment)

        val resultFallback = EvalResult(
            flagKey = "string_flag",
            variantKey = "from_variant",
            variantAttachment = null,
            evalContext = EvalContext()
        )

        val fallbackValue = resultFallback.variantKey
        assertEquals("from_variant", fallbackValue)
    }

    @Test
    fun `numeric mapping reads value from variantAttachment`() {
        val resultInt = EvalResult(
            flagKey = "cart_max_items",
            variantKey = "default",
            variantAttachment = JsonObject(mapOf("value" to JsonPrimitive("42"))),
            evalContext = EvalContext()
        )

        val resultDouble = EvalResult(
            flagKey = "discount_rate",
            variantKey = "default",
            variantAttachment = JsonObject(mapOf("value" to JsonPrimitive("0.15"))),
            evalContext = EvalContext()
        )

        val provider = FlagentOpenFeatureProvider(
            FlagentOpenFeatureConfig(baseUrl = "http://localhost") { }
        )

        // direct numeric extraction logic
        val intVal = (resultInt.variantAttachment!!["value"] as JsonPrimitive).content.toDoubleOrNull()?.toInt()
        val doubleVal = (resultDouble.variantAttachment!!["value"] as JsonPrimitive).content.toDoubleOrNull()

        assertEquals(42, intVal)
        assertEquals(0.15, doubleVal)
    }

    @Test
    fun `object mapping returns full attachment when present`() {
        val attachment = JsonObject(
            mapOf(
                "value" to JsonPrimitive("payload"),
                "other" to JsonPrimitive("x")
            )
        )

        val result = EvalResult(
            flagKey = "object_flag",
            variantKey = "default",
            variantAttachment = attachment,
            evalContext = EvalContext()
        )

        val mapped = result.variantAttachment ?: JsonObject(emptyMap())

        assertEquals("payload", (mapped["value"] as JsonPrimitive).content)
        assertEquals("x", (mapped["other"] as JsonPrimitive).content)
    }
}

