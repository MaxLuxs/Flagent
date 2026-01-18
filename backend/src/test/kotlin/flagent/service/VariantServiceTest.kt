package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IDistributionRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IVariantRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class VariantServiceTest {
    private lateinit var variantRepository: IVariantRepository
    private lateinit var flagRepository: IFlagRepository
    private lateinit var distributionRepository: IDistributionRepository
    private lateinit var flagSnapshotService: FlagSnapshotService
    private lateinit var variantService: VariantService
    
    @BeforeTest
    fun setup() {
        variantRepository = mockk()
        flagRepository = mockk()
        distributionRepository = mockk()
        flagSnapshotService = mockk()
        variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
    }
    
    @Test
    fun testFindVariantsByFlagId() = runBlocking {
        val variants = listOf(
            Variant(id = 1, flagId = 1, key = "variant1"),
            Variant(id = 2, flagId = 1, key = "variant2")
        )
        
        coEvery { variantRepository.findByFlagId(1) } returns variants
        
        val result = variantService.findVariantsByFlagId(1)
        
        assertEquals(2, result.size)
        coVerify { variantRepository.findByFlagId(1) }
    }
    
    @Test
    fun testCreateVariant_ThrowsException_WhenFlagNotFound() = runBlocking {
        val variant = Variant(flagId = 1, key = "test-variant")
        
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.createVariant(1, variant) }
        }
        
        coVerify { flagRepository.findById(1) }
        coVerify(exactly = 0) { variantRepository.create(any()) }
    }
    
    @Test
    fun testCreateVariant_ThrowsException_WhenKeyIsEmpty() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(flagId = 1, key = "")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.createVariant(1, variant) }
        }
    }
    
    @Test
    fun testCreateVariant_ThrowsException_WhenKeyIsTooLong() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(flagId = 1, key = "a".repeat(64))
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.createVariant(1, variant) }
        }
    }
    
    @Test
    fun testCreateVariant_Success() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(flagId = 1, key = "test-variant")
        val createdVariant = variant.copy(id = 1, flagId = 1)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.create(any()) } returns createdVariant
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = variantService.createVariant(1, variant)
        
        assertEquals(1, result.id)
        assertEquals(1, result.flagId)
        assertEquals("test-variant", result.key)
        coVerify { flagRepository.findById(1) }
        coVerify { variantRepository.create(match { it.flagId == 1 && it.key == "test-variant" }) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateVariant_ThrowsException_WhenFlagNotFound() = runBlocking {
        val variant = Variant(id = 1, flagId = 1, key = "updated-variant")
        
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.updateVariant(1, 1, variant) }
        }
    }
    
    @Test
    fun testUpdateVariant_ThrowsException_WhenVariantNotFound() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(id = 1, flagId = 1, key = "updated-variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.updateVariant(1, 1, variant) }
        }
    }
    
    @Test
    fun testUpdateVariant_ThrowsException_WhenVariantDoesNotBelongToFlag() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingVariant = Variant(id = 1, flagId = 2, key = "variant")
        val variant = Variant(id = 1, flagId = 1, key = "updated-variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns existingVariant
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.updateVariant(1, 1, variant) }
        }
    }
    
    @Test
    fun testUpdateVariant_Success() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingVariant = Variant(id = 1, flagId = 1, key = "old-variant")
        val variant = Variant(id = 1, flagId = 1, key = "updated-variant")
        val updatedVariant = variant.copy(flagId = 1)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns existingVariant
        coEvery { variantRepository.update(any()) } returns updatedVariant
        coEvery { distributionRepository.updateVariantKeyByVariantId(1, "updated-variant") } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = variantService.updateVariant(1, 1, variant)
        
        assertEquals("updated-variant", result.key)
        coVerify { variantRepository.update(any()) }
        coVerify { distributionRepository.updateVariantKeyByVariantId(1, "updated-variant") }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateVariant_WithUpdatedBy() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingVariant = Variant(id = 1, flagId = 1, key = "old-variant")
        val variant = Variant(id = 1, flagId = 1, key = "updated-variant")
        val updatedVariant = variant.copy(flagId = 1)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns existingVariant
        coEvery { variantRepository.update(any()) } returns updatedVariant
        coEvery { distributionRepository.updateVariantKeyByVariantId(1, "updated-variant") } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = variantService.updateVariant(1, 1, variant, updatedBy = "test-user")
        
        assertEquals("updated-variant", result.key)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testDeleteVariant_ThrowsException_WhenVariantHasNonZeroDistribution() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            segments = listOf(
                Segment(id = 1, flagId = 1, distributions = listOf(
                    Distribution(id = 1, segmentId = 1, variantId = 1, percent = 50)
                ))
            )
        )
        val variant = Variant(id = 1, flagId = 1, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        coEvery { distributionRepository.findBySegmentId(1) } returns listOf(
            Distribution(id = 1, segmentId = 1, variantId = 1, percent = 50)
        )
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.deleteVariant(1, 1) }
        }
    }
    
    @Test
    fun testDeleteVariant_Success_WhenDistributionsAreZero() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            segments = listOf(
                Segment(id = 1, flagId = 1, distributions = listOf(
                    Distribution(id = 1, segmentId = 1, variantId = 1, percent = 0)
                ))
            )
        )
        val variant = Variant(id = 1, flagId = 1, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        coEvery { distributionRepository.findBySegmentId(1) } returns listOf(
            Distribution(id = 1, segmentId = 1, variantId = 1, percent = 0)
        )
        coEvery { variantRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        variantService.deleteVariant(1, 1)
        
        coVerify { variantRepository.delete(1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateVariant_UpdatesDistributionsVariantKey() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingVariant = Variant(id = 1, flagId = 1, key = "old-variant")
        val variant = Variant(id = 1, flagId = 1, key = "new-variant")
        val updatedVariant = variant.copy(flagId = 1)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns existingVariant
        coEvery { variantRepository.update(any()) } returns updatedVariant
        coEvery { distributionRepository.updateVariantKeyByVariantId(1, "new-variant") } just Runs
        
        val result = variantService.updateVariant(1, 1, variant)
        
        assertEquals("new-variant", result.key)
        coVerify { distributionRepository.updateVariantKeyByVariantId(1, "new-variant") }
    }
    
    @Test
    fun testDeleteVariant_ThrowsException_WhenMultipleSegmentsHaveNonZeroDistribution() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            segments = listOf(
                Segment(id = 1, flagId = 1),
                Segment(id = 2, flagId = 1)
            )
        )
        val variant = Variant(id = 1, flagId = 1, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        coEvery { distributionRepository.findBySegmentId(1) } returns listOf(
            Distribution(id = 1, segmentId = 1, variantId = 1, percent = 0)
        )
        coEvery { distributionRepository.findBySegmentId(2) } returns listOf(
            Distribution(id = 2, segmentId = 2, variantId = 1, percent = 50) // Non-zero!
        )
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { variantService.deleteVariant(1, 1) }
        }
    }
    
    @Test
    fun testDeleteVariant_Success_WhenNoDistributions() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            segments = listOf(
                Segment(id = 1, flagId = 1)
            )
        )
        val variant = Variant(id = 1, flagId = 1, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        coEvery { distributionRepository.findBySegmentId(1) } returns emptyList()
        coEvery { variantRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        variantService.deleteVariant(1, 1)
        
        coVerify { variantRepository.delete(1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testDeleteVariant_WithUpdatedBy() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            segments = listOf(
                Segment(id = 1, flagId = 1)
            )
        )
        val variant = Variant(id = 1, flagId = 1, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        coEvery { distributionRepository.findBySegmentId(1) } returns emptyList()
        coEvery { variantRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        variantService.deleteVariant(1, 1, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testCreateVariant_WithUpdatedBy() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(flagId = 1, key = "test-variant")
        val createdVariant = variant.copy(id = 1, flagId = 1)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.create(any()) } returns createdVariant
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = variantService.createVariant(1, variant, updatedBy = "test-user")
        
        assertEquals(1, result.id)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testCreateVariant_WithInvalidKeyCharacters() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(flagId = 1, key = "invalid@key")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            variantService.createVariant(1, variant)
        }
    }
    
    @Test
    fun testCreateVariant_WithValidSpecialCharacters() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val validKeys = listOf("variant-key", "variant_key", "variant.key", "variant:key", "variant/key", "variant123")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        validKeys.forEach { key ->
            val variant = Variant(flagId = 1, key = key)
            val createdVariant = variant.copy(id = 1, flagId = 1)
            coEvery { variantRepository.create(any()) } returns createdVariant
            
            val result = variantService.createVariant(1, variant)
            assertEquals(key, result.key)
        }
    }
    
    @Test
    fun testUpdateVariant_WithInvalidKey() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingVariant = Variant(id = 1, flagId = 1, key = "old-variant")
        val variant = Variant(id = 1, flagId = 1, key = "invalid key with spaces")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns existingVariant
        
        assertFailsWith<IllegalArgumentException> {
            variantService.updateVariant(1, 1, variant)
        }
    }
    
    @Test
    fun testDeleteVariant_ThrowsException_WhenFlagNotFound() = runBlocking {
        coEvery { flagRepository.findById(999) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            variantService.deleteVariant(999, 1)
        }
    }
    
    @Test
    fun testDeleteVariant_ThrowsException_WhenVariantDoesNotBelongToFlag() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val variant = Variant(id = 1, flagId = 2, key = "variant")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { variantRepository.findById(1) } returns variant
        
        assertFailsWith<IllegalArgumentException> {
            variantService.deleteVariant(1, 1)
        }
    }
}
