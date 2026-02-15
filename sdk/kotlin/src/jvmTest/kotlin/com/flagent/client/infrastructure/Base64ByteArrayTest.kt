package com.flagent.client.infrastructure

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.Json

class Base64ByteArrayTest : ShouldSpec({

    should("serialize and deserialize round-trip") {
        val bytes = byteArrayOf(1, 2, 3)
        val arr = Base64ByteArray(bytes)
        val json = Json.encodeToString(Base64ByteArray.serializer(), arr)
        val decoded = Json.decodeFromString(Base64ByteArray.serializer(), json)
        decoded.value shouldBe bytes
    }

    should("equals same content") {
        val a = Base64ByteArray(byteArrayOf(1, 2))
        val b = Base64ByteArray(byteArrayOf(1, 2))
        (a == b) shouldBe true
        a.hashCode() shouldBe b.hashCode()
    }

    should("equals different content") {
        val a = Base64ByteArray(byteArrayOf(1, 2))
        val b = Base64ByteArray(byteArrayOf(1, 2, 3))
        (a == b) shouldBe false
    }

    should("toString contains hex") {
        val arr = Base64ByteArray(byteArrayOf(0x0a, 0x0b))
        arr.toString() shouldBe "Base64ByteArray(0a0b)"
    }
})
