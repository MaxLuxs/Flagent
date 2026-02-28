package flagent.repository.impl

import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class AnalyticsEventRepositoryTest {

    private lateinit var repository: AnalyticsEventRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = AnalyticsEventRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) { SchemaUtils.drop(AnalyticsEvents) }
        } catch (_: Exception) { }
    }

    @Test
    fun saveBatch_persistsEvents() = runBlocking {
        val events = listOf(
            AnalyticsEventRecord(eventName = "first_open", timestampMs = 1L),
            AnalyticsEventRecord(eventName = "session_start", timestampMs = 2L)
        )
        repository.saveBatch(events, null)
        val deleted = repository.deleteOlderThan(100L)
        assertTrue(deleted >= 2)
    }

    @Test
    fun deleteOlderThan_returnsDeletedCount() = runBlocking {
        repository.saveBatch(
            listOf(AnalyticsEventRecord(eventName = "e", timestampMs = 10L)),
            null
        )
        val deleted = repository.deleteOlderThan(20L)
        assertTrue(deleted >= 1)
    }
}
