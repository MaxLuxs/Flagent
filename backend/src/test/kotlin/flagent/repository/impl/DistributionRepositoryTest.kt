package flagent.repository.impl

import flagent.domain.entity.Distribution
import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.domain.entity.Variant
import flagent.repository.Database
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

@Testcontainers
class DistributionRepositoryTest {
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
    private lateinit var segmentRepository: SegmentRepository
    private lateinit var variantRepository: VariantRepository
    private lateinit var repository: DistributionRepository
    
    @BeforeTest
    fun setup() {
        Database.init()
        flagRepository = FlagRepository()
        segmentRepository = SegmentRepository()
        variantRepository = VariantRepository()
        repository = DistributionRepository()
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
    fun testUpdateDistributions() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val variant1 = variantRepository.create(Variant(flagId = flag.id, key = "variant1"))
        val variant2 = variantRepository.create(Variant(flagId = flag.id, key = "variant2"))
        
        val distributions = listOf(
            Distribution(segmentId = segment.id, variantId = variant1.id, variantKey = "variant1", percent = 50),
            Distribution(segmentId = segment.id, variantId = variant2.id, variantKey = "variant2", percent = 50)
        )
        
        repository.updateDistributions(segment.id, distributions)
        
        val found = repository.findBySegmentId(segment.id)
        assertEquals(2, found.size)
        assertEquals(50, found[0].percent)
        assertEquals(50, found[1].percent)
    }
    
    @Test
    fun testFindBySegmentId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val variant = variantRepository.create(Variant(flagId = flag.id, key = "variant1"))
        
        val distributions = listOf(
            Distribution(segmentId = segment.id, variantId = variant.id, variantKey = "variant1", percent = 100)
        )
        
        repository.updateDistributions(segment.id, distributions)
        
        val found = repository.findBySegmentId(segment.id)
        assertEquals(1, found.size)
        assertEquals(variant.id, found[0].variantId)
        assertEquals(100, found[0].percent)
    }
    
    @Test
    fun testUpdateVariantKeyByVariantId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val variant = variantRepository.create(Variant(flagId = flag.id, key = "variant1"))
        
        val distributions = listOf(
            Distribution(segmentId = segment.id, variantId = variant.id, variantKey = "variant1", percent = 100)
        )
        
        repository.updateDistributions(segment.id, distributions)
        
        variantRepository.update(variant.copy(key = "variant_updated"))
        repository.updateVariantKeyByVariantId(variant.id, "variant_updated")
        
        val found = repository.findBySegmentId(segment.id)
        assertEquals("variant_updated", found[0].variantKey)
    }
}
