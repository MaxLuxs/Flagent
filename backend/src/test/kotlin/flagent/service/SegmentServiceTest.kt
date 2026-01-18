package flagent.service

import flagent.domain.entity.Segment
import flagent.domain.repository.ISegmentRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SegmentServiceTest {
    private lateinit var segmentRepository: ISegmentRepository
    private lateinit var segmentService: SegmentService
    
    private lateinit var flagSnapshotService: FlagSnapshotService
    
    @BeforeTest
    fun setup() {
        segmentRepository = mockk()
        flagSnapshotService = mockk()
        segmentService = SegmentService(segmentRepository, flagSnapshotService)
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
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = segmentService.createSegment(segment)
        
        assertEquals(SegmentService.SegmentDefaultRank, result.rank)
        coVerify { segmentRepository.create(match { it.rank == SegmentService.SegmentDefaultRank }) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateSegment() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Updated segment", rank = 1, rolloutPercent = 75)
        
        coEvery { segmentRepository.update(any()) } returns segment
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = segmentService.updateSegment(segment)
        
        assertEquals("Updated segment", result.description)
        assertEquals(75, result.rolloutPercent)
        coVerify { segmentRepository.update(segment) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateSegment_WithUpdatedBy() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Updated segment", rank = 1, rolloutPercent = 75)
        
        coEvery { segmentRepository.update(any()) } returns segment
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = segmentService.updateSegment(segment, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testDeleteSegment() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { segmentRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        segmentService.deleteSegment(1)
        
        coVerify { segmentRepository.findById(1) }
        coVerify { segmentRepository.delete(1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testDeleteSegment_ThrowsException_WhenSegmentNotFound() = runBlocking {
        coEvery { segmentRepository.findById(999) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            segmentService.deleteSegment(999)
        }
        
        coVerify(exactly = 0) { segmentRepository.delete(any()) }
    }
    
    @Test
    fun testDeleteSegment_WithUpdatedBy() = runBlocking {
        val segment = Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { segmentRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        segmentService.deleteSegment(1, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testReorderSegments() = runBlocking {
        val segmentIds = listOf(3, 1, 2)
        
        coEvery { segmentRepository.reorder(1, segmentIds) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        segmentService.reorderSegments(1, segmentIds)
        
        coVerify { segmentRepository.reorder(1, segmentIds) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testReorderSegments_WithUpdatedBy() = runBlocking {
        val segmentIds = listOf(3, 1, 2)
        
        coEvery { segmentRepository.reorder(1, segmentIds) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        segmentService.reorderSegments(1, segmentIds, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testReorderSegments_WithEmptyList() = runBlocking {
        coEvery { segmentRepository.reorder(1, emptyList()) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        segmentService.reorderSegments(1, emptyList())
        
        coVerify { segmentRepository.reorder(1, emptyList()) }
    }
    
    @Test
    fun testCreateSegment_WithUpdatedBy() = runBlocking {
        val segment = Segment(id = 0, flagId = 1, description = "New segment", rank = 0, rolloutPercent = 100)
        val createdSegment = segment.copy(id = 1, rank = SegmentService.SegmentDefaultRank)
        
        coEvery { segmentRepository.create(any()) } returns createdSegment
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = segmentService.createSegment(segment, updatedBy = "test-user")
        
        assertEquals(1, result.id)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testFindSegmentsByFlagId_ReturnsEmptyList_WhenNoSegments() = runBlocking {
        coEvery { segmentRepository.findByFlagId(999) } returns emptyList()
        
        val result = segmentService.findSegmentsByFlagId(999)
        
        assertTrue(result.isEmpty())
    }
}
