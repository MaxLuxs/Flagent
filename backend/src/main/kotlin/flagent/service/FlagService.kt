package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IFlagRepository
import flagent.route.RealtimeEventBus
import flagent.service.command.CreateFlagCommand
import flagent.service.command.CreateSegmentCommand
import flagent.service.command.CreateVariantCommand
import flagent.service.command.DistributionItemCommand
import flagent.service.command.PutDistributionsCommand
import flagent.service.command.PutFlagCommand
import java.security.SecureRandom

/**
 * Flag CRUD operations
 */
class FlagService(
    private val flagRepository: IFlagRepository,
    private val flagSnapshotService: FlagSnapshotService? = null,
    private val segmentService: SegmentService? = null,
    private val variantService: VariantService? = null,
    private val distributionService: DistributionService? = null,
    private val flagEntityTypeService: FlagEntityTypeService? = null,
    private val eventBus: RealtimeEventBus? = null,
    private val webhookService: WebhookService? = null
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

    suspend fun countFlags(
        enabled: Boolean? = null,
        description: String? = null,
        key: String? = null,
        descriptionLike: String? = null,
        deleted: Boolean = false,
        tags: String? = null
    ): Long {
        return flagRepository.countAll(
            enabled = enabled,
            description = description,
            key = key,
            descriptionLike = descriptionLike,
            deleted = deleted,
            tags = tags
        )
    }
    
    suspend fun getFlag(id: Int): Flag? {
        return flagRepository.findById(id)
    }
    
    suspend fun createFlag(command: CreateFlagCommand, updatedBy: String? = null): Flag {
        val key = createFlagKey(command.key)
        val flag = Flag(
            key = key,
            description = command.description,
            enabled = false,
            environmentId = command.environmentId
        )
        val created = flagRepository.create(flag)
        
        // Load template if specified
        if (command.template != null) {
            when (command.template) {
                "simple_boolean_flag" -> {
                    loadSimpleBooleanFlagTemplate(created, updatedBy)
                }
                else -> {
                    throw IllegalArgumentException("unknown value for template: ${command.template}")
                }
            }
        }
        
        // Save flag snapshot after creation
        flagSnapshotService?.saveFlagSnapshot(created.id, updatedBy)
        eventBus?.publishFlagCreated(created.id.toLong(), created.key)
        webhookService?.dispatchFlagCreated(created.id)

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
            CreateSegmentCommand(
                flagId = flag.id,
                description = "",
                rolloutPercent = 100
            ),
            updatedBy = updatedBy
        )
        
        // Create default variant
        val variant = variantService.createVariant(
            CreateVariantCommand(
                flagId = flag.id,
                key = "on",
                attachment = null
            ),
            updatedBy = updatedBy
        )
        
        // Create default distribution
        distributionService.updateDistributions(
            PutDistributionsCommand(
                flagId = flag.id,
                segmentId = segment.id,
                distributions = listOf(
                    DistributionItemCommand(
                        variantID = variant.id,
                        variantKey = variant.key,
                        percent = 100
                    )
                )
            ),
            updatedBy = updatedBy
        )
    }
    
    suspend fun updateFlag(flagId: Int, command: PutFlagCommand, updatedBy: String? = null): Flag {
        val existingFlag = flagRepository.findById(flagId)
            ?: throw IllegalArgumentException("Flag not found: $flagId")
        
        val key = if ((command.key ?: existingFlag.key).isNotEmpty()) {
            validateKey(command.key ?: existingFlag.key)
            command.key ?: existingFlag.key
        } else {
            existingFlag.key
        }
        
        val updatedFlag = existingFlag.copy(
            description = command.description ?: existingFlag.description,
            key = key,
            dataRecordsEnabled = command.dataRecordsEnabled ?: existingFlag.dataRecordsEnabled,
            entityType = command.entityType ?: existingFlag.entityType,
            environmentId = command.environmentId ?: existingFlag.environmentId,
            notes = command.notes ?: existingFlag.notes,
            updatedBy = updatedBy
        )
        
        // Create FlagEntityType if entityType is provided and not empty
        updatedFlag.entityType?.let { entityType ->
            if (entityType.isNotEmpty()) {
                // Create entity type if not exists (FirstOrCreate logic)
                flagEntityTypeService?.createOrGet(entityType)
            }
        }
        
        val updated = flagRepository.update(updatedFlag)

        // Save flag snapshot after update
        flagSnapshotService?.saveFlagSnapshot(updated.id, updatedBy)
        eventBus?.publishFlagUpdated(updated.id.toLong(), updated.key)
        webhookService?.dispatchFlagUpdated(updated.id)

        return updated
    }
    
    suspend fun deleteFlag(id: Int) {
        val flag = flagRepository.findById(id)
        flagRepository.delete(id)
        flag?.let {
            eventBus?.publishFlagDeleted(it.id.toLong(), it.key)
            webhookService?.dispatchFlagDeleted(it.id, it.key)
        }
    }
    
    suspend fun restoreFlag(id: Int): Flag? {
        return flagRepository.restore(id)
    }
    
    suspend fun setFlagEnabled(id: Int, enabled: Boolean, updatedBy: String? = null): Flag? {
        val flag = flagRepository.findById(id) ?: return null
        val updated = flagRepository.update(flag.copy(enabled = enabled, updatedBy = updatedBy))

        // Save flag snapshot after enabling/disabling
        flagSnapshotService?.saveFlagSnapshot(updated.id, updatedBy)
        eventBus?.publishFlagToggled(updated.id.toLong(), updated.key, enabled)
        webhookService?.dispatchFlagToggled(updated.id, enabled)

        return updated
    }

    suspend fun batchSetEnabled(ids: List<Int>, enabled: Boolean, updatedBy: String? = null): List<Flag> {
        return ids.mapNotNull { id -> setFlagEnabled(id, enabled, updatedBy) }
    }
}
