package flagent.service

import flagent.domain.entity.FlagEntityType
import flagent.domain.repository.IFlagEntityTypeRepository

/**
 * FlagEntityType service - handles flag entity type business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class FlagEntityTypeService(
    private val flagEntityTypeRepository: IFlagEntityTypeRepository
) {
    suspend fun findAllEntityTypes(): List<String> {
        val entityTypes = flagEntityTypeRepository.findAll()
        return entityTypes.map { it.key }
    }
    
    /**
     * CreateFlagEntityType creates the FlagEntityType if not exists
     */
    suspend fun createOrGet(key: String): FlagEntityType {
        if (key.isEmpty()) {
            throw IllegalArgumentException("invalid DataRecordsEntityType. reason: key cannot be empty")
        }
        
        // Validate key format (same as flag key)
        val keyRegex = Regex("^[\\w\\d-/\\.:]+$")
        val keyLengthLimit = 63
        
        if (!keyRegex.matches(key)) {
            throw IllegalArgumentException("invalid DataRecordsEntityType. reason: key:$key should have the format $keyRegex")
        }
        if (key.length > keyLengthLimit) {
            throw IllegalArgumentException("invalid DataRecordsEntityType. reason: key:$key cannot be longer than $keyLengthLimit")
        }
        
        // Try to find existing
        val existing = flagEntityTypeRepository.findByKey(key)
        if (existing != null) {
            return existing
        }
        
        // Create new
        return flagEntityTypeRepository.create(FlagEntityType(key = key))
    }
}
