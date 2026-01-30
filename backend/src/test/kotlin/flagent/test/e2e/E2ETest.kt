package flagent.test.e2e

import flagent.application.module
import flagent.repository.Database
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.ktor.client.*
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*

/**
 * E2E tests for full flow
 * Tests complete workflow: create flag -> segment -> constraint -> variant -> distribution -> evaluation
 */
class E2ETest {

    private suspend fun HttpResponse.bodyJsonObject() = Json.parseToJsonElement(bodyAsText()).jsonObject
    private suspend fun HttpResponse.bodyJsonArray() = Json.parseToJsonElement(bodyAsText()).jsonArray

    private suspend fun parseFlagId(response: HttpResponse): Int {
        assertEquals(HttpStatusCode.OK, response.status, "Create flag failed: ${response.status}")
        val obj = response.bodyJsonObject()
        return obj["id"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Create flag response missing 'id'. Keys: ${obj.keys}")
    }

    private suspend fun parseSegmentId(response: HttpResponse): Int {
        assertEquals(HttpStatusCode.OK, response.status)
        val obj = response.bodyJsonObject()
        return obj["id"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Create segment response missing 'id'. Keys: ${obj.keys}")
    }

    private suspend fun parseConstraintId(response: HttpResponse): Int {
        assertEquals(HttpStatusCode.OK, response.status)
        val obj = response.bodyJsonObject()
        return obj["id"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Create constraint response missing 'id'. Keys: ${obj.keys}")
    }

    private suspend fun parseVariantId(response: HttpResponse): Int {
        assertEquals(HttpStatusCode.OK, response.status)
        val obj = response.bodyJsonObject()
        return obj["id"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Create variant response missing 'id'. Keys: ${obj.keys}")
    }

    private suspend fun parseTagId(response: HttpResponse): Int {
        assertEquals(HttpStatusCode.OK, response.status)
        val obj = response.bodyJsonObject()
        return obj["id"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Create tag response missing 'id'. Keys: ${obj.keys}")
    }

    @AfterTest
    fun afterTest() {
        Database.close()
    }

    /**
     * When enterprise is present, TenantContextMiddleware requires X-API-Key.
     * Try to create a tenant via /admin/tenants (skipped by middleware) and use its API key for all requests.
     */
    private suspend fun ApplicationTestBuilder.createE2EClient(): HttpClient {
        val baseClient = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiKey = try {
            val r = baseClient.post("/admin/tenants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("key", "e2e-tenant-${System.currentTimeMillis()}")
                    put("name", "E2E Test Tenant")
                    put("plan", "STARTER")
                    put("ownerEmail", "e2e@test.com")
                }.toString())
            }
            if (r.status != HttpStatusCode.Created) null
            else {
                val obj = Json.parseToJsonElement(r.bodyAsText()).jsonObject
                obj["apiKey"]?.jsonPrimitive?.content
            }
        } catch (_: Exception) {
            null
        }
        return if (apiKey != null) {
            createClient {
                install(DefaultRequest) {
                    header("X-API-Key", apiKey)
                }
                // No ContentNegotiation so setBody(jsonString) sends raw JSON (avoids Map mixed-type serialization)
            }
        } else baseClient
    }

    @Test
    fun testFullFlow() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // 1. Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Full flow test flag")
                    put("key", "full_flow_flag")
                }.toString())
            }
            
