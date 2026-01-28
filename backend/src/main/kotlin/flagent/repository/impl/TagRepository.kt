package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Tag
import flagent.domain.repository.ITagRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*

class TagRepository : ITagRepository {
    
    override suspend fun findAll(limit: Int?, offset: Int, valueLike: String?): List<Tag> = withContext(Dispatchers.IO) {
        Database.transaction {
            var query = Tags.selectAll()
            
            if (valueLike != null) {
                val pattern = "%${valueLike.lowercase()}%"
                query = query.andWhere {
                    Tags.value.lowerCase() like pattern
                }
            }
            
            query = query.orderBy(Tags.id, SortOrder.ASC)
            
            if (limit != null) {
                query = query.limit(limit).offset(offset.toLong())
            }
            
            query.map { mapRowToTag(it) }
        }
    }
    
    override suspend fun findByValue(value: String): Tag? = withContext(Dispatchers.IO) {
        Database.transaction {
            Tags.selectAll().where { Tags.value eq value }
                .firstOrNull()
                ?.let { mapRowToTag(it) }
        }
    }
    
    override suspend fun create(tag: Tag): Tag = withContext(Dispatchers.IO) {
        Database.transaction {
            // Try to find existing tag first
            val existing = findByValue(tag.value)
            if (existing != null) {
                return@transaction existing
            }
            
            val id = Tags.insert {
                it[value] = tag.value
                it[createdAt] = java.time.LocalDateTime.now()
            }[Tags.id].value
            
            tag.copy(id = id)
        }
    }
    
    override suspend fun delete(id: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Tags.update({ Tags.id eq id }) {
                it[deletedAt] = java.time.LocalDateTime.now()
            }
        }
        Unit
    }
    
    override suspend fun addTagToFlag(flagId: Int, tagId: Int) = withContext(Dispatchers.IO) {
        Database.transaction {
            // Check if already exists
            val exists = FlagsTags.selectAll().where {
                (FlagsTags.flagId eq flagId) and (FlagsTags.tagId eq tagId)
            }.firstOrNull()
            
            if (exists == null) {
                FlagsTags.insert {
                    it[FlagsTags.flagId] = flagId
                    it[FlagsTags.tagId] = tagId
                }
            }
        }
    }
    
    override suspend fun removeTagFromFlag(flagId: Int, tagId: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            FlagsTags.deleteWhere {
                (FlagsTags.flagId eq flagId) and (FlagsTags.tagId eq tagId)
            }
        }
        Unit
    }
    
    override suspend fun findByFlagId(flagId: Int): List<Tag> = withContext(Dispatchers.IO) {
        Database.transaction {
            Tags
                .innerJoin(FlagsTags)
                .selectAll().where { FlagsTags.flagId eq flagId }
                .orderBy(Tags.id, SortOrder.ASC)
                .map { mapRowToTag(it) }
        }
    }
    
    private fun mapRowToTag(row: ResultRow): Tag {
        return Tag(
            id = row[Tags.id].value,
            value = row[Tags.value]
        )
    }
}
