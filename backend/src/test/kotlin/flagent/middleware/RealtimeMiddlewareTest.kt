package flagent.middleware

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that configureSSE and configureRealtimeEventBus can be installed
 * and requests still pass (middleware doesn't break the pipeline).
 */
class RealtimeMiddlewareTest {

    @Test
    fun configureSSEInstalledRequestsPass() = testApplication {
        application {
            configureSSE()
            routing {
                get("/ping") {
                    call.respond("pong")
                }
            }
        }

        val response = client.get("/ping")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("pong", response.bodyAsText())
    }

    @Test
    fun configureRealtimeEventBusInstalledRequestsPass() = testApplication {
        application {
            configureRealtimeEventBus()
            routing {
                get("/ping") {
                    call.respond("pong")
                }
            }
        }

        val response = client.get("/ping")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("pong", response.bodyAsText())
    }

    @Test
    fun hasRealtimeEventBusReturnsTrueAfterConfigure() = testApplication {
        application {
            configureRealtimeEventBus()
            routing {
                get("/check") {
                    call.respond(if (hasRealtimeEventBus()) "ok" else "missing")
                }
            }
        }

        val response = client.get("/check")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }
}
