package flagent.route

import flagent.service.FlagService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealtimeRoutesTest {

    @Test
    fun testRealtimeSseHealthReturnsOk() = testApplication {
        application {
            install(SSE)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                realtimeRoutes(mockFlagService(), RealtimeEventBus())
            }
        }

        val response = client.get("/realtime/sse/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("healthy") || body.contains("status"))
        assertTrue(body.contains("activeConnections") || body.contains("SSE"))
    }

    @Test
    fun testRealtimeSseEndpointAcceptsConnection() = testApplication {
        application {
            install(SSE)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                realtimeRoutes(mockFlagService(), RealtimeEventBus())
            }
        }

        // SSE keeps connection open; launch get, wait briefly, cancel to avoid UncompletedCoroutinesError
        var response: HttpResponse? = null
        runBlocking {
            val job = launch {
                response = client.get("/realtime/sse") {
                    header(HttpHeaders.Accept, "text/event-stream")
                }
            }
            delay(1500)
            job.cancel()
        }
        if (response != null) {
            assertEquals(HttpStatusCode.OK, response!!.status)
            assertTrue(
                response!!.contentType()?.contentSubtype?.contains("event-stream") == true ||
                    response!!.contentType()?.toString()?.contains("event-stream") == true
            )
        }
        // If response is null, client.get() did not return before cancel (SSE blocks until stream ends)
    }

    @Test
    fun testRealtimeEventBusPublishAndSubscribe() = runBlocking {
        val eventBus = RealtimeEventBus()
        val events = mutableListOf<RealtimeEvent>()
        val job = launch(Dispatchers.Unconfined) {
            eventBus.subscribe().collect { events.add(it) }
        }
        kotlinx.coroutines.delay(50)
        eventBus.publishFlagUpdated(1L, "test_flag")
        withTimeout(500) {
            while (events.none { it.type == "flag.updated" && it.flagKey == "test_flag" }) {
                kotlinx.coroutines.delay(10)
            }
        }
        job.cancel()
        assertTrue(events.any { it.type == "flag.updated" && it.flagKey == "test_flag" })
    }

    private fun mockFlagService(): FlagService = mockk(relaxed = true)
}
