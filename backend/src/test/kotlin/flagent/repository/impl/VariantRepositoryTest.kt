package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Variant
import flagent.repository.Database
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class VariantRepositoryTest {
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
    private lateinit var repository: VariantRepository
    
    @BeforeTest
    fun setup() {
        Database.init()
        flagRepository = FlagRepository()
        repository = VariantRepository()
    }
    
    @AfterTest
    fun cleanup() {
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
    fun testCreateVariant() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = Variant(flagId = flag.id, key = "variant1")
        
        val created = repository.create(variant)
        
        assertTrue(created.id > 0)
        assertEquals(flag.id, created.flagId)
        assertEquals("variant1", created.key)
    }
    
    @Test
    fun testFindById() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = repository.create(Variant(flagId = flag.id, key = "variant1"))
        
        val found = repository.findById(variant.id)
        
        assertNotNull(found)
        assertEquals(variant.id, found.id)
        assertEquals("variant1", found.key)
    }
    
    @Test
    fun testFindByFlagId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        repository.create(Variant(flagId = flag.id, key = "variant1"))
        repository.create(Variant(flagId = flag.id, key = "variant2"))
        
        val variants = repository.findByFlagId(flag.id)
        
        assertEquals(2, variants.size)
        assertTrue(variants.all { it.flagId == flag.id })
    }
    
    @Test
    fun testUpdateVariant() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = repository.create(Variant(flagId = flag.id, key = "variant1"))
        
        val updated = repository.update(variant.copy(key = "variant_updated"))
        
        assertEquals(variant.id, updated.id)
        assertEquals("variant_updated", updated.key)
        
        val found = repository.findById(variant.id)
        assertNotNull(found)
        assertEquals("variant_updated", found.key)
    }
    
    @Test
    fun testDeleteVariant() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = repository.create(Variant(flagId = flag.id, key = "variant1"))
        
        repository.delete(variant.id)
        
        val found = repository.findById(variant.id)
        assertNull(found)
    }
}
