package flagent.service.mapper

import flagent.domain.entity.Variant
import flagent.service.dto.VariantDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.*

class VariantMapperTest {
    @Test
    fun `toDTO converts Variant entity to VariantDTO`() {
        val entity = Variant(
            id = 1,
            flagId = 10,
            key = "control",
            attachment = null
        )
        
        val dto = VariantMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.flagId, dto.flagId)
        assertEquals(entity.key, dto.key)
        assertNull(dto.attachment)
    }
    
    @Test
    fun `toDTO converts Variant entity with attachment to VariantDTO`() {
        val attachment = buildJsonObject {
            put("color", "red")
            put("size", 42)
        }
        
        val entity = Variant(
            id = 2,
            flagId = 10,
            key = "treatment",
            attachment = attachment
        )
        
        val dto = VariantMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.flagId, dto.flagId)
        assertEquals(entity.key, dto.key)
        assertEquals(attachment, dto.attachment)
    }
    
    @Test
    fun `toEntity converts VariantDTO to Variant entity`() {
        val dto = VariantDTO(
            id = 1,
            flagId = 10,
            key = "control",
            attachment = null
        )
        
        val entity = VariantMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.flagId, entity.flagId)
        assertEquals(dto.key, entity.key)
        assertNull(entity.attachment)
    }
    
    @Test
    fun `toEntity converts VariantDTO with attachment to Variant entity`() {
        val attachment = buildJsonObject {
            put("enabled", true)
            put("message", "Hello World")
        }
        
        val dto = VariantDTO(
            id = 2,
            flagId = 10,
            key = "treatment",
            attachment = attachment
        )
        
        val entity = VariantMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.flagId, entity.flagId)
        assertEquals(dto.key, entity.key)
        assertEquals(attachment, entity.attachment)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val attachment = buildJsonObject {
            put("test", "value")
        }
        
        val originalEntity = Variant(
            id = 5,
            flagId = 20,
            key = "variant-key",
            attachment = attachment
        )
        
        val dto = VariantMapper.toDTO(originalEntity)
        val convertedEntity = VariantMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.flagId, convertedEntity.flagId)
        assertEquals(originalEntity.key, convertedEntity.key)
        assertEquals(originalEntity.attachment, convertedEntity.attachment)
    }
}
