package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.FlagEntityType
import flagent.domain.repository.IFlagEntityTypeRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*

class FlagEntityTypeRepository : IFlagEntityTypeRepository {
    
    override suspend fun findAll(): List<FlagEntityType> = withContext(Dispatchers.IO) {
        Database.transaction {
            FlagEntityTypes.selectAll()
                .orderBy(FlagEntityTypes.key, SortOrder.ASC)
                .map { mapRowToFlagEntityType(it) }
        }
    }
    
    override suspend fun findByKey(key: String): FlagEntityType? = withContext(Dispatchers.IO) {
        Database.transaction {
            FlagEntityTypes.selectAll().where { FlagEntityTypes.key eq key }
                .firstOrNull()
                ?.let { mapRowToFlagEntityType(it) }
        }
    }
    
    override suspend fun create(entityType: FlagEntityType): FlagEntityType = withContext(Dispatchers.IO) {
        Database.transaction {
            // Try to find existing entity type first
            val existing = findByKey(entityType.key)
            if (existing != null) {
                return@transaction existing
            }
            
            val id = FlagEntityTypes.insert {
                it[FlagEntityTypes.key] = entityType.key
                it[FlagEntityTypes.createdAt] = java.time.LocalDateTime.now()
            }[FlagEntityTypes.id].value
            
            entityType.copy(id = id)
        }
    }
    
    private fun mapRowToFlagEntityType(row: ResultRow): FlagEntityType {
        return FlagEntityType(
            id = row[FlagEntityTypes.id].value,
            key = row[FlagEntityTypes.key]
        )
    }
}
