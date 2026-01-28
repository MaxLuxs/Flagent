package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.UsageRecord
import flagent.domain.entity.UsageMetricType
import flagent.domain.repository.IUsageRecordRepository
import flagent.repository.tables.UsageRecords
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.LocalDateTime

/**
 * Usage record repository implementation using Exposed.
 */
class UsageRecordRepository : IUsageRecordRepository {

    override suspend fun create(record: UsageRecord): UsageRecord = suspendTransaction {
        val id = UsageRecords.insertAndGetId {
            it[tenantId] = record.tenantId
            it[subscriptionId] = record.subscriptionId
            it[metricType] = record.metricType.name
            it[quantity] = record.quantity
            it[timestamp] = record.timestamp
            it[reportedToStripe] = record.reportedToStripe
            it[stripeUsageRecordId] = record.stripeUsageRecordId
            it[createdAt] = record.createdAt
        }
        record.copy(id = id.value)
    }

    override suspend fun update(record: UsageRecord): UsageRecord = suspendTransaction {
        UsageRecords.update({ UsageRecords.id eq record.id }) {
            it[tenantId] = record.tenantId
            it[subscriptionId] = record.subscriptionId
            it[metricType] = record.metricType.name
            it[quantity] = record.quantity
            it[timestamp] = record.timestamp
            it[reportedToStripe] = record.reportedToStripe
            it[stripeUsageRecordId] = record.stripeUsageRecordId
        }
        record
    }

    override suspend fun findById(id: Long): UsageRecord? = suspendTransaction {
        UsageRecords.selectAll()
            .where { UsageRecords.id eq id }
            .singleOrNull()
            ?.toUsageRecord()
    }

    override suspend fun findByTenantIdAndPeriod(
        tenantId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<UsageRecord> = suspendTransaction {
        UsageRecords.selectAll()
            .where { 
                (UsageRecords.tenantId eq tenantId) and
                (UsageRecords.timestamp greaterEq startTime) and
                (UsageRecords.timestamp lessEq endTime)
            }
            .orderBy(UsageRecords.timestamp to SortOrder.ASC)
            .map { it.toUsageRecord() }
    }

    override suspend fun findUnreportedByTenantId(tenantId: Long): List<UsageRecord> = suspendTransaction {
        UsageRecords.selectAll()
            .where { 
                (UsageRecords.tenantId eq tenantId) and
                (UsageRecords.reportedToStripe eq false)
            }
            .orderBy(UsageRecords.timestamp to SortOrder.ASC)
            .map { it.toUsageRecord() }
    }

    override suspend fun getAggregatedUsage(
        tenantId: Long,
        metricType: UsageMetricType,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Long = suspendTransaction {
        UsageRecords
            .select(UsageRecords.quantity.sum())
            .where { 
                (UsageRecords.tenantId eq tenantId) and
                (UsageRecords.metricType eq metricType.name) and
                (UsageRecords.timestamp greaterEq startTime) and
                (UsageRecords.timestamp lessEq endTime)
            }
            .singleOrNull()
            ?.get(UsageRecords.quantity.sum()) ?: 0L
    }

    override suspend fun markAsReported(ids: List<Long>, stripeUsageRecordId: String): Int = suspendTransaction {
        UsageRecords.update({ UsageRecords.id inList ids }) {
            it[reportedToStripe] = true
            it[UsageRecords.stripeUsageRecordId] = stripeUsageRecordId
        }
    }

    private fun ResultRow.toUsageRecord() = UsageRecord(
        id = this[UsageRecords.id].value,
        tenantId = this[UsageRecords.tenantId],
        subscriptionId = this[UsageRecords.subscriptionId],
        metricType = UsageMetricType.valueOf(this[UsageRecords.metricType]),
        quantity = this[UsageRecords.quantity],
        timestamp = this[UsageRecords.timestamp],
        reportedToStripe = this[UsageRecords.reportedToStripe],
        stripeUsageRecordId = this[UsageRecords.stripeUsageRecordId],
        createdAt = this[UsageRecords.createdAt]
    )
}
