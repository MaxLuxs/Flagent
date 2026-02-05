package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.repository.Database
import flagent.repository.tables.EvaluationEvents
import flagent.repository.tables.Flags
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.*

class EvaluationEventRepositoryTest {
    private lateinit var eventRepo: EvaluationEventRepository
    private lateinit var flagRepo: FlagRepository

    @BeforeTest
    fun setup() {
        Database.init()
        eventRepo = EvaluationEventRepository()
        flagRepo = FlagRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) {
                SchemaUtils.drop(
                    EvaluationEvents,
                    Flags,
                    flagent.repository.tables.Segments,
                    flagent.repository.tables.Variants,
                    flagent.repository.tables.Constraints,
                    flagent.repository.tables.Distributions,
                    flagent.repository.tables.Tags,
                    flagent.repository.tables.FlagsTags,
                    flagent.repository.tables.FlagSnapshots,
                    flagent.repository.tables.FlagEntityTypes,
                    flagent.repository.tables.Users
                )
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        Database.close()
    }

    @Test
    fun saveBatch_emptyList_doesNothing() = runBlocking {
        eventRepo.saveBatch(emptyList())
        // No exception
    }

    @Test
    fun saveBatch_insertsEvents() = runBlocking {
        val flag = flagRepo.create(Flag(key = "test_flag", description = "Test", enabled = true))
        val now = System.currentTimeMillis()
        val events = listOf(
            Pair(flag.id, now),
            Pair(flag.id, now + 1000)
        )
        eventRepo.saveBatch(events)

        val start = now - 1000
        val end = now + 2000
        val overview = eventRepo.getOverview(start, end, 10, 3600_000)

        assertEquals(2L, overview.totalEvaluations)
        assertEquals(1, overview.uniqueFlags)
        assertEquals(1, overview.topFlags.size)
        assertEquals(flag.id, overview.topFlags[0].flagId)
        assertEquals("test_flag", overview.topFlags[0].flagKey)
        assertEquals(2L, overview.topFlags[0].evaluationCount)
    }

    @Test
    fun getOverview_respectsTimeRange() = runBlocking {
        val flag = flagRepo.create(Flag(key = "test_flag", description = "Test", enabled = true))
        val base = 1_700_000_000_000L // fixed timestamp
        eventRepo.saveBatch(
            listOf(
                Pair(flag.id, base),
                Pair(flag.id, base + 1000),
                Pair(flag.id, base + 100_000) // outside range
            )
        )

        val overview = eventRepo.getOverview(base, base + 5000, 10, 3600_000)
        assertEquals(2L, overview.totalEvaluations)
    }

    @Test
    fun getOverview_topFlagsRespectsLimit() = runBlocking {
        val f1 = flagRepo.create(Flag(key = "flag_1", description = "1", enabled = true))
        val f2 = flagRepo.create(Flag(key = "flag_2", description = "2", enabled = true))
        val f3 = flagRepo.create(Flag(key = "flag_3", description = "3", enabled = true))
        val base = System.currentTimeMillis() - 60_000
        eventRepo.saveBatch(
            listOf(
                Pair(f1.id, base),
                Pair(f1.id, base + 1),
                Pair(f2.id, base + 2),
                Pair(f3.id, base + 3)
            )
        )

        val overview = eventRepo.getOverview(base - 1000, base + 10_000, 2, 3600_000)
        assertEquals(4L, overview.totalEvaluations)
        assertEquals(3, overview.uniqueFlags)
        assertEquals(2, overview.topFlags.size)
        assertTrue(overview.topFlags[0].evaluationCount >= overview.topFlags[1].evaluationCount)
    }

    @Test
    fun getOverview_timeSeriesBuckets() = runBlocking {
        val flag = flagRepo.create(Flag(key = "test_flag", description = "Test", enabled = true))
        val bucketMs = 3600_000L
        val base = (System.currentTimeMillis() / bucketMs) * bucketMs
        eventRepo.saveBatch(
            listOf(
                Pair(flag.id, base),
                Pair(flag.id, base + 1000),
                Pair(flag.id, base + 2 * bucketMs)
            )
        )

        val overview = eventRepo.getOverview(base - 1000, base + 3 * bucketMs, 10, bucketMs)
        assertEquals(2, overview.timeSeries.size)
        val counts = overview.timeSeries.map { it.count }
        assertTrue(counts.contains(2L))
        assertTrue(counts.contains(1L))
    }

    @Test
    fun getOverview_emptyRange_returnsZeros() = runBlocking {
        val base = System.currentTimeMillis()
        val overview = eventRepo.getOverview(base - 1_000_000, base - 500_000, 10, 3600_000)

        assertEquals(0L, overview.totalEvaluations)
        assertEquals(0, overview.uniqueFlags)
        assertTrue(overview.topFlags.isEmpty())
        assertTrue(overview.timeSeries.isEmpty())
    }

    @Test
    fun deleteOlderThan_removesOldEvents() = runBlocking {
        val flag = flagRepo.create(Flag(key = "test_flag", description = "Test", enabled = true))
        val now = System.currentTimeMillis()
        val oldTs = now - (100L * 24 * 60 * 60 * 1000) // 100 days ago
        val recentTs = now - (10L * 24 * 60 * 60 * 1000) // 10 days ago

        eventRepo.saveBatch(
            listOf(
                Pair(flag.id, oldTs),
                Pair(flag.id, oldTs + 1000),
                Pair(flag.id, recentTs)
            )
        )

        val cutoff = now - (90L * 24 * 60 * 60 * 1000) // 90 days ago
        val deleted = eventRepo.deleteOlderThan(cutoff)

        assertEquals(2, deleted)

        val overview = eventRepo.getOverview(now - 30L * 24 * 60 * 60 * 1000, now + 1000, 10, 3600_000)
        assertEquals(1L, overview.totalEvaluations)
    }
}
