package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.FlagSnapshot
import flagent.repository.Database
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class FlagSnapshotRepositoryTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            start()
        }
    }
    
    private lateinit var flagRepository: FlagRepository
    private lateinit var repository: FlagSnapshotRepository
    
    @BeforeTest
    fun setup() {
        Database.init()
        flagRepository = FlagRepository()
        repository = FlagSnapshotRepository()
    }
    
    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) {
                SchemaUtils.drop(
                    flagent.repository.tables.Flags,
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
