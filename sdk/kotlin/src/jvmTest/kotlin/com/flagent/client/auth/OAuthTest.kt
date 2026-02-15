package com.flagent.client.auth

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class OAuthTest : ShouldSpec({
    should("apply sets Authorization Bearer header") {
        val auth = OAuth()
        auth.accessToken = "oauth-token"
        val headers = mutableMapOf<String, String>()
        val query = mutableMapOf<String, List<String>>()
        auth.apply(query, headers)
        headers["Authorization"] shouldBe "Bearer oauth-token"
        query shouldBe emptyMap()
    }

    should("apply does nothing when accessToken is null") {
        val auth = OAuth()
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers shouldBe emptyMap()
    }
})
