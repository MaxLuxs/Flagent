package flagent.route

import flagent.repository.impl.FlagRepository
import flagent.service.FlagService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class FlagRoutesTest {
    @Test
    fun testGetFlags() = testApplication {
        val flagRepository = FlagRepository()
        val flagService = FlagService(flagRepository)
        
        application {
            routing {
                configureFlagRoutes(flagService)
            }
        }
        
        val response = client.get("/api/v1/flags")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
    }
    
    @Test
    fun testCreateFlag() = testApplication {
        val flagRepository = FlagRepository()
        val flagService = FlagService(flagRepository)
        
        application {
            routing {
                configureFlagRoutes(flagService)
            }
        }
        
        val response = client.post("/api/v1/flags") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Test flag"}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("id"))
        assertTrue(json.jsonObject.containsKey("key"))
    }
    
    @Test
    fun testGetFlagById() = testApplication {
        val flagRepository = FlagRepository()
        val flagService = FlagService(flagRepository)
        
        application {
            routing {
                configureFlagRoutes(flagService)
            }
        }
        
        // First create a flag
        val createResponse = client.post("/api/v1/flags") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Test flag"}""")
        }
        val createdFlag = Json.parseToJsonElement(createResponse.bodyAsText())
        val flagId = createdFlag.jsonObject["id"]!!.jsonPrimitive.int
        
        // Then get it
        val response = client.get("/api/v1/flags/$flagId")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertEquals(flagId, json.jsonObject["id"]!!.jsonPrimitive.int)
    }
}
