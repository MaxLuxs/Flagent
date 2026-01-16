package flagent.service.mapper

import flagent.domain.entity.Variant
import flagent.service.dto.VariantDTO

/**
 * Mapper for Variant entity ↔ DTO
 */
object VariantMapper {
    fun toDTO(entity: Variant): VariantDTO {
        return VariantDTO(
            id = entity.id,
            flagId = entity.flagId,
            key = entity.key,
            attachment = entity.attachment
        )
    }
    
    fun toEntity(dto: VariantDTO): Variant {
        return Variant(
            id = dto.id,
            flagId = dto.flagId,
            key = dto.key,
            attachment = dto.attachment
        )
    }
}
