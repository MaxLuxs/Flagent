package flagent.route

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class HealthRoutesTest {
    @Test
    fun testHealthCheck() = testApplication {
        application {
            routing {
                configureHealthRoutes()
            }
        }
        
        val response = client.get("/api/v1/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"OK"}""", response.bodyAsText())
    }
}
