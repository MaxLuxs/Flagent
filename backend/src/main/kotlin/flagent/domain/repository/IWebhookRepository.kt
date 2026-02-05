package flagent.domain.repository

import flagent.domain.entity.Webhook

/**
 * Webhook repository interface.
 */
interface IWebhookRepository {
    suspend fun create(webhook: Webhook): Webhook
    suspend fun findById(id: Int, tenantId: String? = null): Webhook?
    suspend fun findAll(tenantId: String? = null): List<Webhook>
    suspend fun findByEvent(event: String, tenantId: String? = null): List<Webhook>
    suspend fun update(webhook: Webhook): Webhook
    suspend fun delete(id: Int, tenantId: String? = null): Boolean
}
