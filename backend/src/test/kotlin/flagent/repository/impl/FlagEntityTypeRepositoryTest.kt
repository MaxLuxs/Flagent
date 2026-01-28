package flagent.repository.impl

import flagent.domain.entity.FlagEntityType
import flagent.repository.Database
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class FlagEntityTypeRepositoryTest {
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
    
    private lateinit var repository: FlagEntityTypeRepository
    
    @BeforeTest
    fun setup() {
        Database.init()
        repository = FlagEntityTypeRepository()
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
    fun testCreateEntityType() = runBlocking {
        val entityType = FlagEntityType(key = "user")
        
        val created = repository.create(entityType)
        
        assertTrue(created.id > 0)
        assertEquals("user", created.key)
    }
    
    @Test
    fun testCreateEntityType_ReturnsExisting_WhenExists() = runBlocking {
        val entityType = FlagEntityType(key = "user")
        val created1 = repository.create(entityType)
        val created2 = repository.create(entityType)
        
        assertEquals(created1.id, created2.id)
        assertEquals("user", created2.key)
    }
    
    @Test
    fun testFindByKey() = runBlocking {
        val entityType = repository.create(FlagEntityType(key = "user"))
        
        val found = repository.findByKey("user")
        
        assertNotNull(found)
        assertEquals(entityType.id, found.id)
        assertEquals("user", found.key)
    }
    
    @Test
    fun testFindAll() = runBlocking {
        repository.create(FlagEntityType(key = "user"))
        repository.create(FlagEntityType(key = "session"))
        repository.create(FlagEntityType(key = "device"))
        
        val all = repository.findAll()
        
        assertTrue(all.size >= 3)
        assertTrue(all.all { it.key in listOf("user", "session", "device") })
    }
}
