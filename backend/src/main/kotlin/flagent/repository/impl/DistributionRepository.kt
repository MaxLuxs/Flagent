package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Distribution
import flagent.domain.repository.IDistributionRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*

class DistributionRepository : IDistributionRepository {
    
    override suspend fun findBySegmentId(segmentId: Int): List<Distribution> = withContext(Dispatchers.IO) {
        Database.transaction {
            Distributions.selectAll().where { Distributions.segmentId eq segmentId }
                .orderBy(Distributions.variantId, SortOrder.ASC)
                .map { mapRowToDistribution(it) }
        }
    }
    
    override suspend fun updateDistributions(
        segmentId: Int,
        distributions: List<Distribution>
    ) = withContext(Dispatchers.IO) {
        Database.transaction {
            // Delete existing distributions
            Distributions.deleteWhere { Distributions.segmentId eq segmentId }
            
            // Insert new distributions
            distributions.forEach { distribution ->
                Distributions.insert {
                    it[Distributions.segmentId] = segmentId
                    it[variantId] = distribution.variantId
                    it[variantKey] = distribution.variantKey
                    it[percent] = distribution.percent
                    it[createdAt] = java.time.LocalDateTime.now()
                }
            }
        }
    }
    
    override suspend fun updateVariantKeyByVariantId(variantId: Int, variantKey: String): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Distributions.update({ Distributions.variantId eq variantId }) {
                it[Distributions.variantKey] = variantKey
                it[updatedAt] = java.time.LocalDateTime.now()
            }
        }
        Unit
    }
    
    private fun mapRowToDistribution(row: ResultRow): Distribution {
        return Distribution(
            id = row[Distributions.id].value,
            segmentId = row[Distributions.segmentId],
            variantId = row[Distributions.variantId],
            variantKey = row[Distributions.variantKey],
            percent = row[Distributions.percent]
        )
    }
}
