package flagent.repository.impl

import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class WebhookRepositoryTest {

    private lateinit var repository: WebhookRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = WebhookRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) { SchemaUtils.drop(Webhooks) }
        } catch (_: Exception) { }
    }

    @Test
    fun create_returnsWebhookWithId() = runBlocking {
        val w = Webhook(url = "https://x.com", events = listOf(WebhookEvents.FLAG_CREATED))
        val created = repository.create(w)
        assertTrue(created.id > 0)
        assertEquals("https://x.com", created.url)
        assertTrue(created.events.contains(WebhookEvents.FLAG_CREATED))
    }

    @Test
    fun findById_returnsWebhook() = runBlocking {
        val created = repository.create(Webhook(url = "https://a.com", events = emptyList()))
        val found = repository.findById(created.id, null)
        assertNotNull(found)
        assertEquals(created.id, found!!.id)
    }

    @Test
    fun findById_returnsNull_whenNotFound() = runBlocking {
        assertNull(repository.findById(99999, null))
    }

    @Test
    fun findAll_returnsList() = runBlocking {
        repository.create(Webhook(url = "https://b.com", events = emptyList()))
        val list = repository.findAll(null)
        assertTrue(list.size >= 1)
    }

    @Test
    fun update_modifiesWebhook() = runBlocking {
        val created = repository.create(Webhook(url = "https://old.com", events = emptyList()))
        val updated = repository.update(created.copy(url = "https://new.com"))
        assertEquals("https://new.com", updated.url)
    }

    @Test
    fun delete_returnsTrue() = runBlocking {
        val created = repository.create(Webhook(url = "https://del.com", events = emptyList()))
        val deleted = repository.delete(created.id, null)
        assertTrue(deleted)
        assertNull(repository.findById(created.id, null))
    }
}
