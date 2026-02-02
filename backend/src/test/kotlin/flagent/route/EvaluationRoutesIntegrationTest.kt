package flagent.route

import flagent.application.module
import flagent.test.bodyJsonArray
import flagent.test.bodyJsonObject
import flagent.test.createAuthenticatedClient
import flagent.test.intOrNull
import flagent.test.isNullOrJsonNull
import flagent.test.requireSuccess
import flagent.test.stringOrNull
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put
import kotlin.test.*

/**
 * Integration tests for Evaluation routes
 * Tests evaluation API with complete flag setup
 */
class EvaluationRoutesIntegrationTest {
    
    @Test
    fun testEvaluation_CompleteFlow() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            // 1. Create flag
            val flagKey = "eval_test_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Evaluation test flag")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val flagId = flag.intOrNull("id") ?: error("Missing id")
            
            // 2. Enable flag
            client.put("/api/v1/flags/$flagId/enabled") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("enabled", true) }.toString())
            }
            
            // 3. Create segment
            val segment = client.post("/api/v1/flags/$flagId/segments") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Test segment")
                    put("rolloutPercent", 100)
                }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val segmentId = segment.intOrNull("id") ?: error("Missing id")
            
            // 4. Create variant
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "variant_a") }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val variantId = variant.intOrNull("id") ?: error("Missing id")
            
            // 5. Create distribution
            client.put("/api/v1/flags/$flagId/segments/$segmentId/distributions") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("distributions", buildJsonArray {
                        add(buildJsonObject {
                            put("variantID", variantId)
                            put("percent", 100)
                        })
                    })
                }.toString())
            }
            
            // Wait for cache refresh (FLAGENT_EVALCACHE_REFRESHINTERVAL=100ms in integration tests)
            delay(350)
            
            // 6. Evaluate flag by ID (enableDebug to diagnose when variantID is null)
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "test_entity")
                    put("entityType", "user")
                    put("enableDebug", true)
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertEquals(flagId, result.intOrNull("flagID"))
            if (result["variantID"].isNullOrJsonNull()) {
                val debugLog = result["evalDebugLog"]
                val msg = (debugLog as? kotlinx.serialization.json.JsonObject)?.get("msg")?.toString()
                val segmentLogs = (debugLog as? kotlinx.serialization.json.JsonObject)?.get("segmentDebugLogs")
                fail("variantID is null; evalDebugLog.msg=$msg; segmentDebugLogs=$segmentLogs; result=$result")
            }
            assertFalse(result["variantID"].isNullOrJsonNull())
            assertEquals(variantId, result.intOrNull("variantID"))
            assertEquals("variant_a", result.stringOrNull("variantKey"))
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_ByFlagKey() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            // Create flag with known key
            val flagKey = "eval_by_key_flag_${System.currentTimeMillis()}"
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Evaluation by key test")
                    put("key", flagKey)
                }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val flagId = flag.intOrNull("id") ?: error("Missing id")
            
            // Setup flag (enable, segment, variant, distribution)
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
                setBody(buildJsonObject { put("key", "test_variant") }.toString())
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
            
            // Evaluate by key
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagKey", flagKey)
                    put("entityID", "test_entity")
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertEquals(flagId, result.intOrNull("flagID"))
            assertEquals(flagKey, result.stringOrNull("flagKey"))
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_WithEntityContext() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            // Create flag with constraint
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Context test flag")
                    put("key", "context_test_flag_${System.currentTimeMillis()}")
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
            
            val segmentId = segment.intOrNull("id") ?: error("Missing segment id")
            
            // Add constraint
            client.post("/api/v1/flags/$flagId/segments/$segmentId/constraints") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("property", "region")
                    put("operator", "EQ")
                    put("value", "US")
                }.toString())
            }
            
            val variant = client.post("/api/v1/flags/$flagId/variants") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("key", "us_variant") }.toString())
            }.requireSuccess().bodyJsonObject()
            
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
            
            // Evaluate with matching context
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "test_entity")
                    put("entityContext", buildJsonObject { put("region", "US") })
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertEquals(flagId, result.intOrNull("flagID"))
            assertFalse(result["variantID"].isNullOrJsonNull())
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_DisabledFlag() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Disabled flag")
                    put("key", "disabled_eval_flag_${System.currentTimeMillis()}")
                }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val flagId = flag.intOrNull("id") ?: error("Missing id")
            
            // Don't enable flag
            
            delay(350)
            
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", flagId)
                    put("entityID", "test_entity")
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertTrue(result["variantID"].isNullOrJsonNull())
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluationBatch_ByFlagIDs() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            // Create flag
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Batch test flag")
                    put("key", "batch_test_flag_${System.currentTimeMillis()}")
                }.toString())
            }.requireSuccess().bodyJsonObject()
            
            val flagId = flag.intOrNull("id") ?: error("Missing id")
            
            // Setup flag
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
                setBody(buildJsonObject { put("key", "batch_variant") }.toString())
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
            
            // Batch evaluate
            val batchResponse = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagIDs", buildJsonArray { add(JsonPrimitive(flagId)) })
                    put("entities", buildJsonArray {
                        add(buildJsonObject { put("entityID", "entity1") })
                        add(buildJsonObject { put("entityID", "entity2") })
                    })
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val batchResult = batchResponse.bodyJsonObject()
            val results = batchResult["evaluationResults"]?.jsonArray ?: buildJsonArray { }
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
        
        val client = createAuthenticatedClient()
        
        try {
            val evalResponse = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("flagID", 99999)
                    put("entityID", "test_entity")
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertTrue(result["variantID"].isNullOrJsonNull())
            
        } finally {
            client.close()
        }
    }
    
    @Test
    fun testEvaluation_WithDebugEnabled() = testApplication {
        application {
            module()
        }
        
        val client = createAuthenticatedClient()
        
        try {
            val flag = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", "Debug test flag")
                    put("key", "debug_test_flag_${System.currentTimeMillis()}")
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
                setBody(buildJsonObject { put("key", "debug_variant") }.toString())
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
                    put("entityID", "test_entity")
                    put("enableDebug", true)
                }.toString())
            }
            
            assertEquals(HttpStatusCode.OK, evalResponse.status)
            val result = evalResponse.bodyJsonObject()
            assertFalse(result["evalDebugLog"].isNullOrJsonNull())
            
        } finally {
            client.close()
        }
    }
}
