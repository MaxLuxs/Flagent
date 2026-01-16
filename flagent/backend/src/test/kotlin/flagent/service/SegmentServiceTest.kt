package flagent.service

import flagent.domain.entity.Segment
import flagent.domain.repository.ISegmentRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SegmentServiceTest {
    private lateinit var segmentRepository: ISegmentRepository
    private lateinit var segmentService: SegmentService
    
    @BeforeTest
    fun setup() {
        segmentRepository = mockk()
        segmentService = SegmentService(segmentRepository)
    }
    
    @Test
    fun testFindSegmentsByFlagId() = runBlocking {
        val segments = listOf(
            Segment(id = 1, flagId = 1, description = "Segment 1", rank = 1, rolloutPercent = 50),
            Segment(id = 2, flagId = 1, description = "Segment 2", rank = 2, rolloutPercent = 50)
        )
        
        coEvery { segmentRepository.findByFlagId(1) } returns segments
        
        val result = segmentService.findSegmentsByFlagId(1)
        
        assertEquals(2, result.size)
        coVerify { segmentRepository.findByFlagId(1) }
    }
    
    @Test
    fun testGetSegment() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        
        coEvery { segmentRepository.findById(1) } returns segment
        
        val result = segmentService.getSegment(1)
        
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Test segment", result.description)
        coVerify { segmentRepository.findById(1) }
    }
    
    @Test
    fun testGetSegment_ReturnsNull_WhenNotFound() = runBlocking {
        coEvery { segmentRepository.findById(999) } returns null
        
        val result = segmentService.getSegment(999)
        
        assertNull(result)
        coVerify { segmentRepository.findById(999) }
    }
    
    @Test
    fun testCreateSegment_SetsDefaultRank() = runBlocking {
        val segment = Segment(id = 0, flagId = 1, description = "New segment", rank = 0, rolloutPercent = 100)
        val createdSegment = segment.copy(id = 1, rank = SegmentService.SegmentDefaultRank)
        
        coEvery { segmentRepository.create(any()) } returns createdSegment
        
        val result = segmentService.createSegment(segment)
        
        assertEquals(SegmentService.SegmentDefaultRank, result.rank)
        coVerify { segmentRepository.create(match { it.rank == SegmentService.SegmentDefaultRank }) }
    }
    
    @Test
    fun testUpdateSegment() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Updated segment", rank = 1, rolloutPercent = 75)
        
        coEvery { segmentRepository.update(any()) } returns segment
        
        val result = segmentService.updateSegment(segment)
        
        assertEquals("Updated segment", result.description)
        assertEquals(75, result.rolloutPercent)
        coVerify { segmentRepository.update(segment) }
    }
    
    @Test
    fun testDeleteSegment() = runBlocking {
        coEvery { segmentRepository.delete(1) } just Runs
        
        segmentService.deleteSegment(1)
        
        coVerify { segmentRepository.delete(1) }
    }
    
    @Test
    fun testReorderSegments() = runBlocking {
        val segmentIds = listOf(3, 1, 2)
        
        coEvery { segmentRepository.reorder(1, segmentIds) } just Runs
        
        segmentService.reorderSegments(1, segmentIds)
        
        coVerify { segmentRepository.reorder(1, segmentIds) }
    }
}
