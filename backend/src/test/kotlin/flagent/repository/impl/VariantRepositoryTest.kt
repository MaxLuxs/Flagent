package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Variant
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class VariantRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var repository: VariantRepository

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
        repository = VariantRepository()
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
