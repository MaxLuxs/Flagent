package flagent.middleware

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class RecoveryMiddlewareTest {
    @Test
    fun testRecoveryHandlesExceptions() = testApplication {
        application {
            configureRecovery()
            routing {
                get("/test") {
                    throw RuntimeException("Unhandled exception")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
