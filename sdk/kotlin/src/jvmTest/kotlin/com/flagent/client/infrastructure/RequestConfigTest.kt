package com.flagent.client.infrastructure

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class RequestConfigTest : ShouldSpec({
    should("create with defaults") {
        val config = RequestConfig<Unit>(
            method = RequestMethod.GET,
            path = "/flags",
            requiresAuthentication = false
        )
        config.method shouldBe RequestMethod.GET
        config.path shouldBe "/flags"
        config.headers shouldBe emptyMap()
        config.params shouldBe emptyMap()
        config.query shouldBe emptyMap()
        config.requiresAuthentication shouldBe false
        config.body shouldBe null
    }

    should("create with body") {
        val config = RequestConfig(
            method = RequestMethod.POST,
            path = "/evaluation",
            requiresAuthentication = true,
            body = "payload"
        )
        config.body shouldBe "payload"
    }
})
