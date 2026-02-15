package flagent.repository.impl

import flagent.domain.entity.Distribution
import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
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
class DistributionRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var segmentRepository: SegmentRepository
    private lateinit var variantRepository: VariantRepository
    private lateinit var repository: DistributionRepository

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
        segmentRepository = SegmentRepository()
        variantRepository = VariantRepository()
        repository = DistributionRepository()
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
