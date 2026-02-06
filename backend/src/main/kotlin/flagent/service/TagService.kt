package flagent.service

import flagent.domain.entity.Tag
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.ITagRepository
import flagent.service.command.CreateTagCommand

/**
 * Tag CRUD operations
 */
class TagService(
    private val tagRepository: ITagRepository,
    private val flagRepository: IFlagRepository,
    private val flagSnapshotService: FlagSnapshotService? = null
) {
    suspend fun findTagsByFlagId(flagId: Int): List<Tag> {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        
        return tagRepository.findByFlagId(flagId)
    }
    
    suspend fun findAllTags(limit: Int? = null, offset: Int = 0, valueLike: String? = null): List<Tag> {
        return tagRepository.findAll(limit, offset, valueLike)
    }
    
    suspend fun createTag(command: CreateTagCommand, updatedBy: String? = null): Tag {
        // Validate flag exists
        val flag = flagRepository.findById(command.flagId)
            ?: throw IllegalArgumentException("error finding flagID ${command.flagId}")
        
        // Validate tag value
        validateTagValue(command.value)
        
        // Find existing tag or create new one
        val tag = tagRepository.findByValue(command.value)
            ?: tagRepository.create(Tag(value = command.value))
        
        // Associate tag with flag
        tagRepository.addTagToFlag(command.flagId, tag.id)
        
        // Save flag snapshot after creating tag
        flagSnapshotService?.saveFlagSnapshot(command.flagId, updatedBy)
        
        return tag
    }
    
    suspend fun deleteTag(flagId: Int, tagId: Int, updatedBy: String? = null) {
        // Validate flag exists
        val flag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("error finding flagID $flagId")
        
        // Remove tag from flag (many-to-many relationship)
        tagRepository.removeTagFromFlag(flagId, tagId)
        
        // Save flag snapshot after deleting tag
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
    
    private fun validateTagValue(value: String) {
        if (value.isBlank()) {
            throw IllegalArgumentException("value cannot be empty")
        }
        
        // Value length limit: 63 characters
        if (value.length > 63) {
            throw IllegalArgumentException("value:$value cannot be longer than 63")
        }
        
        // Value format: ^[ \w\d-/\.\:]+$
        val valueRegex = Regex("^[ \\w\\d-/\\.:]+$")
        if (!valueRegex.matches(value)) {
            throw IllegalArgumentException("value:$value should have the format $valueRegex")
        }
    }
}
