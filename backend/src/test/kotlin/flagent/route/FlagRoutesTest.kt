package flagent.route

import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.service.FlagService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class FlagRoutesTest {
    @Test
    fun testGetFlags() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.get("/api/v1/flags")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testCreateFlag() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetFlagById() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
}
