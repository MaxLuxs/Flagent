package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebhookTest {

    @Test
    fun `Webhook subscribesTo returns true when event in list`() {
        val webhook = Webhook(
            id = 1,
            url = "https://example.com/hook",
            events = listOf(WebhookEvents.FLAG_CREATED, WebhookEvents.FLAG_UPDATED)
        )
        assertTrue(webhook.subscribesTo(WebhookEvents.FLAG_CREATED))
        assertTrue(webhook.subscribesTo(WebhookEvents.FLAG_UPDATED))
    }

    @Test
    fun `Webhook subscribesTo returns false when event not in list`() {
        val webhook = Webhook(
            id = 1,
            url = "https://example.com/hook",
            events = listOf(WebhookEvents.FLAG_CREATED)
        )
        assertFalse(webhook.subscribesTo(WebhookEvents.FLAG_DELETED))
        assertFalse(webhook.subscribesTo(WebhookEvents.ANOMALY_DETECTED))
    }

    @Test
    fun `WebhookEvents ALL contains all flag events and anomaly`() {
        assertEquals(6, WebhookEvents.ALL.size)
        assertTrue(WebhookEvents.ALL.contains(WebhookEvents.FLAG_CREATED))
        assertTrue(WebhookEvents.ALL.contains(WebhookEvents.ANOMALY_DETECTED))
    }

    @Test
    fun `WebhookEvents ALL_FLAG_EVENTS has five elements`() {
        assertEquals(5, WebhookEvents.ALL_FLAG_EVENTS.size)
    }
}
