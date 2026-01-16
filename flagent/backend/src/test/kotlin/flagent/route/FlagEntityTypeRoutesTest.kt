package flagent.route

import flagent.repository.Database
import flagent.repository.impl.FlagEntityTypeRepository
import flagent.service.FlagEntityTypeService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class FlagEntityTypeRoutesTest {
    @Test
    fun testGetEntityTypes() = testApplication {
        Database.init()
        val flagEntityTypeRepository = FlagEntityTypeRepository()
        val flagEntityTypeService = FlagEntityTypeService(flagEntityTypeRepository)
        
        application {
            routing {
                configureFlagEntityTypeRoutes(flagEntityTypeService)
            }
        }
        
        val response = client.get("/api/v1/flags/entity_types")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
}
