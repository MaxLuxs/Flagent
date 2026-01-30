package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IFlagRepository
import java.security.SecureRandom

/**
 * Flag service - handles flag business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class FlagService(
    private val flagRepository: IFlagRepository,
    private val flagSnapshotService: FlagSnapshotService? = null,
    private val segmentService: SegmentService? = null,
    private val variantService: VariantService? = null,
    private val distributionService: DistributionService? = null,
    private val flagEntityTypeService: FlagEntityTypeService? = null
) {
    /**
     * Create flag key - generates random key if not provided
     */
    fun createFlagKey(key: String?): String {
        return if (key.isNullOrBlank()) {
            generateSecureRandomKey()
        } else {
            validateKey(key)
            key
        }
    }
    
    private fun generateSecureRandomKey(): String {
        // Generate cryptographically secure random key (util.NewSecureRandomKey())
        // Format: "k" + random chars from charset
        val charset = "123456789abcdefghijkmnopqrstuvwxyz"
        val length = 16 // uniuri.StdLen
        val random = SecureRandom()
        val randomKey = (1..length).map { charset[random.nextInt(charset.length)] }.joinToString("")
        return "k$randomKey"
    }
    
    private fun validateKey(key: String): String {
        // Validate key format - matches util.IsSafeKey()
        // Format: ^[\w\d-/\.\:]+$ (word chars, digits, dash, slash, dot, colon)
        // Max length: 63
        val keyRegex = Regex("^[\\w\\d-/\\.:]+$")
        val keyLengthLimit = 63
        
        if (!keyRegex.matches(key)) {
            throw IllegalArgumentException("key:$key should have the format $keyRegex")
        }
        if (key.length > keyLengthLimit) {
            throw IllegalArgumentException("key:$key cannot be longer than $keyLengthLimit")
        }
        return key
    }
    
    suspend fun findFlags(
        limit: Int? = null,
        offset: Int = 0,
        enabled: Boolean? = null,
        description: String? = null,
        key: String? = null,
        descriptionLike: String? = null,
        preload: Boolean = false,
        deleted: Boolean = false,
        tags: String? = null
    ): List<Flag> {
        return flagRepository.findAll(
            limit = limit,
            offset = offset,
            enabled = enabled,
            description = description,
            key = key,
            descriptionLike = descriptionLike,
            preload = preload,
            deleted = deleted,
            tags = tags
        )
    }
    
    suspend fun getFlag(id: Int): Flag? {
        return flagRepository.findById(id)
    }
    
    suspend fun createFlag(flag: Flag, template: String? = null, updatedBy: String? = null): Flag {
        val key = createFlagKey(flag.key)
        val created = flagRepository.create(flag.copy(key = key))
        
        // Load template if specified
        if (template != null) {
            when (template) {
                "simple_boolean_flag" -> {
                    loadSimpleBooleanFlagTemplate(created, updatedBy)
                }
                else -> {
                    throw IllegalArgumentException("unknown value for template: $template")
                }
            }
        }
        
        // Save flag snapshot after creation
        flagSnapshotService?.saveFlagSnapshot(created.id, updatedBy)
        
        return created
    }
    
    /**
     * LoadSimpleBooleanFlagTemplate loads the simple boolean flag template into
     * a new flag. It creates a single segment, variant ('on'), and distribution.
     */
    private suspend fun loadSimpleBooleanFlagTemplate(flag: Flag, updatedBy: String?) {
        if (segmentService == null || variantService == null || distributionService == null) {
            throw IllegalStateException("SegmentService, VariantService, and DistributionService are required for template loading")
        }
        
        // Create default segment
        val segment = segmentService.createSegment(
            Segment(
                flagId = flag.id,
                description = null,
                rank = SegmentService.SegmentDefaultRank,
                rolloutPercent = 100
            ),
            updatedBy = updatedBy
        )
        
        // Create default variant
        val variant = variantService.createVariant(
            flagId = flag.id,
            variant = Variant(
                flagId = flag.id,
                key = "on",
                attachment = null
            ),
            updatedBy = updatedBy
        )
        
        // Create default distribution
        distributionService.updateDistributions(
            flagId = flag.id,
            segmentId = segment.id,
            distributions = listOf(
                Distribution(
                    segmentId = segment.id,
                    variantId = variant.id,
                    variantKey = variant.key,
                    percent = 100
                )
            ),
            updatedBy = updatedBy
        )
    }
    
    suspend fun updateFlag(flag: Flag, updatedBy: String? = null): Flag {
        val key = if (flag.key.isNotEmpty()) {
            validateKey(flag.key)
            flag.key
        } else {
            flag.key
        }
        
        // Create FlagEntityType if entityType is provided and not empty
        flag.entityType?.let { entityType ->
            if (entityType.isNotEmpty()) {
                // Create entity type if not exists (FirstOrCreate logic)
                flagEntityTypeService?.createOrGet(entityType)
            }
        }
        
        val updated = flagRepository.update(flag.copy(key = key, updatedBy = updatedBy))
        
        // Save flag snapshot after update
        flagSnapshotService?.saveFlagSnapshot(updated.id, updatedBy)
        
        return updated
    }
    
    suspend fun deleteFlag(id: Int) {
        flagRepository.delete(id)
    }
    
    suspend fun restoreFlag(id: Int): Flag? {
        return flagRepository.restore(id)
    }
    
    suspend fun setFlagEnabled(id: Int, enabled: Boolean, updatedBy: String? = null): Flag? {
        val flag = flagRepository.findById(id) ?: return null
        val updated = flagRepository.update(flag.copy(enabled = enabled, updatedBy = updatedBy))
        
        // Save flag snapshot after enabling/disabling
        flagSnapshotService?.saveFlagSnapshot(updated.id, updatedBy)
        
        return updated
    }
}
