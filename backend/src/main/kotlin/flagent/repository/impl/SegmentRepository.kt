package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Segment
import flagent.domain.repository.ISegmentRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*

class SegmentRepository : ISegmentRepository {
    
    override suspend fun findByFlagId(flagId: Int): List<Segment> = withContext(Dispatchers.IO) {
        Database.transaction {
            Segments.selectAll()
                .where { (Segments.flagId eq flagId) and (Segments.deletedAt.isNull()) }
                .orderBy(Segments.rank, SortOrder.ASC)
                .orderBy(Segments.id, SortOrder.ASC)
                .map { mapRowToSegment(it) }
        }
    }
    
    override suspend fun findById(id: Int): Segment? = withContext(Dispatchers.IO) {
        Database.transaction {
            Segments.selectAll()
                .where { (Segments.id eq id) and (Segments.deletedAt.isNull()) }
                .firstOrNull()
                ?.let { mapRowToSegment(it) }
        }
    }
    
    override suspend fun create(segment: Segment): Segment = withContext(Dispatchers.IO) {
        Database.transaction {
            val id = Segments.insert {
                it[flagId] = segment.flagId
                it[description] = segment.description
                it[rank] = segment.rank
                it[rolloutPercent] = segment.rolloutPercent
                it[createdAt] = java.time.LocalDateTime.now()
            }[Segments.id].value
            
            segment.copy(id = id)
        }
    }
    
    override suspend fun update(segment: Segment): Segment = withContext(Dispatchers.IO) {
        Database.transaction {
            Segments.update({ Segments.id eq segment.id }) {
                it[description] = segment.description
                it[rank] = segment.rank
                it[rolloutPercent] = segment.rolloutPercent
                it[updatedAt] = java.time.LocalDateTime.now()
            }
            segment
        }
    }
    
    override suspend fun delete(id: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Segments.update({ Segments.id eq id }) {
                it[deletedAt] = java.time.LocalDateTime.now()
            }
        }
        Unit
    }
    
    override suspend fun reorder(flagId: Int, segmentIds: List<Int>) = withContext(Dispatchers.IO) {
        Database.transaction {
            segmentIds.forEachIndexed { index, segmentId ->
                Segments.update({ Segments.id eq segmentId and (Segments.flagId eq flagId) }) {
                    it[rank] = index
                }
            }
        }
    }
    
    private fun mapRowToSegment(row: ResultRow): Segment {
        val segmentId = row[Segments.id].value
        
        val constraints = Constraints.selectAll().where { Constraints.segmentId eq segmentId }
            .orderBy(Constraints.createdAt, SortOrder.ASC)
            .map { 
                Constraint(
                    id = it[Constraints.id].value,
                    segmentId = it[Constraints.segmentId],
                    property = it[Constraints.property],
                    operator = it[Constraints.operator],
                    value = it[Constraints.value]
                )
            }
        
        val distributions = Distributions.selectAll().where { Distributions.segmentId eq segmentId }
            .orderBy(Distributions.variantId, SortOrder.ASC)
            .map {
                flagent.domain.entity.Distribution(
                    id = it[Distributions.id].value,
                    segmentId = it[Distributions.segmentId],
                    variantId = it[Distributions.variantId],
                    variantKey = it[Distributions.variantKey],
                    percent = it[Distributions.percent]
                )
            }
        
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
}
