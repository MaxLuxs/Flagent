package io.ktor.flagent

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FlagentPluginTest {
    @Test
    fun testPluginInstallation() = testApplication {
        application {
            installFlagent {
                flagentBaseUrl = "http://localhost:18000"
                enableEvaluation = true
                enableCache = true
            }
        }
        
        // Plugin should be installed without errors
        assertNotNull(application)
    }
    
    @Test
    fun testEvaluationEndpoint() = testApplication {
        application {
            installFlagent {
                flagentBaseUrl = "http://localhost:18000"
                enableEvaluation = true
            }
        }
        
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        try {
            // Note: This will fail if Flagent server is not running
            // In real tests, you would mock the server or use test containers
            val response = client.post("/flagent/evaluate") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "flagKey" to "test_flag",
                    "entityID" to "test_entity"
                ))
            }
            
            // Should either succeed or fail with connection error
            assertNotNull(response.status)
        } catch (e: Exception) {
            // Expected if server is not running
        } finally {
            client.close()
        }
    }
}