            val flagId = parseFlagId(createFlagResponse)
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }
            
            // 2. Create segment
            val createSegmentResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }
            
            val segmentId = parseSegmentId(createSegmentResponse)
            
            // 3. Create constraint
            val createConstraintResponse = client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("property", "region")
                    put("operator", "EQ")
                    put("value", "US")
                }.toString())
            }
            
            val constraintId = parseConstraintId(createConstraintResponse)
            
            // 4. Create variant
            val createVariantResponse = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "variant_a") }.toString())
            }
            
            val variantId = parseVariantId(createVariantResponse)
            
            // 5. Create distribution
            val createDistributionResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variantId)
                            put("variantKey", "variant_a")
                            put("percent", 100)
                        })
                    })
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, createDistributionResponse.status)
            
            // Wait a bit for cache refresh (if needed)
            delay(100)
            
            // 6. Evaluate flag
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "test_entity_123")
                    put("entityType", "user")
                    put("entityContext", buildJsonObject { put("region", "US") })
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val evalResult = evalResponse.bodyJsonObject()
            val resultFlagId = evalResult["flagID"]?.jsonPrimitive?.content?.toIntOrNull()
            assertEquals(flagId, resultFlagId)
            assertNotNull(evalResult["variantID"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testCreateFlagAndEvaluate() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "E2E test flag") }.toString())
            }
            
            val flagId = parseFlagId(createResponse)
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }
            
            // Evaluate flag
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "test_entity")
                    put("entityType", "user")
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val evalResult = evalResponse.bodyJsonObject()
            assertNotNull(evalResult["flagID"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testFlagCRUD() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "CRUD test") }.toString())
            }
            
            val flagId = parseFlagId(createResponse)
            
            // Read
            val getResponse = client.get("/api/v1/flags/$flagId")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val getFlag = getResponse.bodyJsonObject()
            assertEquals(flagId, getFlag["id"]?.jsonPrimitive?.content?.toIntOrNull())
            
            // Update
            val updateResponse = client.put("/api/v1/flags/$flagId") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Updated description") }.toString())
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)
            
            // Delete
            val deleteResponse = client.delete("/api/v1/flags/$flagId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
            // Verify deletion
            val getAfterDeleteResponse = client.get("/api/v1/flags/$flagId")
            assertEquals(HttpStatusCode.NotFound, getAfterDeleteResponse.status)
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testSegmentCRUD() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Segment CRUD test flag") }.toString())
            }
            val flagId = parseFlagId(createFlagResponse)
            
            // Create segment
            val createResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 50)
                }.toString())
            }
            
            val segmentId = parseSegmentId(createResponse)
            
            // Read segments
            val getResponse = client.get("/api/v1/flags/$flagId/segments")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val segments = getResponse.bodyJsonArray()
            assertTrue(segments.isNotEmpty())
            
            // Update segment
            val updateResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Updated segment")
                    put("rolloutPercent", 75)
                }.toString())
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)
            
            // Delete segment
            val deleteResponse = client.delete("/api/v1/flags/$flagId/segments/$segmentId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testVariantCRUD() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Variant CRUD test flag") }.toString())
            }
            val flagId = parseFlagId(createFlagResponse)
            
            // Create variant
            val createResponse = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "test_variant") }.toString())
            }
            
            val variantId = parseVariantId(createResponse)
            
            // Read variants
            val getResponse = client.get("/api/v1/flags/$flagId/variants")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val variants = getResponse.bodyJsonArray()
            assertTrue(variants.isNotEmpty())
            
            // Update variant
            val updateResponse = client.put("/api/v1/flags/$flagId/variants/$variantId") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "updated_variant") }.toString())
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)
            
            // Delete variant
            val deleteResponse = client.delete("/api/v1/flags/$flagId/variants/$variantId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testConstraintCRUD() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Constraint CRUD test flag") }.toString())
            }
            val flagId = parseFlagId(createFlagResponse)
            
            // Create segment
            val createSegmentResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }
            val segmentId = parseSegmentId(createSegmentResponse)
            
            // Create constraint
            val createResponse = client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("property", "age")
                    put("operator", "GT")
                    put("value", "18")
                }.toString())
            }
            
            val constraintId = parseConstraintId(createResponse)
            
            // Read constraints
            val getResponse = client.get("/api/v1/flags/$flagId/segments/$segmentId/constraints")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val constraints = getResponse.bodyJsonArray()
            assertTrue(constraints.isNotEmpty())
            
            // Update constraint
            val updateResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId/constraints/$constraintId") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("property", "age")
                    put("operator", "GTE")
                    put("value", "21")
                }.toString())
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)
            
            // Delete constraint
            val deleteResponse = client.delete("/api/v1/flags/$flagId/segments/$segmentId/constraints/$constraintId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluationBatch() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Batch evaluation test flag") }.toString())
            }
            val flagId = parseFlagId(createFlagResponse)
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }
            
            // Batch evaluation
            val batchResponse = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("entities", buildJsonArray {
                        add(buildJsonObject {
                            put("entityID", "entity1")
                            put("entityType", "user")
                        })
                        add(buildJsonObject {
                            put("entityID", "entity2")
                            put("entityType", "user")
                        })
                    })
                    put("flagIDs", buildJsonArray { add(JsonPrimitive(flagId)) })
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val batchResult = batchResponse.bodyJsonObject()
            val results = batchResult["evaluationResults"]?.jsonArray ?: error("Batch response missing evaluationResults")
            assertTrue(results.isNotEmpty())
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testTagOperations() = testApplication {
        application {
            module()
        }
        val client = createE2EClient()
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("description", "Tag test flag") }.toString())
            }
            val flagId = parseFlagId(createFlagResponse)
            
            // Create tag
            val createTagResponse = client.post("/api/v1/flags/$flagId/tags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("value", "test_tag") }.toString())
            }
            
            val tagId = parseTagId(createTagResponse)
            
            // Read tags for flag
            val getTagsResponse = client.get("/api/v1/flags/$flagId/tags")
            assertEquals(HttpStatusCode.OK, getTagsResponse.status)
            val tags = getTagsResponse.bodyJsonArray()
            assertTrue(tags.isNotEmpty())
            
            // Delete tag
            val deleteResponse = client.delete("/api/v1/flags/$flagId/tags/$tagId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
        } finally {
            client.close()
        }
    }
}
