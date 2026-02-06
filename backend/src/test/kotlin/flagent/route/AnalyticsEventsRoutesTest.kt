package flagent.route

import flagent.repository.impl.AnalyticsEventRepository
import flagent.service.AnalyticsEventsService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsEventsRoutesTest {

    @Test
    fun `POST analytics events batch returns 200 and accepted count`() = testApplication {
        val repo = mockk<AnalyticsEventRepository>(relaxed = true)
        coEvery { repo.saveBatch(any(), any()) } returns Unit
        val service = AnalyticsEventsService(repo)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            routing {
                configureAnalyticsEventsRoutes(service)
            }
        }
        val now = System.currentTimeMillis()
        val response = client.post("/api/v1/analytics/events") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "events": [
                        {"eventName": "first_open", "platform": "android", "timestampMs": $now},
                        {"eventName": "screen_view", "eventParams": "{\"screen\":\"Home\"}", "timestampMs": ${now + 1}}
                    ]
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("accepted"))
        assertTrue(body.contains("2"))
    }

    @Test
    fun `POST analytics events empty list returns 400`() = testApplication {
        val repo = mockk<AnalyticsEventRepository>()
        val service = AnalyticsEventsService(repo)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            routing { configureAnalyticsEventsRoutes(service) }
        }
        val response = client.post("/api/v1/analytics/events") {
            contentType(ContentType.Application.Json)
            setBody("""{"events": []}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET analytics overview missing start returns 400`() = testApplication {
        val repo = mockk<AnalyticsEventRepository>()
        val service = AnalyticsEventsService(repo)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            routing { configureAnalyticsEventsRoutes(service) }
        }
        val response = client.get("/api/v1/analytics/overview?end=1000")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET analytics overview missing end returns 400`() = testApplication {
        val repo = mockk<AnalyticsEventRepository>()
        val service = AnalyticsEventsService(repo)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            routing { configureAnalyticsEventsRoutes(service) }
        }
        val response = client.get("/api/v1/analytics/overview?start=0")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
