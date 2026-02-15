package com.flagent.client.infrastructure

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.ShouldSpec

class PartConfigTest : ShouldSpec({

    should("create with defaults") {
        val config = PartConfig<Unit>()
        config.headers shouldBe emptyMap()
        config.body shouldBe null
    }

    should("create with headers and body") {
        val headers = mutableMapOf("Content-Type" to "application/octet-stream")
        val config = PartConfig(headers = headers, body = "data")
        config.headers shouldBe headers
        config.body shouldBe "data"
    }

    should("headers are mutable") {
        val config = PartConfig<String>()
        config.headers["X-Custom"] = "value"
        config.headers["X-Custom"] shouldBe "value"
    }
})
