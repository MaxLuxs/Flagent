package flagent.repository.impl

import flagent.domain.entity.Constraint
import flagent.domain.repository.IConstraintRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ConstraintRepository : IConstraintRepository {
    
    override suspend fun findBySegmentId(segmentId: Int): List<Constraint> = withContext(Dispatchers.IO) {
        Database.transaction {
            Constraints.select { Constraints.segmentId eq segmentId }
                .orderBy(Constraints.createdAt, SortOrder.ASC)
                .map { mapRowToConstraint(it) }
        }
    }
    
    override suspend fun findById(id: Int): Constraint? = withContext(Dispatchers.IO) {
        Database.transaction {
            Constraints.select { Constraints.id eq id }
                .firstOrNull()
                ?.let { mapRowToConstraint(it) }
        }
    }
    
    override suspend fun create(constraint: Constraint): Constraint = withContext(Dispatchers.IO) {
        Database.transaction {
            val id = Constraints.insert {
                it[segmentId] = constraint.segmentId
                it[property] = constraint.property
                it[operator] = constraint.operator
                it[value] = constraint.value
                it[createdAt] = java.time.Instant.now()
            }[Constraints.id].value
            
            constraint.copy(id = id)
        }
    }
    
    override suspend fun update(constraint: Constraint): Constraint = withContext(Dispatchers.IO) {
        Database.transaction {
            Constraints.update({ Constraints.id eq constraint.id }) {
                it[property] = constraint.property
                it[operator] = constraint.operator
                it[value] = constraint.value
                it[updatedAt] = java.time.Instant.now()
            }
            constraint
        }
    }
    
    override suspend fun delete(id: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Constraints.update({ Constraints.id eq id }) {
                it[deletedAt] = java.time.Instant.now()
            }
        }
        Unit
    }
    
    private fun mapRowToConstraint(row: ResultRow): Constraint {
        return Constraint(
            id = row[Constraints.id].value,
            segmentId = row[Constraints.segmentId],
            property = row[Constraints.property],
            operator = row[Constraints.operator],
            value = row[Constraints.value]
        )
    }
}
