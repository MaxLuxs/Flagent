package com.flagent.client.infrastructure

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec
import kotlin.time.Instant
import kotlinx.serialization.json.Json

class InstantEpochMillisecondsSerializerTest : ShouldSpec({

    should("serialize to epoch milliseconds") {
        val instant = Instant.fromEpochMilliseconds(1704067200000L)
        val encoded = Json.encodeToString(InstantEpochMillisecondsSerializer, instant)
        encoded shouldBe "1704067200000"
    }

    should("deserialize from epoch milliseconds string") {
        val decoded = Json.decodeFromString(InstantEpochMillisecondsSerializer, "1704067200000")
        decoded shouldBe Instant.fromEpochMilliseconds(1704067200000L)
    }

    should("deserialize from ISO-8601 string in JSON") {
        val decoded = Json.decodeFromString(InstantEpochMillisecondsSerializer, "\"2024-01-01T00:00:00Z\"")
        decoded shouldBe Instant.parse("2024-01-01T00:00:00Z")
    }

    should("deserialize throws on non-primitive JSON") {
        val e = shouldThrow<IllegalArgumentException> {
            Json.decodeFromString(InstantEpochMillisecondsSerializer, "[]")
        }
        e.message!!.startsWith("Expected number or string for Instant") shouldBe true
    }
})
