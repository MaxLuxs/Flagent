package flagent.middleware

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.Json

class RecoveryMiddlewareTest {
    @Test
    fun testRecoveryHandlesExceptions() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
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
