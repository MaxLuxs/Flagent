package flagent.service

import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IWebhookRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebhookServiceTest {

    @Test
    fun create_returnsCreatedWebhook() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        val w = Webhook(id = 0, url = "https://x.com", events = listOf(WebhookEvents.FLAG_CREATED))
        val created = w.copy(id = 1)
        coEvery { repo.create(any()) } returns created
        val service = WebhookService(repo)
        val result = service.create(w)
        assertEquals(1, result.id)
        coVerify(exactly = 1) { repo.create(any()) }
    }

    @Test
    fun findById_returnsWebhook() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        val w = Webhook(id = 1, url = "https://a.com", events = emptyList())
        coEvery { repo.findById(1, null) } returns w
        val service = WebhookService(repo)
        assertEquals(w, service.findById(1))
    }

    @Test
    fun findById_returnsNull() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        coEvery { repo.findById(999, null) } returns null
        val service = WebhookService(repo)
        assertNull(service.findById(999))
    }

    @Test
    fun findAll_returnsList() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        val list = listOf(Webhook(id = 1, url = "https://a.com", events = emptyList()))
        coEvery { repo.findAll(null) } returns list
        val service = WebhookService(repo)
        assertEquals(list, service.findAll())
    }

    @Test
    fun update_returnsUpdated() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        val w = Webhook(id = 1, url = "https://b.com", events = listOf(WebhookEvents.FLAG_UPDATED))
        coEvery { repo.update(any()) } returns w
        val service = WebhookService(repo)
        assertEquals(w, service.update(w))
    }

    @Test
    fun delete_returnsTrue() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        coEvery { repo.delete(1, null) } returns true
        val service = WebhookService(repo)
        assertTrue(service.delete(1))
    }

    @Test
    fun delete_returnsFalse() = runBlocking {
        val repo = mockk<IWebhookRepository>(relaxed = true)
        coEvery { repo.delete(999, null) } returns false
        val service = WebhookService(repo)
        assertTrue(!service.delete(999))
    }

    @Test
    fun dispatchSync_sendsPostWithHeaders_andNoSignatureWhenSecretMissing() = runTest {
        var capturedMethod: HttpMethod? = null
        var capturedUrl: String? = null
        var capturedEvent: String? = null
        var capturedSignature: String? = null

        val engine = MockEngine { request ->
            capturedMethod = request.method
            capturedUrl = request.url.toString()
            capturedEvent = request.headers["X-Flagent-Event"]
            capturedSignature = request.headers["X-Flagent-Signature"]
            respond(
                content = ByteReadChannel("""{"ok":true}"""),
                status = HttpStatusCode.OK
            )
        }
        val client = HttpClient(engine)

        val repo = mockk<IWebhookRepository>()
        coEvery { repo.findByEvent(WebhookEvents.FLAG_CREATED, null) } returns listOf(
            Webhook(id = 1, url = "https://example.com/hook", events = listOf(WebhookEvents.FLAG_CREATED))
        )

        val service = WebhookService(
            webhookRepository = repo,
            flagRepository = null,
            tenantId = null,
            httpClient = client
        )

        val payload = WebhookPayload(event = WebhookEvents.FLAG_CREATED, flagDeletedData = null, flagData = null)
        service.dispatchSync(WebhookEvents.FLAG_CREATED, payload)

        assertEquals(HttpMethod.Post, capturedMethod)
        assertEquals("https://example.com/hook", capturedUrl)
        assertEquals(WebhookEvents.FLAG_CREATED, capturedEvent)
        assertNull(capturedSignature)
    }

    @Test
    fun dispatchSync_includesHmacSignatureWhenSecretConfigured() = runTest {
        var capturedSignature: String? = null

        val engine = MockEngine { request ->
            capturedSignature = request.headers["X-Flagent-Signature"]
            respond(
                content = ByteReadChannel("""{"ok":true}"""),
                status = HttpStatusCode.OK
            )
        }
        val client = HttpClient(engine)

        val secret = "topsecret"
        val webhook = Webhook(
            id = 1,
            url = "https://example.com/hook",
            events = listOf(WebhookEvents.FLAG_UPDATED),
            secret = secret
        )

        val repo = mockk<IWebhookRepository>()
        coEvery { repo.findByEvent(WebhookEvents.FLAG_UPDATED, null) } returns listOf(webhook)

        val service = WebhookService(
            webhookRepository = repo,
            flagRepository = null,
            tenantId = null,
            httpClient = client
        )

        val payload = WebhookPayload(event = WebhookEvents.FLAG_UPDATED, flagDeletedData = null, flagData = null)
        service.dispatchSync(WebhookEvents.FLAG_UPDATED, payload)

        assertTrue(capturedSignature != null && capturedSignature!!.startsWith("sha256="))
    }

    @Test
    fun dispatchSync_retriesOnFailure_andStopsOnSuccess() = runTest {
        var callCount = 0

        val engine = MockEngine { _ ->
            callCount++
            if (callCount < 3) {
                throw RuntimeException("transient error")
            }
            respond(
                content = ByteReadChannel("""{"ok":true}"""),
                status = HttpStatusCode.OK
            )
        }
        val client = HttpClient(engine)

        val webhook = Webhook(
            id = 1,
            url = "https://example.com/hook",
            events = listOf(WebhookEvents.FLAG_DELETED)
        )

        val repo = mockk<IWebhookRepository>()
        coEvery { repo.findByEvent(WebhookEvents.FLAG_DELETED, null) } returns listOf(webhook)

        val service = WebhookService(
            webhookRepository = repo,
            flagRepository = null,
            tenantId = null,
            httpClient = client
        )

        val payload = WebhookPayload(
            event = WebhookEvents.FLAG_DELETED,
            flagDeletedData = WebhookFlagDeletedData(flagId = 1, flagKey = "flag_a"),
            flagData = null
        )

        service.dispatchSync(WebhookEvents.FLAG_DELETED, payload)

        assertEquals(3, callCount)
    }
}
