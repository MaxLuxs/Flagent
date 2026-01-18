package flagent.service.mapper

import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Segment
import flagent.service.dto.SegmentDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentMapperTest {
    @Test
    fun `toDTO converts Segment entity to SegmentDTO`() {
        val entity = Segment(
            id = 1,
            flagId = 10,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 50,
            constraints = emptyList(),
            distributions = emptyList()
        )
        
        val dto = SegmentMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.flagId, dto.flagId)
        assertEquals(entity.description, dto.description)
        assertEquals(entity.rank, dto.rank)
        assertEquals(entity.rolloutPercent, dto.rolloutPercent)
        assertEquals(0, dto.constraints.size)
        assertEquals(0, dto.distributions.size)
    }
    
    @Test
    fun `toDTO converts Segment entity with constraints and distributions`() {
        val constraints = listOf(
            Constraint(id = 1, segmentId = 1, property = "country", operator = "EQ", value = "US")
        )
        val distributions = listOf(
            Distribution(id = 1, segmentId = 1, variantId = 10, variantKey = "control", percent = 50)
        )
        
        val entity = Segment(
            id = 1,
            flagId = 10,
            description = "US segment",
            rank = 1,
            rolloutPercent = 50,
            constraints = constraints,
            distributions = distributions
        )
        
        val dto = SegmentMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.flagId, dto.flagId)
        assertEquals(1, dto.constraints.size)
        assertEquals(1, dto.distributions.size)
        assertEquals("country", dto.constraints[0].property)
        assertEquals("control", dto.distributions[0].variantKey)
    }
    
    @Test
    fun `toEntity converts SegmentDTO to Segment entity`() {
        val dto = SegmentDTO(
            id = 1,
            flagId = 10,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 50,
            constraints = emptyList(),
            distributions = emptyList()
        )
        
        val entity = SegmentMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.flagId, entity.flagId)
        assertEquals(dto.description, entity.description)
        assertEquals(dto.rank, entity.rank)
        assertEquals(dto.rolloutPercent, entity.rolloutPercent)
        assertEquals(0, entity.constraints.size)
        assertEquals(0, entity.distributions.size)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val constraints = listOf(
            Constraint(id = 1, segmentId = 1, property = "age", operator = "GT", value = "18")
        )
        val distributions = listOf(
            Distribution(id = 1, segmentId = 1, variantId = 10, variantKey = "treatment", percent = 100)
        )
        
        val originalEntity = Segment(
            id = 5,
            flagId = 20,
            description = "Adult segment",
            rank = 2,
            rolloutPercent = 75,
            constraints = constraints,
            distributions = distributions
        )
        
        val dto = SegmentMapper.toDTO(originalEntity)
        val convertedEntity = SegmentMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.flagId, convertedEntity.flagId)
        assertEquals(originalEntity.description, convertedEntity.description)
        assertEquals(originalEntity.rank, convertedEntity.rank)
        assertEquals(originalEntity.rolloutPercent, convertedEntity.rolloutPercent)
        assertEquals(originalEntity.constraints.size, convertedEntity.constraints.size)
        assertEquals(originalEntity.distributions.size, convertedEntity.distributions.size)
    }
    
    @Test
    fun `mapper handles segment with null description`() {
        val entity = Segment(
            id = 1,
            flagId = 10,
            description = null,
            rank = 1,
            rolloutPercent = 50,
            constraints = emptyList(),
            distributions = emptyList()
        )
        
        val dto = SegmentMapper.toDTO(entity)
        val convertedEntity = SegmentMapper.toEntity(dto)
        
        assertEquals(null, convertedEntity.description)
    }
}
