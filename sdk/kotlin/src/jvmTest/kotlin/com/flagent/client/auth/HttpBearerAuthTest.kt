package com.flagent.client.auth

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class HttpBearerAuthTest : ShouldSpec({
    should("apply sets Authorization header with Bearer scheme") {
        val auth = HttpBearerAuth("bearer")
        auth.bearerToken = "token123"
        val headers = mutableMapOf<String, String>()
        val query = mutableMapOf<String, List<String>>()
        auth.apply(query, headers)
        headers["Authorization"] shouldBe "Bearer token123"
        query shouldBe emptyMap()
    }

    should("apply with null scheme uses raw scheme") {
        val auth = HttpBearerAuth("Custom")
        auth.bearerToken = "t"
        val headers = mutableMapOf<String, String>()
        auth.apply(query = mutableMapOf(), headers = headers)
        headers["Authorization"] shouldBe "Custom t"
    }

    should("apply does nothing when bearerToken is null") {
        val auth = HttpBearerAuth("bearer")
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers shouldBe emptyMap()
    }
})
