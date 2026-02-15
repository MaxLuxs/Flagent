package com.flagent.client.auth

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class ApiKeyAuthTest : ShouldSpec({
    should("apply header when location is header") {
        val auth = ApiKeyAuth("header", "X-API-Key")
        auth.apiKey = "secret"
        val headers = mutableMapOf<String, String>()
        val query = mutableMapOf<String, List<String>>()
        auth.apply(query, headers)
        headers["X-API-Key"] shouldBe "secret"
        query shouldBe emptyMap()
    }

    should("apply with prefix") {
        val auth = ApiKeyAuth("header", "Authorization")
        auth.apiKey = "key"
        auth.apiKeyPrefix = "ApiKey"
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers["Authorization"] shouldBe "ApiKey key"
    }

    should("apply query when location is query") {
        val auth = ApiKeyAuth("query", "api_key")
        auth.apiKey = "qval"
        val query = mutableMapOf<String, List<String>>()
        val headers = mutableMapOf<String, String>()
        auth.apply(query, headers)
        query["api_key"] shouldBe listOf("qval")
        headers shouldBe emptyMap()
    }

    should("apply does nothing when apiKey is null") {
        val auth = ApiKeyAuth("header", "Key")
        val headers = mutableMapOf<String, String>()
        auth.apply(mutableMapOf(), headers)
        headers shouldBe emptyMap()
    }
})
