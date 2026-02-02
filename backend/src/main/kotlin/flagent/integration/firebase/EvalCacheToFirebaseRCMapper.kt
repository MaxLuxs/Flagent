package flagent.integration.firebase

import flagent.cache.impl.EvalCacheFlagExport
import flagent.cache.impl.EvalCacheJSON
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Maps EvalCacheJSON to Firebase Remote Config template parameters.
 * One flag -> one parameter. Value: "true"/"false" for bool, or JSON for experiments.
 */
object EvalCacheToFirebaseRCMapper {

    const val FIREBASE_RC_MAX_PARAMETERS = 2000

    /**
     * Build Firebase RC template JSON (parameters only, no conditions in v1).
     * Returns JSON string suitable for PUT.
     */
    fun map(
        evalCache: EvalCacheJSON,
        parameterPrefix: String = ""
    ): String {
        val parameters = buildJsonObject {
            evalCache.flags.take(FIREBASE_RC_MAX_PARAMETERS).forEach { flag ->
                val paramKey = parameterPrefix + flag.key
                val defaultValue = mapFlagToDefaultValue(flag)
                putJsonObject(paramKey) {
                    putJsonObject("defaultValue") {
                        put("value", defaultValue)
                    }
                }
            }
        }
        return buildJsonObject {
            put("parameters", parameters)
        }.toString()
    }

    private fun mapFlagToDefaultValue(flag: EvalCacheFlagExport): String {
        return when {
            !flag.enabled -> "false"
            flag.variants.isEmpty() -> "true"
            else -> {
                val firstSegment = flag.segments.minByOrNull { it.rank } ?: return "true"
                val firstDist = firstSegment.distributions.maxByOrNull { it.percent }
                val variant = firstDist?.let { d ->
                    flag.variants.find { it.id == d.variantId }
                }
                val variantKey = variant?.key ?: "control"
                val attachment = variant?.attachment ?: emptyMap()
                val attachmentJson = if (attachment.isEmpty()) ""
                else attachment.entries.joinToString(",") { (k, v) -> "\"$k\":\"${v.replace("\"", "\\\"")}\"" }
                if (attachmentJson.isEmpty()) "{\"variant\":\"$variantKey\"}"
                else "{\"variant\":\"$variantKey\",\"attachment\":{$attachmentJson}}"
            }
        }
    }
}
