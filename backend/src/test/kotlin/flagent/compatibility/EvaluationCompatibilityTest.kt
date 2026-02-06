package flagent.compatibility

import flagent.application.module
import flagent.test.bodyJsonObject
import flagent.test.createAuthenticatedClient
import flagent.test.intOrNull
import flagent.test.requireSuccess
import flagent.test.stringOrNull
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Evaluation API contract tests.
 * Verifies that Flagent returns evaluation responses in the industry-standard format.
 * Excluded from default CI via excludeTags("compatibility"); run with
 * ./gradlew :backend:test -PincludeIntegrationTests -PincludeCompatibilityTests
 */
@Tag("compatibility")
class EvaluationCompatibilityTest {

    @Test
    fun `evaluation response contains required fields`() = testApplication {
        application {
            module()
        }

        val client = createAuthenticatedClient()
        try {
            val flagKey = "contract_test_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Contract test flag")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val flagId = flag.intOrNull("id") ?: error("Missing id")
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }

            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "control") }.toString())
            }.requireSuccess().bodyJsonObject()

            val segmentId = segment.intOrNull("id") ?: error("Missing segment id")
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variant.intOrNull("id")!!)
                            put("percent", 100)
                        })
                    })
                }.toString())
            }

            delay(350)

            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "user_123")
                    put("entityType", "user")
                    put("entityContext", buildJsonObject {
                        put("country", "US")
                        put("tier", "premium")
                    })
                }.toString())
            }

            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()

            assertTrue(result.containsKey("flagID"), "Response must contain flagID")
            assertTrue(result.containsKey("flagKey"), "Response must contain flagKey")
            assertTrue(result.containsKey("flagSnapshotID"), "Response must contain flagSnapshotID")
            assertTrue(result.containsKey("flagTags"), "Response must contain flagTags")
            assertTrue(result.containsKey("segmentID"), "Response must contain segmentID")
            assertTrue(result.containsKey("variantID"), "Response must contain variantID")
            assertTrue(result.containsKey("variantKey"), "Response must contain variantKey")
            assertTrue(result.containsKey("evalContext"), "Response must contain evalContext")
            assertTrue(result.containsKey("timestamp"), "Response must contain timestamp")

            assertEquals(flagId, result.intOrNull("flagID"))
            assertEquals(flagKey, result.stringOrNull("flagKey"))

            val evalContext = result["evalContext"]
            assertNotNull(evalContext)
            val ctxObj = evalContext as? kotlinx.serialization.json.JsonObject
            assertNotNull(ctxObj)
            assertTrue(ctxObj!!.containsKey("entityID"))
            assertTrue(ctxObj.containsKey("entityType"))
            assertTrue(ctxObj.containsKey("entityContext"))
            assertEquals("user_123", ctxObj["entityID"]?.toString()?.trim('"'))
        } finally {
            client.close()
        }
    }

    @Test
    fun `evaluation request accepts entityContext with various value types`() = testApplication {
        application {
            module()
        }

        val client = createAuthenticatedClient()
        try {
            val flagKey = "context_types_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Context types test")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val flagId = flag.intOrNull("id") ?: error("Missing id")
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }

            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "v1") }.toString())
            }.requireSuccess().bodyJsonObject()

            val segmentId = segment.intOrNull("id") ?: error("Missing segment id")
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variant.intOrNull("id")!!)
                            put("percent", 100)
                        })
                    })
                }.toString())
            }

            delay(350)

            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "entity_with_context")
                    put("entityType", "user")
                    put("entityContext", buildJsonObject {
                        put("country", "US")
                        put("age", 25)
                        put("tier", "premium")
                    })
                }.toString())
            }

            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertTrue(result.containsKey("evalContext"))
        } finally {
            client.close()
        }
    }

    @Test
    fun `evaluation batch response contains evaluationResults array`() = testApplication {
        application {
            module()
        }

        val client = createAuthenticatedClient()
        try {
            val flagKey = "batch_contract_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Batch contract test")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val flagId = flag.intOrNull("id") ?: error("Missing id")
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }

            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "batch_v") }.toString())
            }.requireSuccess().bodyJsonObject()

            val segmentId = segment.intOrNull("id") ?: error("Missing segment id")
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variant.intOrNull("id")!!)
                            put("percent", 100)
                        })
                    })
                }.toString())
            }

            delay(350)

            val batchResponse = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagIDs", buildJsonArray { add(flagId) })
                    put("entities", kotlinx.serialization.json.buildJsonArray {
                        add(buildJsonObject { put("entityID", "e1") })
                        add(buildJsonObject { put("entityID", "e2") })
                    })
                }.toString())
            }

            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val batchResult = batchResponse.bodyJsonObject()
            assertTrue(batchResult.containsKey("evaluationResults"))

            val results = batchResult["evaluationResults"]?.let { it as? kotlinx.serialization.json.JsonArray }
            assertNotNull(results)
            assertEquals(2, results!!.size)

            val firstResult = results[0] as? kotlinx.serialization.json.JsonObject
            assertNotNull(firstResult)
            assertTrue(firstResult!!.containsKey("flagID"))
            assertTrue(firstResult.containsKey("evalContext"))
        } finally {
            client.close()
        }
    }

    @Test
    fun `optional comparison with external API when FLAGENT_COMPATIBILITY_TEST_URL is set`() = testApplication {
        val externalUrl = System.getenv("FLAGENT_COMPATIBILITY_TEST_URL")
        assumeTrue(externalUrl != null && externalUrl.isNotBlank()) {
            "Skipped: FLAGENT_COMPATIBILITY_TEST_URL not set"
        }

        application {
            module()
        }

        val client = createAuthenticatedClient()
        try {
            val flagKey = "external_compare_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "External compare test")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val flagId = flag.intOrNull("id") ?: error("Missing id")
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }

            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }.requireSuccess().bodyJsonObject()

            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "control") }.toString())
            }.requireSuccess().bodyJsonObject()

            val segmentId = segment.intOrNull("id") ?: error("Missing segment id")
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variant.intOrNull("id")!!)
                            put("percent", 100)
                        })
                    })
                }.toString())
            }

            delay(350)

            val requestBody = buildJsonObject {
                put("flagID", flagId)
                put("entityID", "compare_entity")
                put("entityType", "user")
                put("entityContext", buildJsonObject { put("country", "US") })
            }.toString()

            val flagentResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            assertEquals(HttpStatusCode.OK, flagentResponse.status)
            val flagentResult = flagentResponse.bodyJsonObject()

            val externalClient = io.ktor.client.HttpClient(io.ktor.client.engine.cio.CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            try {
                val externalResponse = externalClient.post("$externalUrl/api/v1/evaluation") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                if (externalResponse.status == HttpStatusCode.OK) {
                    val externalResult = externalResponse.bodyAsText()
                    val externalJson = kotlinx.serialization.json.Json.parseToJsonElement(externalResult).jsonObject

                    assertTrue(externalJson.containsKey("flagID"))
                    assertTrue(externalJson.containsKey("evalContext"))
                    assertEquals(
                        flagentResult["entityID"] ?: flagentResult["evalContext"]?.let { (it as? kotlinx.serialization.json.JsonObject)?.get("entityID") },
                        externalJson["evalContext"]?.let { (it as? kotlinx.serialization.json.JsonObject)?.get("entityID") }
                    )
                }
            } finally {
                externalClient.close()
            }
        } finally {
            client.close()
        }
    }
}
