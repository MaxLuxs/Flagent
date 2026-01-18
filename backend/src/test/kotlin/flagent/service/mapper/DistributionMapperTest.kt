package flagent.service.mapper

import flagent.domain.entity.Distribution
import flagent.service.dto.DistributionDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class DistributionMapperTest {
    @Test
    fun `toDTO converts Distribution entity to DistributionDTO`() {
        val entity = Distribution(
            id = 1,
            segmentId = 10,
            variantId = 20,
            variantKey = "control",
            percent = 50
        )
        
        val dto = DistributionMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.segmentId, dto.segmentId)
        assertEquals(entity.variantId, dto.variantId)
        assertEquals(entity.variantKey, dto.variantKey)
        assertEquals(entity.percent, dto.percent)
    }
    
    @Test
    fun `toEntity converts DistributionDTO to Distribution entity`() {
        val dto = DistributionDTO(
            id = 1,
            segmentId = 10,
            variantId = 20,
            variantKey = "control",
            percent = 50
        )
        
        val entity = DistributionMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.segmentId, entity.segmentId)
        assertEquals(dto.variantId, entity.variantId)
        assertEquals(dto.variantKey, entity.variantKey)
        assertEquals(dto.percent, entity.percent)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val originalEntity = Distribution(
            id = 5,
            segmentId = 15,
            variantId = 25,
            variantKey = "treatment",
            percent = 75
        )
        
        val dto = DistributionMapper.toDTO(originalEntity)
        val convertedEntity = DistributionMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.segmentId, convertedEntity.segmentId)
        assertEquals(originalEntity.variantId, convertedEntity.variantId)
        assertEquals(originalEntity.variantKey, convertedEntity.variantKey)
        assertEquals(originalEntity.percent, convertedEntity.percent)
    }
    
    @Test
    fun `mapper handles different percent values`() {
        val percents = listOf(0, 25, 50, 75, 100)
        
        percents.forEach { percent ->
            val entity = Distribution(
                id = 1,
                segmentId = 10,
                variantId = 20,
                variantKey = "variant",
                percent = percent
            )
            
            val dto = DistributionMapper.toDTO(entity)
            val convertedEntity = DistributionMapper.toEntity(dto)
            
            assertEquals(percent, convertedEntity.percent)
        }
    }
}
