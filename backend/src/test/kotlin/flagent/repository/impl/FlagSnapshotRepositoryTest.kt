package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.FlagSnapshot
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class FlagSnapshotRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var repository: FlagSnapshotRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        flagRepository = FlagRepository()
        repository = FlagSnapshotRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) {
                SchemaUtils.drop(
                    Flags, Segments, Variants, Constraints, Distributions,
                    Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Users
                )
            }
        } catch (_: Exception) { }
    }
    
    @Test
    fun testCreateSnapshot() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val snapshot = FlagSnapshot(
            flagId = flag.id,
            updatedBy = "test_user",
            flag = "{\"id\":${flag.id},\"key\":\"test_flag\"}"
        )
        
        val created = repository.create(snapshot)
        
        assertTrue(created.id > 0)
        assertEquals(flag.id, created.flagId)
        assertEquals("test_user", created.updatedBy)
    }
    
    @Test
    fun testFindByFlagId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        repository.create(FlagSnapshot(flagId = flag.id, updatedBy = "user1", flag = "{}"))
        repository.create(FlagSnapshot(flagId = flag.id, updatedBy = "user2", flag = "{}"))
        
        val snapshots = repository.findByFlagId(flag.id)
        
        assertEquals(2, snapshots.size)
        assertTrue(snapshots.all { it.flagId == flag.id })
    }
    
    @Test
    fun testFindAll() = runBlocking {
        val flag1 = flagRepository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        val flag2 = flagRepository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        repository.create(FlagSnapshot(flagId = flag1.id, updatedBy = "user1", flag = "{}"))
        repository.create(FlagSnapshot(flagId = flag2.id, updatedBy = "user2", flag = "{}"))
        
        val all = repository.findAll()
        
        assertTrue(all.size >= 2)
    }
}
