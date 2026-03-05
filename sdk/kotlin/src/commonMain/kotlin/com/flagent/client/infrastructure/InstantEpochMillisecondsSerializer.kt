package com.flagent.client.infrastructure

import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serializes kotlin.time.Instant as epoch milliseconds (Long).
 * Deserializes from Long (epoch ms) or ISO-8601 string (backend may use either).
 *
 * Not registered in ApiClient.JSON_DEFAULT (on JVM there was an initialization error).
 * If backend returns dates as numbers (epoch ms), create your own [Json] with serializersModule
 * and pass it into the API constructor (e.g. FlagApi(jsonSerializer = myJson)).
 */
object InstantEpochMillisecondsSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.flagent.client.InstantEpochMs", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant {
        if (decoder is JsonDecoder) {
            val el = decoder.decodeJsonElement()
            return when (el) {
                is JsonPrimitive -> el.content.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
                    ?: Instant.parse(el.content)
                else -> throw IllegalArgumentException("Expected number or string for Instant, got $el")
            }
        }
        return Instant.fromEpochMilliseconds(decoder.decodeLong())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }
}
