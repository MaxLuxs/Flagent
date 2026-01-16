package flagent.route

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class InfoRoutesTest {
    @Test
    fun testInfo() = testApplication {
        application {
            routing {
                configureInfoRoutes()
            }
        }
        
        val response = client.get("/api/v1/info")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("version"))
    }
}
