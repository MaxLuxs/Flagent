package com.flagent.client.infrastructure

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.Json

class OctetByteArrayTest : ShouldSpec({

    should("serialize and deserialize round-trip") {
        val bytes = byteArrayOf(0x0a, 0x0b, 0x0c)
        val arr = OctetByteArray(bytes)
        val json = Json.encodeToString(OctetByteArray.serializer(), arr)
        val decoded = Json.decodeFromString(OctetByteArray.serializer(), json)
        decoded.value.contentEquals(bytes) shouldBe true
    }

    should("equals same content") {
        val a = OctetByteArray(byteArrayOf(1, 2))
        val b = OctetByteArray(byteArrayOf(1, 2))
        (a == b) shouldBe true
        a.hashCode() shouldBe b.hashCode()
    }

    should("equals different content") {
        val a = OctetByteArray(byteArrayOf(1, 2))
        val b = OctetByteArray(byteArrayOf(1, 2, 3))
        (a == b) shouldBe false
    }

    should("toString contains hex") {
        val arr = OctetByteArray(byteArrayOf(0x0a, 0x0b))
        arr.toString() shouldBe "OctetByteArray(0a0b)"
    }
})
