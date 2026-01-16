package flagent.service.mapper

import flagent.domain.entity.Flag
import flagent.service.dto.FlagDTO

/**
 * Mapper for Flag entity ↔ DTO
 */
object FlagMapper {
    fun toDTO(entity: Flag): FlagDTO {
        return FlagDTO(
            id = entity.id,
            key = entity.key,
            description = entity.description,
            createdBy = entity.createdBy,
            updatedBy = entity.updatedBy,
            enabled = entity.enabled,
            snapshotId = entity.snapshotId,
            notes = entity.notes,
            dataRecordsEnabled = entity.dataRecordsEnabled,
            entityType = entity.entityType,
            segments = entity.segments.map { SegmentMapper.toDTO(it) },
            variants = entity.variants.map { VariantMapper.toDTO(it) },
            tags = entity.tags.map { TagMapper.toDTO(it) }
        )
    }
    
    fun toEntity(dto: FlagDTO): Flag {
        return Flag(
            id = dto.id,
            key = dto.key,
            description = dto.description,
            createdBy = dto.createdBy,
            updatedBy = dto.updatedBy,
            enabled = dto.enabled,
            snapshotId = dto.snapshotId,
            notes = dto.notes,
            dataRecordsEnabled = dto.dataRecordsEnabled,
            entityType = dto.entityType,
            segments = dto.segments.map { SegmentMapper.toEntity(it) },
            variants = dto.variants.map { VariantMapper.toEntity(it) },
            tags = dto.tags.map { TagMapper.toEntity(it) }
        )
    }
}
