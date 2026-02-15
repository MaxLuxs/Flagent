package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Tag
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class TagRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var repository: TagRepository

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
        repository = TagRepository()
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
    fun testCreateTag() = runBlocking {
        val tag = Tag(value = "test_tag")
        
        val created = repository.create(tag)
        
        assertTrue(created.id > 0)
        assertEquals("test_tag", created.value)
    }
    
    @Test
    fun testCreateTag_ReturnsExisting_WhenTagExists() = runBlocking {
        val tag = Tag(value = "test_tag")
        val created1 = repository.create(tag)
        val created2 = repository.create(tag)
        
        assertEquals(created1.id, created2.id)
        assertEquals("test_tag", created2.value)
    }
    
    @Test
    fun testFindByValue() = runBlocking {
        val tag = repository.create(Tag(value = "test_tag"))
        
        val found = repository.findByValue("test_tag")
        
        assertNotNull(found)
        assertEquals(tag.id, found.id)
        assertEquals("test_tag", found.value)
    }
    
    @Test
    fun testFindAll() = runBlocking {
        repository.create(Tag(value = "tag1"))
        repository.create(Tag(value = "tag2"))
        repository.create(Tag(value = "tag3"))
        
        val all = repository.findAll()
        
        assertTrue(all.size >= 3)
    }
    
    @Test
    fun testFindAll_WithValueLike() = runBlocking {
        repository.create(Tag(value = "test_tag1"))
        repository.create(Tag(value = "test_tag2"))
        repository.create(Tag(value = "other_tag"))
        
        val found = repository.findAll(valueLike = "test")
        
        assertTrue(found.size >= 2)
        assertTrue(found.all { it.value.contains("test", ignoreCase = true) })
    }
    
    @Test
    fun testDeleteTag() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val tag = repository.create(Tag(value = "test_tag"))
        
        repository.addTagToFlag(flag.id, tag.id)
        repository.removeTagFromFlag(flag.id, tag.id)
        
        // Tag should still exist, just not attached to flag
        val found = repository.findByValue("test_tag")
        assertNotNull(found)
    }
}
