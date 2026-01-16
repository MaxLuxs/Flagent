package flagent.service.mapper

import flagent.domain.entity.Tag
import flagent.service.dto.TagDTO

/**
 * Mapper for Tag entity ↔ DTO
 */
object TagMapper {
    fun toDTO(entity: Tag): TagDTO {
        return TagDTO(
            id = entity.id,
            value = entity.value
        )
    }
    
    fun toEntity(dto: TagDTO): Tag {
        return Tag(
            id = dto.id,
            value = dto.value
        )
    }
}
