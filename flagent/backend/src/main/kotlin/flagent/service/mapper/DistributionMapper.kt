package flagent.service.mapper

import flagent.domain.entity.Distribution
import flagent.service.dto.DistributionDTO

/**
 * Mapper for Distribution entity ↔ DTO
 */
object DistributionMapper {
    fun toDTO(entity: Distribution): DistributionDTO {
        return DistributionDTO(
            id = entity.id,
            segmentId = entity.segmentId,
            variantId = entity.variantId,
            variantKey = entity.variantKey,
            percent = entity.percent
        )
    }
    
    fun toEntity(dto: DistributionDTO): Distribution {
        return Distribution(
            id = dto.id,
            segmentId = dto.segmentId,
            variantId = dto.variantId,
            variantKey = dto.variantKey,
            percent = dto.percent
        )
    }
}
