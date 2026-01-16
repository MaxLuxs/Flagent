package flagent.service.mapper

import flagent.domain.entity.Constraint
import flagent.service.dto.ConstraintDTO

/**
 * Mapper for Constraint entity ↔ DTO
 */
object ConstraintMapper {
    fun toDTO(entity: Constraint): ConstraintDTO {
        return ConstraintDTO(
            id = entity.id,
            segmentId = entity.segmentId,
            property = entity.property,
            operator = entity.operator,
            value = entity.value
        )
    }
    
    fun toEntity(dto: ConstraintDTO): Constraint {
        return Constraint(
            id = dto.id,
            segmentId = dto.segmentId,
            property = dto.property,
            operator = dto.operator,
            value = dto.value
        )
    }
}
