package com.flagent.client.infrastructure

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class ApiAbstractionsTest : ShouldSpec({

    should("collectionDelimiter csv") {
        collectionDelimiter("csv") shouldBe ","
    }
    should("collectionDelimiter tsv") {
        collectionDelimiter("tsv") shouldBe "\t"
    }
    should("collectionDelimiter pipe") {
        collectionDelimiter("pipe") shouldBe "|"
    }
    should("collectionDelimiter space") {
        collectionDelimiter("space") shouldBe " "
    }
    should("collectionDelimiter other returns empty") {
        collectionDelimiter("other") shouldBe ""
    }

    should("toMultiValue multi format returns list of items") {
        toMultiValue(arrayOf("a", "b"), "multi") shouldContainExactly listOf("a", "b")
    }
    should("toMultiValue csv joins with comma") {
        toMultiValue(arrayOf("x", "y"), "csv") shouldContainExactly listOf("x,y")
    }
    should("toMultiValue tsv joins with tab") {
        toMultiValue(arrayOf("p", "q"), "tsv") shouldContainExactly listOf("p\tq")
    }
    should("toMultiValue pipe joins with pipe") {
        toMultiValue(arrayOf("m", "n"), "pipe") shouldContainExactly listOf("m|n")
    }
    should("toMultiValue space joins with space") {
        toMultiValue(arrayOf("1", "2"), "space") shouldContainExactly listOf("1 2")
    }
    should("toMultiValue unknown format joins with empty separator") {
        toMultiValue(arrayOf("a", "b"), "unknown") shouldContainExactly listOf("ab")
    }
    should("toMultiValue with iterable") {
        toMultiValue(listOf(1, 2, 3), "csv") shouldContainExactly listOf("1,2,3")
    }
    should("toMultiValue with custom map") {
        toMultiValue(arrayOf(1, 2), "csv") { it.times(10).toString() } shouldContainExactly listOf("10,20")
    }
})
