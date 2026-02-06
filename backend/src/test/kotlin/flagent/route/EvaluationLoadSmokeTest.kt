package flagent.route

import flagent.application.module
import flagent.test.bodyJsonObject
import flagent.test.createAuthenticatedClient
import flagent.test.intOrNull
import flagent.test.requireSuccess
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.system.measureTimeMillis
import kotlin.test.*

/**
 * Smoke test for evaluation under repeated load.
 * Validates that POST /api/v1/evaluation handles many requests successfully.
 * Full load testing is done via k6 (evaluation-load-test.js).
 */
class EvaluationLoadSmokeTest {

    @Test
    fun testEvaluation_HandlesRepeatedRequests() = testApplication {
        application {
            module()
        }

        val client = createAuthenticatedClient()

        try {
            val flagKey = "load_smoke_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Load smoke test flag")
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

            val iterations = 100
            var failures = 0
            val totalTime = measureTimeMillis {
                repeat(iterations) { i ->
                    val response = client.post("/api/v1/evaluation") {
                        contentType(ContentType.Application.Json)
                        setBody(buildJsonObject {
                            put("flagID", flagId)
                            put("entityID", "user_$i")
                            put("entityType", "user")
                            put("entityContext", buildJsonObject { put("country", "US") })
                            put("enableDebug", false)
                        }.toString())
                    }
                    if (response.status != HttpStatusCode.OK) failures++
                }
            }

            assertEquals(0, failures, "All $iterations evaluation requests should succeed")
            val avgMs = totalTime.toDouble() / iterations
            assertTrue(avgMs < 200.0, "Avg latency should be < 200ms, got ${avgMs}ms")
            val throughput = iterations * 1000.0 / totalTime
            assertTrue(throughput >= 10.0, "Throughput should be >= 10 req/s, got $throughput")
        } finally {
            client.close()
        }
    }
}
