package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IFlagEntityTypeRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IFlagSnapshotRepository
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * ExportService - handles SQLite export functionality
 * Maps to pkg/handler/export.go from original project
 */
class ExportService(
    private val flagRepository: IFlagRepository,
    private val flagSnapshotRepository: IFlagSnapshotRepository,
    private val flagEntityTypeRepository: IFlagEntityTypeRepository
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Export database to SQLite file
     * Creates temporary SQLite database, exports all data, returns file bytes
     */
    suspend fun exportSQLite(excludeSnapshots: Boolean = false): ByteArray = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("flagent_", ".sqlite")
        tempFile.deleteOnExit()
        
        try {
            // Create temporary SQLite database connection
            val tempDb = Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            // Create all tables in temporary database
            transaction(tempDb) {
                SchemaUtils.create(
                    Flags,
                    Segments,
                    Variants,
                    Constraints,
                    Distributions,
                    Tags,
                    FlagsTags,
                    FlagSnapshots,
                    FlagEntityTypes,
                    Users
                )
            }
            
            // Export all flags with related data
            exportFlags(tempDb)
            
            // Export snapshots if not excluded
            if (!excludeSnapshots) {
                exportFlagSnapshots(tempDb)
            }
            
            // Export entity types
            exportFlagEntityTypes(tempDb)
            
            // Read file and return bytes
            tempFile.readBytes()
        } finally {
            // Clean up temporary file
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
    
    private suspend fun exportFlags(tempDb: Database) = withContext(Dispatchers.IO) {
        val flags = flagRepository.findAll()
        
        newSuspendedTransaction(Dispatchers.IO, tempDb) {
            // First, collect all unique tags and insert them
            val allTags = flags.flatMap { it.tags }.distinctBy { it.id }
            allTags.forEach { tag ->
                Tags.insert {
                    it[id] = EntityID(tag.id, Tags)
                    it[value] = tag.value
                    it[createdAt] = java.time.Instant.now()
                }
            }
            
            // Export flags with all related data
            flags.forEach { flag ->
                // Insert flag
                Flags.insert {
                    it[id] = EntityID(flag.id, Flags)
                    it[key] = flag.key
                    it[description] = flag.description
                    it[createdBy] = flag.createdBy
                    it[updatedBy] = flag.updatedBy
                    it[enabled] = flag.enabled
                    it[snapshotId] = flag.snapshotId
                    it[notes] = flag.notes
                    it[dataRecordsEnabled] = flag.dataRecordsEnabled
                    it[entityType] = flag.entityType
                    it[createdAt] = java.time.Instant.now()
                    it[updatedAt] = null
                    it[deletedAt] = null
                }
                
                // Insert variants (must be after flags due to FK)
                flag.variants.forEach { variant ->
                    val attachmentJson = variant.attachment?.let { 
                        json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), it) 
                    }
                    Variants.insert {
                        it[id] = EntityID(variant.id, Variants)
                        it[flagId] = flag.id
                        it[key] = variant.key
                        it[attachment] = attachmentJson
                        it[createdAt] = java.time.Instant.now()
                        it[updatedAt] = null
                        it[deletedAt] = null
                    }
                }
                
                // Insert segments (must be after flags due to FK)
                flag.segments.forEach { segment ->
                    Segments.insert {
                        it[id] = EntityID(segment.id, Segments)
                        it[flagId] = flag.id
                        it[description] = segment.description
                        it[rank] = segment.rank
                        it[rolloutPercent] = segment.rolloutPercent
                        it[createdAt] = java.time.Instant.now()
                        it[updatedAt] = null
                        it[deletedAt] = null
                    }
                }
            }
            
            // Insert constraints (must be after segments due to FK)
            flags.forEach { flag ->
                flag.segments.forEach { segment ->
                    segment.constraints.forEach { constraint ->
                        Constraints.insert {
                            it[id] = EntityID(constraint.id, Constraints)
                            it[segmentId] = segment.id
                            it[property] = constraint.property
                            it[operator] = constraint.operator
                            it[value] = constraint.value
                            it[createdAt] = java.time.Instant.now()
                            it[updatedAt] = null
                            it[deletedAt] = null
                        }
                    }
                }
            }
            
            // Insert distributions (must be after segments and variants due to FK)
            flags.forEach { flag ->
                flag.segments.forEach { segment ->
                    segment.distributions.forEach { distribution ->
                        Distributions.insert {
                            it[id] = EntityID(distribution.id, Distributions)
                            it[segmentId] = segment.id
                            it[variantId] = distribution.variantId
                            it[variantKey] = distribution.variantKey
                            it[percent] = distribution.percent
                            it[createdAt] = java.time.Instant.now()
                            it[updatedAt] = null
                            it[deletedAt] = null
                        }
                    }
                }
            }
            
            // Insert flags_tags junction table (must be after flags and tags due to FK)
            flags.forEach { flag ->
                flag.tags.forEach { tag ->
                    FlagsTags.insert {
                        it[flagId] = flag.id
                        it[tagId] = tag.id
                    }
                }
            }
            
            logger.debug { "Exported ${flags.size} flags" }
        }
    }
    
    private suspend fun exportFlagSnapshots(tempDb: Database) = withContext(Dispatchers.IO) {
        val snapshots = flagSnapshotRepository.findAll()
        
        newSuspendedTransaction(Dispatchers.IO, tempDb) {
            snapshots.forEach { snapshot ->
                FlagSnapshots.insert {
                    it[id] = EntityID(snapshot.id, FlagSnapshots)
                    it[FlagSnapshots.flagId] = snapshot.flagId
                    it[FlagSnapshots.updatedBy] = snapshot.updatedBy
                    it[FlagSnapshots.flag] = snapshot.flag
                    it[FlagSnapshots.createdAt] = java.time.Instant.now()
                    it[FlagSnapshots.updatedAt] = null
                    it[FlagSnapshots.deletedAt] = null
                }
            }
            
            logger.debug { "Exported ${snapshots.size} flag snapshots" }
        }
    }
    
    private suspend fun exportFlagEntityTypes(tempDb: Database) = withContext(Dispatchers.IO) {
        val entityTypes = flagEntityTypeRepository.findAll()
        
        newSuspendedTransaction(Dispatchers.IO, tempDb) {
            entityTypes.forEach { entityType ->
                FlagEntityTypes.insert {
                    it[id] = EntityID(entityType.id, FlagEntityTypes)
                    it[FlagEntityTypes.key] = entityType.key
                    it[FlagEntityTypes.createdAt] = java.time.Instant.now()
                    it[FlagEntityTypes.updatedAt] = null
                    it[FlagEntityTypes.deletedAt] = null
                }
            }
            
            logger.debug { "Exported ${entityTypes.size} flag entity types" }
        }
    }
}
