package flagent.service

import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IWebhookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
}
