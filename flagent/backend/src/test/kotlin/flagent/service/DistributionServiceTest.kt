package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IDistributionRepository
import flagent.domain.repository.IFlagRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class DistributionServiceTest {
    private lateinit var distributionRepository: IDistributionRepository
    private lateinit var flagRepository: IFlagRepository
    private lateinit var distributionService: DistributionService
    
    @BeforeTest
    fun setup() {
        distributionRepository = mockk()
        flagRepository = mockk()
        distributionService = DistributionService(distributionRepository, flagRepository)
    }
    
    @Test
    fun testFindDistributionsBySegmentId() = runBlocking {
        val distributions = listOf(
            Distribution(id = 1, segmentId = 1, variantId = 1, percent = 50),
            Distribution(id = 2, segmentId = 1, variantId = 2, percent = 50)
        )
        
        coEvery { distributionRepository.findBySegmentId(1) } returns distributions
        
        val result = distributionService.findDistributionsBySegmentId(1)
        
        assertEquals(2, result.size)
        coVerify { distributionRepository.findBySegmentId(1) }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenSumNot100() = runBlocking {
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, percent = 50),
            Distribution(segmentId = 1, variantId = 2, percent = 30) // Sum = 80, not 100
        )
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
        
        coVerify(exactly = 0) { distributionRepository.updateDistributions(any(), any()) }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenFlagNotFound() = runBlocking {
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, percent = 100)
        )
        
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenVariantNotFound() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            variants = listOf(Variant(id = 1, flagId = 1, key = "variant1"))
        )
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 999, percent = 100) // Variant 999 doesn't exist
        )
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenVariantKeyMismatch() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            variants = listOf(Variant(id = 1, flagId = 1, key = "variant1"))
        )
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, variantKey = "wrong-key", percent = 100)
        )
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_Success() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            variants = listOf(
                Variant(id = 1, flagId = 1, key = "variant1"),
                Variant(id = 2, flagId = 1, key = "variant2")
            )
        )
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, variantKey = "variant1", percent = 60),
            Distribution(segmentId = 1, variantId = 2, variantKey = "variant2", percent = 40)
        )
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { distributionRepository.updateDistributions(1, distributions) } just Runs
        
        distributionService.updateDistributions(1, 1, distributions)
        
        coVerify { flagRepository.findById(1) }
        coVerify { distributionRepository.updateDistributions(1, distributions) }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenSumGreaterThan100() = runBlocking {
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, percent = 60),
            Distribution(segmentId = 1, variantId = 2, percent = 50) // Sum = 110, not 100
        )
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenSumLessThan100() = runBlocking {
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, percent = 30),
            Distribution(segmentId = 1, variantId = 2, percent = 40) // Sum = 70, not 100
        )
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenEmptyDistributions() = runBlocking {
        val distributions = emptyList<Distribution>()
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
    
    @Test
    fun testUpdateDistributions_ThrowsException_WhenSingleDistributionNot100() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test-flag",
            description = "Test",
            variants = listOf(Variant(id = 1, flagId = 1, key = "variant1"))
        )
        val distributions = listOf(
            Distribution(segmentId = 1, variantId = 1, variantKey = "variant1", percent = 50) // Not 100
        )
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { distributionService.updateDistributions(1, 1, distributions) }
        }
    }
}
