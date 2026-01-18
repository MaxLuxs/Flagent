package flagent.route

import flagent.application.module
import flagent.repository.Database
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Integration tests for Evaluation routes
 * Tests evaluation API with complete flag setup
 */
class EvaluationRoutesIntegrationTest {
    
    @BeforeTest
    fun setup() {
        Database.init()
    }
    
    @AfterTest
    fun cleanup() {
        Database.close()
    }
    
    @Test
    fun testEvaluation_CompleteFlow() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            // 1. Create flag
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Evaluation test flag",
                    "key" to "eval_test_flag"
                ))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            // 2. Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            // 3. Create segment
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Test segment",
                    "rolloutPercent" to 100
                ))
            }.body<Map<String, Any>>()
            
            val segmentId = (segment["id"] as Number).toInt()
            
            // 4. Create variant
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "variant_a"))
            }.body<Map<String, Any>>()
            
            val variantId = (variant["id"] as Number).toInt()
            
            // 5. Create distribution
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
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
            
            // Wait for cache refresh
            delay(200)
            
            // 6. Evaluate flag by ID
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity_123",
                    "entityType" to "user"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertEquals(flagId, (result["flagID"] as Number).toInt())
            assertNotNull(result["variantID"])
            assertEquals(variantId, (result["variantID"] as Number).toInt())
            assertEquals("variant_a", result["variantKey"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_ByFlagKey() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            // Create flag with known key
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Evaluation by key test",
                    "key" to "eval_by_key_flag"
                ))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            // Setup flag (enable, segment, variant, distribution)
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rolloutPercent" to 100))
            }.body<Map<String, Any>>()
            
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "test_variant"))
            }.body<Map<String, Any>>()
            
            client.put("/api/v1/flags/$flagId/segments/${(segment["id"] as Number).toInt()}/distributions") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "distributions" to listOf(
                        mapOf(
                            "variantID" to (variant["id"] as Number).toInt(),
                            "percent" to 100
                        )
                    )
                ))
            }
            
            delay(200)
            
            // Evaluate by key
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagKey" to "eval_by_key_flag",
                    "entityID" to "test_entity"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertEquals(flagId, (result["flagID"] as Number).toInt())
            assertEquals("eval_by_key_flag", result["flagKey"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_WithEntityContext() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            // Create flag with constraint
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Context test flag",
                    "key" to "context_test_flag"
                ))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rolloutPercent" to 100))
            }.body<Map<String, Any>>()
            
            val segmentId = (segment["id"] as Number).toInt()
            
            // Add constraint
            client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "property" to "region",
                    "operator" to "EQ",
                    "value" to "US"
                ))
            }
            
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "us_variant"))
            }.body<Map<String, Any>>()
            
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "distributions" to listOf(
                        mapOf(
                            "variantID" to (variant["id"] as Number).toInt(),
                            "percent" to 100
                        )
                    )
                ))
            }
            
            delay(200)
            
            // Evaluate with matching context
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity",
                    "entityContext" to mapOf("region" to "US")
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertEquals(flagId, (result["flagID"] as Number).toInt())
            assertNotNull(result["variantID"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_DisabledFlag() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "description" to "Disabled flag",
                    "key" to "disabled_eval_flag"
                ))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            // Don't enable flag
            
            delay(200)
            
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertNull(result["variantID"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluationBatch_ByFlagIDs() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            // Create flag
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "batch_test_flag"))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            // Setup flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rolloutPercent" to 100))
            }.body<Map<String, Any>>()
            
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "batch_variant"))
            }.body<Map<String, Any>>()
            
            client.put("/api/v1/flags/$flagId/segments/${(segment["id"] as Number).toInt()}/distributions") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "distributions" to listOf(
                        mapOf(
                            "variantID" to (variant["id"] as Number).toInt(),
                            "percent" to 100
                        )
                    )
                ))
            }
            
            delay(200)
            
            // Batch evaluate
            val batchResponse = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagIDs" to listOf(flagId),
                    "entities" to listOf(
                        mapOf("entityID" to "entity1"),
                        mapOf("entityID" to "entity2")
                    )
                ))
            }
            
            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val batchResult = batchResponse.body<Map<String, Any>>()
            @Suppress("UNCHECKED_CAST")
            val results = batchResult["evaluationResults"] as? List<Map<String, Any>> ?: emptyList()
            assertEquals(2, results.size) // 2 entities * 1 flag = 2 results
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_NotFound() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to 99999,
                    "entityID" to "test_entity"
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertNull(result["variantID"])
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_WithDebugEnabled() = testApplication {
        application {
            module()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        try {
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "debug_test_flag"))
            }.body<Map<String, Any>>()
            
            val flagId = (flag["id"] as Number).toInt()
            
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("enabled" to true))
            }
            
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rolloutPercent" to 100))
            }.body<Map<String, Any>>()
            
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("key" to "debug_variant"))
            }.body<Map<String, Any>>()
            
            client.put("/api/v1/flags/$flagId/segments/${(segment["id"] as Number).toInt()}/distributions") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "distributions" to listOf(
                        mapOf(
                            "variantID" to (variant["id"] as Number).toInt(),
                            "percent" to 100
                        )
                    )
                ))
            }
            
            delay(200)
            
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagID" to flagId,
                    "entityID" to "test_entity",
                    "enableDebug" to true
                ))
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.body<Map<String, Any>>()
            assertNotNull(result["evalDebugLog"])
            
        } finally {
            client.close()
        }
    }
}
