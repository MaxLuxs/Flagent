package flagent.service.mapper

import flagent.domain.entity.Constraint
import flagent.service.dto.ConstraintDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstraintMapperTest {
    @Test
    fun `toDTO converts Constraint entity to ConstraintDTO`() {
        val entity = Constraint(
            id = 1,
            segmentId = 10,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        
        val dto = ConstraintMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.segmentId, dto.segmentId)
        assertEquals(entity.property, dto.property)
        assertEquals(entity.operator, dto.operator)
        assertEquals(entity.value, dto.value)
    }
    
    @Test
    fun `toEntity converts ConstraintDTO to Constraint entity`() {
        val dto = ConstraintDTO(
            id = 1,
            segmentId = 10,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        
        val entity = ConstraintMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.segmentId, entity.segmentId)
        assertEquals(dto.property, entity.property)
        assertEquals(dto.operator, entity.operator)
        assertEquals(dto.value, entity.value)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val originalEntity = Constraint(
            id = 5,
            segmentId = 15,
            property = "age",
            operator = "GT",
            value = "18"
        )
        
        val dto = ConstraintMapper.toDTO(originalEntity)
        val convertedEntity = ConstraintMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.segmentId, convertedEntity.segmentId)
        assertEquals(originalEntity.property, convertedEntity.property)
        assertEquals(originalEntity.operator, convertedEntity.operator)
        assertEquals(originalEntity.value, convertedEntity.value)
    }
    
    @Test
    fun `mapper handles different operators`() {
        val operators = listOf("EQ", "NEQ", "GT", "GTE", "LT", "LTE", "IN", "NOT_IN", "CONTAINS")
        
        operators.forEach { operator ->
            val entity = Constraint(
                id = 1,
                segmentId = 10,
                property = "test",
                operator = operator,
                value = "value"
            )
            
            val dto = ConstraintMapper.toDTO(entity)
            val convertedEntity = ConstraintMapper.toEntity(dto)
            
            assertEquals(operator, convertedEntity.operator)
        }
    }
}
