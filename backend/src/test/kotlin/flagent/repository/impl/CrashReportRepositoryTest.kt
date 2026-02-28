package flagent.repository.impl

import flagent.domain.entity.CrashReport
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class CrashReportRepositoryTest {

    private lateinit var repository: CrashReportRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = CrashReportRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) { SchemaUtils.drop(CrashReports) }
        } catch (_: Exception) { }
    }

    @Test
    fun save_returnsWithId() = runBlocking {
        val crash = CrashReport(
            stackTrace = "at foo.bar",
            message = "NPE",
            platform = "android",
            timestamp = 12345L
        )
        val saved = repository.save(crash)
        assertTrue(saved.id > 0)
        assertEquals("at foo.bar", saved.stackTrace)
    }

    @Test
    fun saveBatch_returnsListWithIds() = runBlocking {
        val crashes = listOf(
            CrashReport(stackTrace = "s1", message = "m1", platform = "p", timestamp = 1L),
            CrashReport(stackTrace = "s2", message = "m2", platform = "p", timestamp = 2L)
        )
        val saved = repository.saveBatch(crashes)
        assertEquals(2, saved.size)
        assertTrue(saved[0].id > 0)
        assertTrue(saved[1].id > 0)
    }

    @Test
    fun list_returnsSaved() = runBlocking {
        val saved = repository.save(
            CrashReport(stackTrace = "s", message = "m", platform = "p", timestamp = 999L)
        )
        val list = repository.list(null, null, null, 50, 0)
        assertTrue(list.any { it.id == saved.id })
    }

    @Test
    fun count_returnsCorrectCount() = runBlocking {
        repository.save(CrashReport(stackTrace = "s", message = "m", platform = "p", timestamp = 1L))
        val n = repository.count(null, null, null)
        assertTrue(n >= 1)
    }
}
