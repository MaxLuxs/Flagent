package flagent.test.e2e

import flagent.application.module
import flagent.config.AppConfig
import flagent.repository.Database
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * E2E tests for full flow
 * Tests complete workflow: create flag -> segment -> constraint -> variant -> distribution -> evaluation
 */
class E2ETest {
    @Test
    fun testFullFlow() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // 1. Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Full flow test flag",
                    "key" to "full_flow_flag"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createFlagResponse.status)
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            assertNotNull(flagId)
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            // 2. Create segment
            val createSegmentResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Test segment",
                    "rolloutPercent" to 100
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createSegmentResponse.status)
            val segment = createSegmentResponse.body<Map<String, Any>>()
            val segmentId = (segment["id"] as Number).toInt()
            assertNotNull(segmentId)
            
            // 3. Create constraint
            val createConstraintResponse = client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "property" to "region",
                    "operator" to "EQ",
                    "value" to "US"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createConstraintResponse.status)
            val constraint = createConstraintResponse.body<Map<String, Any>>()
            val constraintId = (constraint["id"] as Number).toInt()
            assertNotNull(constraintId)
            
            // 4. Create variant
            val createVariantResponse = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "key" to "variant_a"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createVariantResponse.status)
            val variant = createVariantResponse.body<Map<String, Any>>()
            val variantId = (variant["id"] as Number).toInt()
            assertNotNull(variantId)
            
            // 5. Create distribution
            val createDistributionResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "distributions" to listOf(
                        mapOf(
                            "variantID" to variantId,
                            "percent" to 100
                        )
                    )
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createDistributionResponse.status)
            
            // Wait a bit for cache refresh (if needed)
            delay(100)
            
            // 6. Evaluate flag
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity_123",
                    "entityType" to "user",
                    "entityContext" to mapOf(
                        "region" to "US"
                    )
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val evalResult = evalResponse.body<Map<String, Any>>()
            assertEquals(flagId, (evalResult["flagID"] as Number).toInt())
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "E2E test flag"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val flag = createResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            assertNotNull(flagId)
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            // Evaluate flag
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity",
                    "entityType" to "user"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val evalResult = evalResponse.body<Map<String, Any>>()
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "CRUD test"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val flag = createResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Read
            val getResponse = client.get("/api/v1/flags/$flagId")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val getFlag = getResponse.body<Map<String, Any>>()
            assertEquals(flagId, (getFlag["id"] as Number).toInt())
            
            // Update
            val updateResponse = client.put("/api/v1/flags/$flagId") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Updated description"
                ))
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("description" to "Segment CRUD test flag"))
            }
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Create segment
            val createResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Test segment",
                    "rolloutPercent" to 50
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val segment = createResponse.body<Map<String, Any>>()
            val segmentId = (segment["id"] as Number).toInt()
            
            // Read segments
            val getResponse = client.get("/api/v1/flags/$flagId/segments")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val segments = getResponse.body<List<Map<String, Any>>>()
            assertTrue(segments.isNotEmpty())
            
            // Update segment
            val updateResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Updated segment",
                    "rolloutPercent" to 75
                ))
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("description" to "Variant CRUD test flag"))
            }
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Create variant
            val createResponse = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "test_variant"))
            }
            
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val variant = createResponse.body<Map<String, Any>>()
            val variantId = (variant["id"] as Number).toInt()
            
            // Read variants
            val getResponse = client.get("/api/v1/flags/$flagId/variants")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val variants = getResponse.body<List<Map<String, Any>>>()
            assertTrue(variants.isNotEmpty())
            
            // Update variant
            val updateResponse = client.put("/api/v1/flags/$flagId/variants/$variantId") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "updated_variant"))
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("description" to "Constraint CRUD test flag"))
            }
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Create segment
            val createSegmentResponse = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Test segment",
                    "rolloutPercent" to 100
                ))
            }
            val segment = createSegmentResponse.body<Map<String, Any>>()
            val segmentId = (segment["id"] as Number).toInt()
            
            // Create constraint
            val createResponse = client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "property" to "age",
                    "operator" to "GT",
                    "value" to "18"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val constraint = createResponse.body<Map<String, Any>>()
            val constraintId = (constraint["id"] as Number).toInt()
            
            // Read constraints
            val getResponse = client.get("/api/v1/flags/$flagId/segments/$segmentId/constraints")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val constraints = getResponse.body<List<Map<String, Any>>>()
            assertTrue(constraints.isNotEmpty())
            
            // Update constraint
            val updateResponse = client.put("/api/v1/flags/$flagId/segments/$segmentId/constraints/$constraintId") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "property" to "age",
                    "operator" to "GTE",
                    "value" to "21"
                ))
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("description" to "Batch evaluation test flag"))
            }
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            // Batch evaluation
            val batchResponse = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "entities" to listOf(
                        mapOf(
                            "entityID" to "entity1",
                            "entityType" to "user"
                        ),
                        mapOf(
                            "entityID" to "entity2",
                            "entityType" to "user"
                        )
                    ),
                    "flagIDs" to listOf(flagId)
                ))
            }
            
            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val batchResult = batchResponse.body<Map<String, Any>>()
            val results = batchResult["evaluationResults"] as List<*>
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
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Create flag
            val createFlagResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("description" to "Tag test flag"))
            }
            val flag = createFlagResponse.body<Map<String, Any>>()
            val flagId = (flag["id"] as Number).toInt()
            
            // Create tag
            val createTagResponse = client.post("/api/v1/flags/$flagId/tags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("value" to "test_tag"))
            }
            
            assertEquals(HttpStatusCode.OK, createTagResponse.status)
            val tag = createTagResponse.body<Map<String, Any>>()
            val tagId = (tag["id"] as Number).toInt()
            
            // Read tags for flag
            val getTagsResponse = client.get("/api/v1/flags/$flagId/tags")
            assertEquals(HttpStatusCode.OK, getTagsResponse.status)
            val tags = getTagsResponse.body<List<Map<String, Any>>>()
            assertTrue(tags.isNotEmpty())
            
            // Delete tag
            val deleteResponse = client.delete("/api/v1/flags/$flagId/tags/$tagId")
            assertEquals(HttpStatusCode.OK, deleteResponse.status)
            
        } finally {
            client.close()
        }
    }
}
