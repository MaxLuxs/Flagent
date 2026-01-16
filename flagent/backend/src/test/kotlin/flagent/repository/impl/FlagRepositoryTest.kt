package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.repository.Database
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class FlagRepositoryTest {
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
    
    private lateinit var repository: FlagRepository
    
    @BeforeTest
    fun setup() {
        // Use SQLite in-memory for tests (faster and simpler)
        // For PostgreSQL tests with TestContainers, uncomment the lines below
        // and set environment variables before running tests:
        // FLAGENT_DB_DBDRIVER=postgres
        // FLAGENT_DB_CONNECTIONSTR=<postgres.jdbcUrl>
        
        // For now, use SQLite in-memory (default)
        Database.init()
        repository = FlagRepository()
    }
    
    @AfterTest
    fun cleanup() {
        // Clean up database
        try {
            transaction(Database.getDatabase()) {
                org.jetbrains.exposed.sql.SchemaUtils.drop(
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
    fun testCreateFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag description",
            enabled = true
        )
        
        val created = repository.create(flag)
        
        assertTrue(created.id > 0)
        assertEquals("test_flag", created.key)
        assertEquals("Test flag description", created.description)
        assertTrue(created.enabled)
    }
    
    @Test
    fun testFindById() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findById(created.id)
        
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("test_flag", found.key)
        assertEquals("Test flag", found.description)
    }
    
    @Test
    fun testFindById_ReturnsNull_WhenNotFound() = runBlocking {
        val found = repository.findById(999)
        assertNull(found)
    }
    
    @Test
    fun testFindByKey() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findByKey("test_flag")
        
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("test_flag", found.key)
    }
    
    @Test
    fun testFindByKey_ReturnsNull_WhenNotFound() = runBlocking {
        val found = repository.findByKey("non_existent")
        assertNull(found)
    }
    
    @Test
    fun testFindAll() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = false))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val all = repository.findAll()
        
        assertTrue(all.size >= 3)
    }
    
    @Test
    fun testFindAll_WithLimit() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val limited = repository.findAll(limit = 2, offset = 0)
        
        assertEquals(2, limited.size)
    }
    
    @Test
    fun testFindAll_WithOffset() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val offset = repository.findAll(limit = 2, offset = 1)
        
        assertTrue(offset.size <= 2)
    }
    
    @Test
    fun testFindAll_WithEnabledFilter() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = false))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val enabled = repository.findAll(enabled = true)
        
        assertTrue(enabled.all { it.enabled })
        assertTrue(enabled.size >= 2)
    }
    
    @Test
    fun testFindAll_WithKeyFilter() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        
        val found = repository.findAll(key = "flag1")
        
        assertEquals(1, found.size)
        assertEquals("flag1", found[0].key)
    }
    
    @Test
    fun testFindAll_WithDescriptionLike() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Test flag one", enabled = true))
        repository.create(Flag(key = "flag2", description = "Test flag two", enabled = true))
        repository.create(Flag(key = "flag3", description = "Other description", enabled = true))
        
        val found = repository.findAll(descriptionLike = "test")
        
        assertTrue(found.size >= 2)
        assertTrue(found.all { it.description.lowercase().contains("test") })
    }
    
    @Test
    fun testUpdateFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Original description",
            enabled = true
        )
        
        val created = repository.create(flag)
        val updated = repository.update(created.copy(description = "Updated description"))
        
        assertEquals(created.id, updated.id)
        assertEquals("Updated description", updated.description)
        
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals("Updated description", found.description)
    }
    
    @Test
    fun testDeleteFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        repository.delete(created.id)
        
        val found = repository.findById(created.id)
        assertNull(found)
        
        val foundDeleted = repository.findAll(deleted = true)
        assertTrue(foundDeleted.any { it.id == created.id })
    }
    
    @Test
    fun testRestoreFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        repository.delete(created.id)
        
        val restored = repository.restore(created.id)
        
        assertNotNull(restored)
        assertEquals(created.id, restored?.id)
        
        val found = repository.findById(created.id)
        assertNotNull(found)
    }
    
    @Test
    fun testFindAll_WithPreload() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findAll(preload = true)
        
        assertTrue(found.isNotEmpty())
        // With preload, segments and variants should be loaded (even if empty)
        val flagWithPreload = found.find { it.id == created.id }
        assertNotNull(flagWithPreload)
        assertNotNull(flagWithPreload.segments)
        assertNotNull(flagWithPreload.variants)
    }
}
