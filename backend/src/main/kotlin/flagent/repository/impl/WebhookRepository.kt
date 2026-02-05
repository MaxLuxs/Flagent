package flagent.repository.impl

import flagent.domain.entity.Webhook
import flagent.domain.repository.IWebhookRepository
import flagent.repository.Database
import flagent.repository.tables.Webhooks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

/**
 * Webhook repository implementation.
 */
class WebhookRepository : IWebhookRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun create(webhook: Webhook): Webhook = withContext(Dispatchers.IO) {
        Database.transaction {
            val id = Webhooks.insert {
                it[url] = webhook.url
                it[events] = json.encodeToString(webhook.events)
                it[secret] = webhook.secret
                it[enabled] = webhook.enabled
                it[tenantId] = webhook.tenantId
                it[createdAt] = webhook.createdAt
                it[updatedAt] = webhook.updatedAt
            }[Webhooks.id].value
            webhook.copy(id = id)
        }
    }

    override suspend fun findById(id: Int, tenantId: String?): Webhook? = withContext(Dispatchers.IO) {
        Database.transaction {
            val whereOp = if (tenantId != null) {
                (Webhooks.id eq id) and (Webhooks.tenantId eq tenantId)
            } else {
                (Webhooks.id eq id) and (Webhooks.tenantId.isNull())
            }
            Webhooks.selectAll().where { whereOp }.map { it.toWebhook() }.singleOrNull()
        }
    }

    override suspend fun findAll(tenantId: String?): List<Webhook> = withContext(Dispatchers.IO) {
        Database.transaction {
            val whereOp = if (tenantId != null) {
                Webhooks.tenantId eq tenantId
            } else {
                Webhooks.tenantId.isNull()
            }
            Webhooks.selectAll().where { whereOp }
                .orderBy(Webhooks.id, SortOrder.ASC)
                .map { it.toWebhook() }
        }
    }

    override suspend fun findByEvent(event: String, tenantId: String?): List<Webhook> {
        val all = findAll(tenantId)
        return all.filter { it.subscribesTo(event) && it.enabled }
    }

    override suspend fun update(webhook: Webhook): Webhook = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        Database.transaction {
            Webhooks.update({ Webhooks.id eq webhook.id }) {
                it[url] = webhook.url
                it[events] = json.encodeToString(webhook.events)
                it[secret] = webhook.secret
                it[enabled] = webhook.enabled
                it[updatedAt] = now
            }
            webhook.copy(updatedAt = now)
        }
    }

    override suspend fun delete(id: Int, tenantId: String?): Boolean = withContext(Dispatchers.IO) {
        Database.transaction {
            val whereOp = if (tenantId != null) {
                (Webhooks.id eq id) and (Webhooks.tenantId eq tenantId)
            } else {
                (Webhooks.id eq id) and (Webhooks.tenantId.isNull())
            }
            Webhooks.deleteWhere { whereOp } > 0
        }
    }

    private fun ResultRow.toWebhook() = Webhook(
        id = this[Webhooks.id].value,
        url = this[Webhooks.url],
        events = json.decodeFromString<List<String>>(this[Webhooks.events]),
        secret = this[Webhooks.secret],
        enabled = this[Webhooks.enabled],
        tenantId = this[Webhooks.tenantId],
        createdAt = this[Webhooks.createdAt],
        updatedAt = this[Webhooks.updatedAt]
    )
}
