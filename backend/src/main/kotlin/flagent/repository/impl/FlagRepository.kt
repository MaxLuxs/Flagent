package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.domain.entity.Tag
import flagent.domain.entity.Variant
import flagent.domain.repository.IFlagRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Flag repository implementation
 * Infrastructure layer - uses Exposed
 */
class FlagRepository : IFlagRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun findById(id: Int): Flag? = withContext(Dispatchers.IO) {
        Database.transaction {
            Flags.select { Flags.id eq id }
                .firstOrNull()
                ?.let { row -> mapRowToFlag(row) }
        }
    }
    
    override suspend fun findByKey(key: String): Flag? = withContext(Dispatchers.IO) {
        Database.transaction {
            Flags.select { Flags.key eq key }
                .firstOrNull()
                ?.let { row -> mapRowToFlag(row) }
        }
    }
    
    override suspend fun findAll(
        limit: Int?,
        offset: Int,
        enabled: Boolean?,
        description: String?,
        key: String?,
        descriptionLike: String?,
        preload: Boolean,
        deleted: Boolean,
        tags: String?
    ): List<Flag> = withContext(Dispatchers.IO) {
        Database.transaction {
            // Use selectAll() - Exposed doesn't automatically filter deleted records
            // We explicitly filter by deletedAt below
            var query = Flags.selectAll()
            
            // Filter by enabled
            enabled?.let {
                query = query.andWhere { Flags.enabled eq it }
            }
            
            // Filter by exact description match
            description?.let {
                query = query.andWhere { Flags.description eq it }
            }
            
            // Filter by exact key match
            key?.let {
                query = query.andWhere { Flags.key eq it }
            }
            
            // Filter by description LIKE (case-insensitive)
            descriptionLike?.let {
                val pattern = "%${it.lowercase()}%"
                query = query.andWhere {
                    Flags.description.lowerCase() like pattern
                }
            }
            
            // Filter by deleted status
            if (deleted) {
                query = query.andWhere { Flags.deletedAt.isNotNull() }
            } else {
                query = query.andWhere { Flags.deletedAt.isNull() }
            }
            
            // Order by id
            query = query.orderBy(Flags.id, SortOrder.ASC)
            
            // Apply limit and offset
            limit?.let {
                query = query.limit(it, offset.toLong())
            } ?: run {
                if (offset > 0) {
                    query = query.limit(Int.MAX_VALUE, offset.toLong())
                }
            }
            
            // Handle tags filter
            val flagIds = if (tags != null) {
                val tagValues = tags.split(",").map { it.trim() }
                val tagIds = Tags.select { Tags.value inList tagValues }
                    .map { it[Tags.id].value }
                
                if (tagIds.isEmpty()) {
                    return@transaction emptyList()
                }
                
                FlagsTags
                    .slice(FlagsTags.flagId)
                    .select { FlagsTags.tagId inList tagIds }
                    .map { it[FlagsTags.flagId] }
                    .distinct()
                    .toSet()
            } else {
                null
            }
            
            // Apply tag filter if specified
            val finalQuery = if (flagIds != null) {
                query.andWhere { Flags.id inList flagIds }
            } else {
                query
            }
            
            // Map rows to flags
            finalQuery.map { row ->
                val flagId = row[Flags.id].value
                if (preload) {
                    // Preload segments, variants, and tags
                    mapRowToFlagWithPreload(row)
                } else {
                    // Only preload tags (for searchability as per original)
                    mapRowToFlagWithTagsOnly(row)
                }
            }
        }
    }
    
    /**
     * Map row to flag with full preload (segments, variants, tags)
     */
    private fun mapRowToFlagWithPreload(row: ResultRow): Flag {
        return mapRowToFlag(row)
    }
    
    /**
     * Map row to flag with tags only preload
     */
    private fun mapRowToFlagWithTagsOnly(row: ResultRow): Flag {
        val flagId = row[Flags.id].value
        
        // Only load tags (filter out deleted tags)
        val tags = Tags
            .innerJoin(FlagsTags)
            .select { 
                (FlagsTags.flagId eq flagId) and (Tags.deletedAt.isNull())
            }
            .orderBy(Tags.id, SortOrder.ASC)
            .map { mapRowToTag(it) }
        
        return Flag(
            id = flagId,
            key = row[Flags.key],
            description = row[Flags.description],
            createdBy = row[Flags.createdBy],
            updatedBy = row[Flags.updatedBy],
            enabled = row[Flags.enabled],
            snapshotId = row[Flags.snapshotId],
            notes = row[Flags.notes],
            dataRecordsEnabled = row[Flags.dataRecordsEnabled],
            entityType = row[Flags.entityType],
            segments = emptyList(), // Not preloaded
            variants = emptyList(), // Not preloaded
            tags = tags,
            updatedAt = row[Flags.updatedAt]?.toString()
        )
    }
    
    override suspend fun findByTags(tags: List<String>): List<Flag> = withContext(Dispatchers.IO) {
        Database.transaction {
            val tagIds = Tags.select { Tags.value inList tags }
                .map { it[Tags.id].value }
            
            if (tagIds.isEmpty()) return@transaction emptyList()
            
            FlagsTags
                .slice(FlagsTags.flagId)
                .select { FlagsTags.tagId inList tagIds }
                .map { it[FlagsTags.flagId] }
                .distinct()
                .mapNotNull { flagId -> findById(flagId) }
        }
    }
    
    override suspend fun create(flag: Flag): Flag = withContext(Dispatchers.IO) {
        Database.transaction {
            val id = Flags.insert {
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
            }[Flags.id].value
            
            flag.copy(id = id)
        }
    }
    
    override suspend fun update(flag: Flag): Flag = withContext(Dispatchers.IO) {
        Database.transaction {
            Flags.update({ Flags.id eq flag.id }) {
                it[key] = flag.key
                it[description] = flag.description
                it[updatedBy] = flag.updatedBy
                it[enabled] = flag.enabled
                it[snapshotId] = flag.snapshotId
                it[notes] = flag.notes
                it[dataRecordsEnabled] = flag.dataRecordsEnabled
                it[entityType] = flag.entityType
                it[updatedAt] = java.time.Instant.now()
            }
            
            flag
        }
    }
    
    override suspend fun delete(id: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Flags.update({ Flags.id eq id }) {
                it[deletedAt] = java.time.Instant.now()
            }
        }
        Unit
    }
    
    override suspend fun restore(id: Int): Flag? = withContext(Dispatchers.IO) {
        Database.transaction {
            Flags.update({ Flags.id eq id }) {
                it[deletedAt] = null
            }
            findById(id)
        }
    }
    
    private fun mapRowToFlag(row: ResultRow): Flag {
        val flagId = row[Flags.id].value
        
        // Load related entities (ordered by rank, then id as in original)
        // Filter out deleted segments, variants, and tags
        val segments = Segments.select { 
            (Segments.flagId eq flagId) and (Segments.deletedAt.isNull())
        }
            .orderBy(Segments.rank, SortOrder.ASC)
            .orderBy(Segments.id, SortOrder.ASC)
            .map { mapRowToSegment(it) }
        
        val variants = Variants.select { 
            (Variants.flagId eq flagId) and (Variants.deletedAt.isNull())
        }
            .orderBy(Variants.id, SortOrder.ASC)
            .map { mapRowToVariant(it) }
        
        val tags = Tags
            .innerJoin(FlagsTags)
            .select { 
                (FlagsTags.flagId eq flagId) and (Tags.deletedAt.isNull())
            }
            .orderBy(Tags.id, SortOrder.ASC)
            .map { mapRowToTag(it) }
        
        return Flag(
            id = flagId,
            key = row[Flags.key],
            description = row[Flags.description],
            createdBy = row[Flags.createdBy],
            updatedBy = row[Flags.updatedBy],
            enabled = row[Flags.enabled],
            snapshotId = row[Flags.snapshotId],
            notes = row[Flags.notes],
            dataRecordsEnabled = row[Flags.dataRecordsEnabled],
            entityType = row[Flags.entityType],
            segments = segments,
            variants = variants,
            tags = tags,
            updatedAt = row[Flags.updatedAt]?.toString()
        )
    }
    
    private fun mapRowToSegment(row: ResultRow): Segment {
        val segmentId = row[Segments.id].value
        
        val constraints = Constraints.select { Constraints.segmentId eq segmentId }
            .orderBy(Constraints.createdAt, SortOrder.ASC)
            .map { mapRowToConstraint(it) }
        
        val distributions = Distributions.select { Distributions.segmentId eq segmentId }
            .orderBy(Distributions.variantId, SortOrder.ASC)
            .map { mapRowToDistribution(it) }
        
        return Segment(
            id = segmentId,
            flagId = row[Segments.flagId],
            description = row[Segments.description],
            rank = row[Segments.rank],
            rolloutPercent = row[Segments.rolloutPercent],
            constraints = constraints,
            distributions = distributions
        )
    }
    
    private fun mapRowToVariant(row: ResultRow): Variant {
        val attachmentJson = row[Variants.attachment]
        val attachment = attachmentJson?.let { 
            try {
                json.parseToJsonElement(it) as? kotlinx.serialization.json.JsonObject
            } catch (e: Exception) {
                null
            }
        }
        
        return Variant(
            id = row[Variants.id].value,
            flagId = row[Variants.flagId],
            key = row[Variants.key] ?: "",
            attachment = attachment
        )
    }
    
    private fun mapRowToConstraint(row: ResultRow) = flagent.domain.entity.Constraint(
        id = row[Constraints.id].value,
        segmentId = row[Constraints.segmentId],
        property = row[Constraints.property],
        operator = row[Constraints.operator],
        value = row[Constraints.value]
    )
    
    private fun mapRowToDistribution(row: ResultRow) = flagent.domain.entity.Distribution(
        id = row[Distributions.id].value,
        segmentId = row[Distributions.segmentId],
        variantId = row[Distributions.variantId],
        variantKey = row[Distributions.variantKey],
        percent = row[Distributions.percent]
    )
    
    private fun mapRowToTag(row: ResultRow) = Tag(
        id = row[Tags.id].value,
        value = row[Tags.value]
    )
}
