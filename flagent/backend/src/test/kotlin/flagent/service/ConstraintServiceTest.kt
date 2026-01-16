package flagent.service

import flagent.domain.entity.Constraint
import flagent.domain.repository.IConstraintRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ConstraintServiceTest {
    private lateinit var constraintRepository: IConstraintRepository
    private lateinit var segmentRepository: flagent.domain.repository.ISegmentRepository
    private lateinit var constraintService: ConstraintService
    
    @BeforeTest
    fun setup() {
        constraintRepository = mockk()
        segmentRepository = mockk()
        constraintService = ConstraintService(constraintRepository, segmentRepository)
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
        val constraint = Constraint(segmentId = 1, property = "age", operator = "GT", value = "18")
        val createdConstraint = constraint.copy(id = 1)
        
        coEvery { constraintRepository.create(any()) } returns createdConstraint
        
        val result = constraintService.createConstraint(constraint)
        
        assertEquals(1, result.id)
        assertEquals("age", result.property)
        coVerify { constraintRepository.create(constraint) }
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
        val constraint = Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "21")
        
        coEvery { constraintRepository.update(any()) } returns constraint
        
        val result = constraintService.updateConstraint(constraint)
        
        assertEquals("21", result.value)
        coVerify { constraintRepository.update(constraint) }
    }
    
    @Test
    fun testDeleteConstraint() = runBlocking {
        coEvery { constraintRepository.delete(1) } just Runs
        
        constraintService.deleteConstraint(1)
        
        coVerify { constraintRepository.delete(1) }
    }
}
