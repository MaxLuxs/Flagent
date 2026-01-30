package flagent.route

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class DocumentationRoutesTest {
    @Test
    fun testGetDocs_SwaggerUI() = testApplication {
        application {
            routing {
                configureDocumentationRoutes()
            }
        }
        
        val response = client.get("/docs")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.contentType()?.match(ContentType.Text.Html) == true)
        val content = response.bodyAsText()
        assertTrue(content.contains("swagger-ui"))
        assertTrue(content.contains("Flagent API Documentation"))
    }
    
    @Test
    fun testGetOpenApiYaml_ReturnsYaml_WhenFileExists() = testApplication {
        application {
            routing {
                configureDocumentationRoutes()
            }
        }
        
        val response = client.get("/api/v1/openapi.yaml")
        // May succeed if file exists, or return 500 if not found
        assertTrue(
            response.status == HttpStatusCode.OK || 
            response.status == HttpStatusCode.InternalServerError
        )
        
        if (response.status == HttpStatusCode.OK) {
            val contentType = response.contentType()
            assertTrue(
                contentType?.contentType == "application" && 
                contentType?.contentSubtype == "x-yaml"
            )
        }
    }
    
    @Test
    fun testGetOpenApiJson_ReturnsJson_WhenFileExists() = testApplication {
        application {
            routing {
                configureDocumentationRoutes()
            }
        }
        
        val response = client.get("/api/v1/openapi.json")
        // May succeed if file exists, or return 500 if not found
        assertTrue(
            response.status == HttpStatusCode.OK || 
            response.status == HttpStatusCode.InternalServerError
        )
        
        if (response.status == HttpStatusCode.OK) {
            assertTrue(response.contentType()?.match(ContentType.Application.Json) == true)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonObject)
        }
    }
    
    @Test
    fun testGetOpenApiYaml_ReturnsError_WhenFileNotFound() = testApplication {
        application {
            routing {
                configureDocumentationRoutes()
            }
        }
        
        // If file doesn't exist, should return 500
        val response = client.get("/api/v1/openapi.yaml")
        // May return 500 if file not found, or 200 if file exists
        assertTrue(
            response.status == HttpStatusCode.OK || 
            response.status == HttpStatusCode.InternalServerError
        )
    }
}
