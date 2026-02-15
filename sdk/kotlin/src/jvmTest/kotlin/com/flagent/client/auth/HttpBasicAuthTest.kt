package com.flagent.client.auth

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.core.spec.style.ShouldSpec

class HttpBasicAuthTest : ShouldSpec({
    should("apply sets Authorization Basic header") {
        val auth = HttpBasicAuth()
        auth.username = "user"
        auth.password = "pass"
        val headers = mutableMapOf<String, String>()
        val query = mutableMapOf<String, List<String>>()
        auth.apply(query, headers)
        headers["Authorization"]!!.shouldStartWith("Basic ")
        query shouldBe emptyMap()
    }

    should("apply with null username and password does nothing") {
        val auth = HttpBasicAuth()
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers shouldBe emptyMap()
    }

    should("apply with only username") {
        val auth = HttpBasicAuth()
        auth.username = "u"
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers["Authorization"]!!.shouldStartWith("Basic ")
    }
})
