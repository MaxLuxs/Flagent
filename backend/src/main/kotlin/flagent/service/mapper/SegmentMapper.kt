package flagent.service.mapper

import flagent.domain.entity.Segment
import flagent.service.dto.SegmentDTO

/**
 * Mapper for Segment entity ↔ DTO
 */
object SegmentMapper {
    fun toDTO(entity: Segment): SegmentDTO {
        return SegmentDTO(
            id = entity.id,
            flagId = entity.flagId,
            description = entity.description,
            rank = entity.rank,
            rolloutPercent = entity.rolloutPercent,
            constraints = entity.constraints.map { ConstraintMapper.toDTO(it) },
            distributions = entity.distributions.map { DistributionMapper.toDTO(it) }
        )
    }
    
    fun toEntity(dto: SegmentDTO): Segment {
        return Segment(
            id = dto.id,
            flagId = dto.flagId,
            description = dto.description,
            rank = dto.rank,
            rolloutPercent = dto.rolloutPercent,
            constraints = dto.constraints.map { ConstraintMapper.toEntity(it) },
            distributions = dto.distributions.map { DistributionMapper.toEntity(it) }
        )
    }
}
