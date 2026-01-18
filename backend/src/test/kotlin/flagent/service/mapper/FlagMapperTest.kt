package flagent.service.mapper

import flagent.domain.entity.*
import flagent.service.dto.FlagDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlagMapperTest {
    @Test
    fun `toDTO converts Flag entity to FlagDTO`() {
        val entity = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag description",
            createdBy = "user1",
            updatedBy = "user2",
            enabled = true,
            snapshotId = 5,
            notes = "Test notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = emptyList(),
            variants = emptyList(),
            tags = emptyList()
        )
        
        val dto = FlagMapper.toDTO(entity)
        
        assertEquals(entity.id, dto.id)
        assertEquals(entity.key, dto.key)
        assertEquals(entity.description, dto.description)
        assertEquals(entity.createdBy, dto.createdBy)
        assertEquals(entity.updatedBy, dto.updatedBy)
        assertEquals(entity.enabled, dto.enabled)
        assertEquals(entity.snapshotId, dto.snapshotId)
        assertEquals(entity.notes, dto.notes)
        assertEquals(entity.dataRecordsEnabled, dto.dataRecordsEnabled)
        assertEquals(entity.entityType, dto.entityType)
        assertEquals(0, dto.segments.size)
        assertEquals(0, dto.variants.size)
        assertEquals(0, dto.tags.size)
    }
    
    @Test
    fun `toDTO converts Flag entity with segments, variants and tags`() {
        val segments = listOf(
            Segment(
                id = 1,
                flagId = 1,
                description = "Segment 1",
                rank = 1,
                rolloutPercent = 50,
                constraints = emptyList(),
                distributions = emptyList()
            )
        )
        val variants = listOf(
            Variant(id = 1, flagId = 1, key = "control", attachment = null)
        )
        val tags = listOf(
            Tag(id = 1, value = "production")
        )
        
        val entity = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            segments = segments,
            variants = variants,
            tags = tags
        )
        
        val dto = FlagMapper.toDTO(entity)
        
        assertEquals(1, dto.segments.size)
        assertEquals(1, dto.variants.size)
        assertEquals(1, dto.tags.size)
        assertEquals("Segment 1", dto.segments[0].description)
        assertEquals("control", dto.variants[0].key)
        assertEquals("production", dto.tags[0].value)
    }
    
    @Test
    fun `toEntity converts FlagDTO to Flag entity`() {
        val dto = FlagDTO(
            id = 1,
            key = "test_flag",
            description = "Test flag description",
            createdBy = "user1",
            updatedBy = "user2",
            enabled = true,
            snapshotId = 5,
            notes = "Test notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = emptyList(),
            variants = emptyList(),
            tags = emptyList()
        )
        
        val entity = FlagMapper.toEntity(dto)
        
        assertEquals(dto.id, entity.id)
        assertEquals(dto.key, entity.key)
        assertEquals(dto.description, entity.description)
        assertEquals(dto.createdBy, entity.createdBy)
        assertEquals(dto.updatedBy, entity.updatedBy)
        assertEquals(dto.enabled, entity.enabled)
        assertEquals(dto.snapshotId, entity.snapshotId)
        assertEquals(dto.notes, entity.notes)
        assertEquals(dto.dataRecordsEnabled, entity.dataRecordsEnabled)
        assertEquals(dto.entityType, entity.entityType)
        assertEquals(0, entity.segments.size)
        assertEquals(0, entity.variants.size)
        assertEquals(0, entity.tags.size)
    }
    
    @Test
    fun `toDTO and toEntity are symmetric`() {
        val segments = listOf(
            Segment(
                id = 1,
                flagId = 1,
                description = "Segment 1",
                rank = 1,
                rolloutPercent = 50,
                constraints = emptyList(),
                distributions = emptyList()
            )
        )
        val variants = listOf(
            Variant(id = 1, flagId = 1, key = "control", attachment = null),
            Variant(id = 2, flagId = 1, key = "treatment", attachment = null)
        )
        val tags = listOf(
            Tag(id = 1, value = "production"),
            Tag(id = 2, value = "feature")
        )
        
        val originalEntity = Flag(
            id = 5,
            key = "feature_flag",
            description = "Feature flag description",
            createdBy = "admin",
            updatedBy = "admin",
            enabled = true,
            snapshotId = 10,
            notes = "Important notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = segments,
            variants = variants,
            tags = tags
        )
        
        val dto = FlagMapper.toDTO(originalEntity)
        val convertedEntity = FlagMapper.toEntity(dto)
        
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.key, convertedEntity.key)
        assertEquals(originalEntity.description, convertedEntity.description)
        assertEquals(originalEntity.createdBy, convertedEntity.createdBy)
        assertEquals(originalEntity.updatedBy, convertedEntity.updatedBy)
        assertEquals(originalEntity.enabled, convertedEntity.enabled)
        assertEquals(originalEntity.snapshotId, convertedEntity.snapshotId)
        assertEquals(originalEntity.notes, convertedEntity.notes)
        assertEquals(originalEntity.dataRecordsEnabled, convertedEntity.dataRecordsEnabled)
        assertEquals(originalEntity.entityType, convertedEntity.entityType)
        assertEquals(originalEntity.segments.size, convertedEntity.segments.size)
        assertEquals(originalEntity.variants.size, convertedEntity.variants.size)
        assertEquals(originalEntity.tags.size, convertedEntity.tags.size)
    }
    
    @Test
    fun `mapper handles flag with null optional fields`() {
        val entity = Flag(
            id = 1,
            key = "test_flag",
            description = "Test",
            createdBy = null,
            updatedBy = null,
            notes = null,
            entityType = null,
            segments = emptyList(),
            variants = emptyList(),
            tags = emptyList()
        )
        
        val dto = FlagMapper.toDTO(entity)
        val convertedEntity = FlagMapper.toEntity(dto)
        
        assertEquals(null, convertedEntity.createdBy)
        assertEquals(null, convertedEntity.updatedBy)
        assertEquals(null, convertedEntity.notes)
        assertEquals(null, convertedEntity.entityType)
    }
    
    @Test
    fun `mapper handles disabled flag`() {
        val entity = Flag(
            id = 1,
            key = "disabled_flag",
            description = "Disabled",
            enabled = false,
            dataRecordsEnabled = false,
            segments = emptyList(),
            variants = emptyList(),
            tags = emptyList()
        )
        
        val dto = FlagMapper.toDTO(entity)
        val convertedEntity = FlagMapper.toEntity(dto)
        
        assertFalse(convertedEntity.enabled)
        assertFalse(convertedEntity.dataRecordsEnabled)
    }
}
