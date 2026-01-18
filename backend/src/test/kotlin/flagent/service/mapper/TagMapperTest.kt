package flagent.service.mapper

import flagent.domain.entity.Tag
import flagent.service.dto.TagDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class TagMapperTest {
    @Test
    fun `toDTO converts Tag entity to TagDTO`() {
        val entity = Tag(
            id = 1,
            value = "production"
        )
        
        val dto = TagMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.value, dto.value)
    }
    
    @Test
    fun `toEntity converts TagDTO to Tag entity`() {
        val dto = TagDTO(
            id = 1,
            value = "production"
        )
        
        val entity = TagMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.value, entity.value)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val originalEntity = Tag(
            id = 5,
            value = "test-tag"
        )
        
        val dto = TagMapper.toDTO(originalEntity)
        val convertedEntity = TagMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.value, convertedEntity.value)
    }
}
