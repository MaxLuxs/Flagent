package flagent.service

import flagent.domain.entity.Constraint
import flagent.domain.repository.IConstraintRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ConstraintServiceTest {
    private lateinit var constraintRepository: IConstraintRepository
    private lateinit var segmentRepository: flagent.domain.repository.ISegmentRepository
    private lateinit var flagSnapshotService: FlagSnapshotService
    private lateinit var constraintService: ConstraintService
    
    @BeforeTest
    fun setup() {
        constraintRepository = mockk()
        segmentRepository = mockk()
        flagSnapshotService = mockk()
        constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
    }
    
    @Test
    fun testFindConstraintsBySegmentId() = runBlocking {
        val constraints = listOf(
            Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18"),
            Constraint(id = 2, segmentId = 1, property = "country", operator = "EQ", value = "US")
        )
        
        coEvery { constraintRepository.findBySegmentId(1) } returns constraints
        
        val result = constraintService.findConstraintsBySegmentId(1)
        
        assertEquals(2, result.size)
        coVerify { constraintRepository.findBySegmentId(1) }
    }
    
    @Test
    fun testGetConstraint() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18")
        
        coEvery { constraintRepository.findById(1) } returns constraint
        
        val result = constraintService.getConstraint(1)
        
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("age", result.property)
        coVerify { constraintRepository.findById(1) }
    }
    
    @Test
    fun testGetConstraint_ReturnsNull_WhenNotFound() = runBlocking {
        coEvery { constraintRepository.findById(999) } returns null
        
        val result = constraintService.getConstraint(999)
        
        assertNull(result)
        coVerify { constraintRepository.findById(999) }
    }
    
    @Test
    fun testCreateConstraint_ThrowsException_WhenInvalid() = runBlocking {
        val constraint = Constraint(segmentId = 1, property = "", operator = "GT", value = "18")
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.createConstraint(constraint) }
        }
        
        coVerify(exactly = 0) { constraintRepository.create(any()) }
    }
    
    @Test
    fun testCreateConstraint_Success() = runBlocking {
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        val constraint = Constraint(segmentId = 1, property = "age", operator = "GT", value = "18")
        val createdConstraint = constraint.copy(id = 1)
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.create(any()) } returns createdConstraint
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = constraintService.createConstraint(constraint)
        
        assertEquals(1, result.id)
        assertEquals("age", result.property)
        coVerify { constraintRepository.create(constraint) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testCreateConstraint_ThrowsException_WhenSegmentNotFound() = runBlocking {
        val constraint = Constraint(segmentId = 1, property = "age", operator = "GT", value = "18")
        
        coEvery { segmentRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.createConstraint(constraint) }
        }
        
        coVerify(exactly = 0) { constraintRepository.create(any()) }
    }
    
    @Test
    fun testCreateConstraint_WithUpdatedBy() = runBlocking {
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        val constraint = Constraint(segmentId = 1, property = "age", operator = "GT", value = "18")
        val createdConstraint = constraint.copy(id = 1)
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.create(any()) } returns createdConstraint
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = constraintService.createConstraint(constraint, updatedBy = "test-user")
        
        assertEquals(1, result.id)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testUpdateConstraint_ThrowsException_WhenInvalid() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "INVALID", value = "18")
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.updateConstraint(constraint) }
        }
        
        coVerify(exactly = 0) { constraintRepository.update(any()) }
    }
    
    @Test
    fun testUpdateConstraint_Success() = runBlocking {
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "21")
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.update(any()) } returns constraint
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = constraintService.updateConstraint(constraint)
        
        assertEquals("21", result.value)
        coVerify { constraintRepository.update(constraint) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testUpdateConstraint_ThrowsException_WhenSegmentNotFound() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "21")
        
        coEvery { segmentRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.updateConstraint(constraint) }
        }
        
        coVerify(exactly = 0) { constraintRepository.update(any()) }
    }
    
    @Test
    fun testUpdateConstraint_WithUpdatedBy() = runBlocking {
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "21")
        
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.update(any()) } returns constraint
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = constraintService.updateConstraint(constraint, updatedBy = "test-user")
        
        assertEquals("21", result.value)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testDeleteConstraint() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18")
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        
        coEvery { constraintRepository.findById(1) } returns constraint
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        constraintService.deleteConstraint(1)
        
        coVerify { constraintRepository.findById(1) }
        coVerify { segmentRepository.findById(1) }
        coVerify { constraintRepository.delete(1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testDeleteConstraint_ThrowsException_WhenConstraintNotFound() = runBlocking {
        coEvery { constraintRepository.findById(999) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.deleteConstraint(999) }
        }
        
        coVerify(exactly = 0) { constraintRepository.delete(any()) }
    }
    
    @Test
    fun testDeleteConstraint_ThrowsException_WhenSegmentNotFound() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18")
        
        coEvery { constraintRepository.findById(1) } returns constraint
        coEvery { segmentRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { constraintService.deleteConstraint(1) }
        }
        
        coVerify(exactly = 0) { constraintRepository.delete(any()) }
    }
    
    @Test
    fun testDeleteConstraint_WithUpdatedBy() = runBlocking {
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18")
        val segment = flagent.domain.entity.Segment(id = 1, flagId = 1, description = "Test segment", rank = 1, rolloutPercent = 100)
        
        coEvery { constraintRepository.findById(1) } returns constraint
        coEvery { segmentRepository.findById(1) } returns segment
        coEvery { constraintRepository.delete(1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        constraintService.deleteConstraint(1, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
}
