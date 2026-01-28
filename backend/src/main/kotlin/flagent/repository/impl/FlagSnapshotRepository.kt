package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.FlagSnapshot
import flagent.domain.repository.IFlagSnapshotRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*

class FlagSnapshotRepository : IFlagSnapshotRepository {
    
    override suspend fun findByFlagId(flagId: Int, limit: Int?, offset: Int, sortDesc: Boolean): List<FlagSnapshot> = withContext(Dispatchers.IO) {
        Database.transaction {
            var query = FlagSnapshots.selectAll().where { FlagSnapshots.flagId eq flagId }
            
            query = query.orderBy(
                if (sortDesc) FlagSnapshots.createdAt to SortOrder.DESC
                else FlagSnapshots.createdAt to SortOrder.ASC
            )
            
            if (limit != null) {
                query = query.limit(limit).offset(offset.toLong())
            }
            
            query.map { mapRowToFlagSnapshot(it) }
        }
    }
    
    override suspend fun findAll(): List<FlagSnapshot> = withContext(Dispatchers.IO) {
        Database.transaction {
            FlagSnapshots.selectAll()
                .orderBy(FlagSnapshots.createdAt, SortOrder.DESC)
                .map { mapRowToFlagSnapshot(it) }
        }
    }
    
    override suspend fun create(snapshot: FlagSnapshot): FlagSnapshot = withContext(Dispatchers.IO) {
        Database.transaction {
            val now = java.time.LocalDateTime.now()
            val id = FlagSnapshots.insert {
                it[FlagSnapshots.flagId] = snapshot.flagId
                it[FlagSnapshots.updatedBy] = snapshot.updatedBy
                it[FlagSnapshots.flag] = snapshot.flag
                it[FlagSnapshots.createdAt] = now
                it[FlagSnapshots.updatedAt] = now
            }[FlagSnapshots.id].value
            
            snapshot.copy(id = id, updatedAt = now.toString())
        }
    }
    
    private fun mapRowToFlagSnapshot(row: ResultRow): FlagSnapshot {
        return FlagSnapshot(
            id = row[FlagSnapshots.id].value,
            flagId = row[FlagSnapshots.flagId],
            updatedBy = row[FlagSnapshots.updatedBy],
            flag = row[FlagSnapshots.flag],
            updatedAt = row[FlagSnapshots.updatedAt]?.toString()
        )
    }
}
