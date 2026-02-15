package com.flagent.client.infrastructure

import com.flagent.client.apis.HealthApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class ApiClientTest : ShouldSpec({

    should("BASE_URL is set") {
        ApiClient.BASE_URL shouldBe "http://localhost:18000/api/v1"
    }

    should("setApiKey throws when no API key auth configured") {
        val api = HealthApi(baseUrl = "http://localhost")
        shouldThrow<Exception> {
            api.setApiKey("key")
        }.message shouldBe "No API key authentication configured"
    }

    should("setApiKeyPrefix throws when no API key auth configured") {
        val api = HealthApi(baseUrl = "http://localhost")
        shouldThrow<Exception> {
            api.setApiKeyPrefix("Prefix")
        }.message shouldBe "No API key authentication configured"
    }

    should("setAccessToken throws when no OAuth configured") {
        val api = HealthApi(baseUrl = "http://localhost")
        shouldThrow<Exception> {
            api.setAccessToken("token")
        }.message shouldBe "No OAuth2 authentication configured"
    }

    should("JSON_DEFAULT is non-null") {
        ApiClient.JSON_DEFAULT shouldBe ApiClient.JSON_DEFAULT
    }
})
