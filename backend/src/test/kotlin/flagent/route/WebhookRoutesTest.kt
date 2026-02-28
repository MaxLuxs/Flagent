package flagent.route

import flagent.api.constants.ApiConstants
import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.service.WebhookService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.*

class WebhookRoutesTest {

    private val basePath = "${ApiConstants.API_BASE_PATH}/webhooks"

    @Test
    fun `GET webhooks returns 200 and list`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        coEvery { webhookService.findAll() } returns listOf(
            Webhook(id = 1, url = "https://a.com", events = listOf(WebhookEvents.FLAG_CREATED))
        )
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.get(basePath)
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("https://a.com"))
    }

    @Test
    fun `POST webhooks with blank URL returns 400`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.post(basePath) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"","events":["flag.created"]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("URL is required"))
    }

    @Test
    fun `POST webhooks with invalid events returns 400`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.post(basePath) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"https://x.com","events":["invalid.event"]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("At least one valid event is required"))
    }

    @Test
    fun `POST webhooks with valid body returns 201`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        val created = Webhook(id = 1, url = "https://hook.com", events = listOf(WebhookEvents.FLAG_CREATED))
        coEvery { webhookService.create(any()) } returns created
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.post(basePath) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"https://hook.com","events":["flag.created"]}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("hook.com"))
    }

    @Test
    fun `GET webhooks by id invalid returns 400`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.get("$basePath/abc")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET webhooks by id not found returns 404`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        coEvery { webhookService.findById(999) } returns null
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.get("$basePath/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET webhooks by id found returns 200`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        val w = Webhook(id = 1, url = "https://x.com", events = listOf(WebhookEvents.FLAG_UPDATED))
        coEvery { webhookService.findById(1) } returns w
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.get("$basePath/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("x.com"))
    }

    @Test
    fun `PUT webhooks by id with blank URL returns 400`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        val existing = Webhook(id = 1, url = "https://old.com", events = listOf(WebhookEvents.FLAG_CREATED))
        coEvery { webhookService.findById(1) } returns existing
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.put("$basePath/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"","events":["flag.created"]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `DELETE webhooks by id invalid returns 400`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.delete("$basePath/notanum")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `DELETE webhooks by id not found returns 404`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        coEvery { webhookService.delete(999) } returns false
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.delete("$basePath/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE webhooks by id success returns 200`() = testApplication {
        val webhookService = mockk<WebhookService>(relaxed = true)
        coEvery { webhookService.delete(1) } returns true
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { configureWebhookRoutes(webhookService) }
        }
        val response = client.delete("$basePath/1")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
