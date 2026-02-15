package flagent.repository.impl

import flagent.domain.entity.FlagEntityType
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class FlagEntityTypeRepositoryTest {
    private lateinit var repository: FlagEntityTypeRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = FlagEntityTypeRepository()
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
