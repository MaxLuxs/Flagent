package flagent.middleware

import flagent.config.AppConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class LoggingMiddlewareTest {
    @Test
    fun testLoggingMiddlewareInstalled() = testApplication {
        application {
            configureLogging()
            routing {
                get("/test") {
                    call.respond("OK")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.OK, response.status)
        // Logging middleware should be installed if enabled
        // This test verifies the middleware doesn't break requests
    }
}
