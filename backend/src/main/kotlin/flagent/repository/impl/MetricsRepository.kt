package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.MetricAggregation
import flagent.domain.entity.MetricDataPoint
import flagent.domain.entity.MetricOverviewRecord
import flagent.domain.repository.IMetricsRepository
import flagent.repository.tables.MetricDataPoints
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import kotlin.math.sqrt

/**
 * MetricsRepository - implementation of IMetricsRepository
 * 
 * Infrastructure layer - uses Exposed for database access
 */
class MetricsRepository : IMetricsRepository {
    override suspend fun save(metric: MetricDataPoint): MetricDataPoint = transaction {
        val id = MetricDataPoints.insertAndGetId {
            it[flagId] = metric.flagId
            it[flagKey] = metric.flagKey
            it[segmentId] = metric.segmentId
            it[variantId] = metric.variantId
            it[variantKey] = metric.variantKey
            it[metricType] = metric.metricType.name
            it[metricValue] = metric.metricValue.toBigDecimal()
            it[timestamp] = metric.timestamp
            it[entityId] = metric.entityId
            it[tenantId] = metric.tenantId
            it[createdAt] = LocalDateTime.now()
        }
        metric.copy(id = id.value)
    }
    
    override suspend fun saveBatch(metrics: List<MetricDataPoint>): List<MetricDataPoint> = transaction {
        val now = LocalDateTime.now()
        
        val ids = MetricDataPoints.batchInsert(metrics) { metric ->
            this[MetricDataPoints.flagId] = metric.flagId
            this[MetricDataPoints.flagKey] = metric.flagKey
            this[MetricDataPoints.segmentId] = metric.segmentId
            this[MetricDataPoints.variantId] = metric.variantId
            this[MetricDataPoints.variantKey] = metric.variantKey
            this[MetricDataPoints.metricType] = metric.metricType.name
            this[MetricDataPoints.metricValue] = metric.metricValue.toBigDecimal()
            this[MetricDataPoints.timestamp] = metric.timestamp
            this[MetricDataPoints.entityId] = metric.entityId
            this[MetricDataPoints.tenantId] = metric.tenantId
            this[MetricDataPoints.createdAt] = now
        }
        
        ids.mapIndexed { index, row ->
            metrics[index].copy(id = row[MetricDataPoints.id].value.toLong())
        }
    }
    
    override suspend fun findByFlagId(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType?,
        variantId: Int?,
        tenantId: String?
    ): List<MetricDataPoint> = transaction {
        MetricDataPoints.selectAll().where {
            (MetricDataPoints.flagId eq flagId) and
            (MetricDataPoints.timestamp greaterEq startTime) and
            (MetricDataPoints.timestamp lessEq endTime) and
            (metricType?.let { MetricDataPoints.metricType eq it.name } ?: Op.TRUE) and
            (variantId?.let { MetricDataPoints.variantId eq it } ?: Op.TRUE) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }.map { rowToMetricDataPoint(it) }
    }
    
    override suspend fun findByFlagKey(
        flagKey: String,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType?,
        variantId: Int?,
        tenantId: String?
    ): List<MetricDataPoint> = transaction {
        MetricDataPoints.selectAll().where {
            (MetricDataPoints.flagKey eq flagKey) and
            (MetricDataPoints.timestamp greaterEq startTime) and
            (MetricDataPoints.timestamp lessEq endTime) and
            (metricType?.let { MetricDataPoints.metricType eq it.name } ?: Op.TRUE) and
            (variantId?.let { MetricDataPoints.variantId eq it } ?: Op.TRUE) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }.map { rowToMetricDataPoint(it) }
    }
    
    override suspend fun getAggregation(
        flagId: Int,
        metricType: MetricDataPoint.MetricType,
        windowStartMs: Long,
        windowEndMs: Long,
        variantId: Int?,
        tenantId: String?
    ): MetricAggregation? = transaction {
        val query = MetricDataPoints.selectAll().where {
            (MetricDataPoints.flagId eq flagId) and
            (MetricDataPoints.metricType eq metricType.name) and
            (MetricDataPoints.timestamp greaterEq windowStartMs) and
            (MetricDataPoints.timestamp lessEq windowEndMs) and
            (variantId?.let { MetricDataPoints.variantId eq it } ?: Op.TRUE) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }
        
        val metrics = query.map { rowToMetricDataPoint(it) }
        
        if (metrics.isEmpty()) {
            return@transaction null
        }
        
        val values = metrics.map { it.metricValue }
        val avg = values.average()
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 0.0
        val stdDev = calculateStdDev(values, avg)
        
        MetricAggregation(
            flagId = flagId,
            flagKey = metrics.first().flagKey,
            variantId = variantId,
            variantKey = metrics.firstOrNull()?.variantKey,
            metricType = metricType,
            avgValue = avg,
            minValue = min,
            maxValue = max,
            stdDev = stdDev,
            count = metrics.size,
            windowStartMs = windowStartMs,
            windowEndMs = windowEndMs,
            tenantId = tenantId
        )
    }
    
    override suspend fun getAggregations(
        flagIds: List<Int>,
        metricType: MetricDataPoint.MetricType,
        windowStartMs: Long,
        windowEndMs: Long,
        tenantId: String?
    ): List<MetricAggregation> = newSuspendedTransaction {
        flagIds.mapNotNull { flagId ->
            getAggregation(flagId, metricType, windowStartMs, windowEndMs, null, tenantId)
        }
    }
    
    override suspend fun deleteOlderThan(timestamp: Long, tenantId: String?): Int = transaction {
        MetricDataPoints.deleteWhere {
            (MetricDataPoints.timestamp lessEq timestamp) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }
    }
    
    override suspend fun countByFlagId(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        metricType: MetricDataPoint.MetricType?,
        variantId: Int?,
        tenantId: String?
    ): Int = transaction {
        MetricDataPoints.selectAll().where {
            (MetricDataPoints.flagId eq flagId) and
            (MetricDataPoints.timestamp greaterEq startTime) and
            (MetricDataPoints.timestamp lessEq endTime) and
            (metricType?.let { MetricDataPoints.metricType eq it.name } ?: Op.TRUE) and
            (variantId?.let { MetricDataPoints.variantId eq it } ?: Op.TRUE) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }.count().toInt()
    }

    override suspend fun findMetricsInTimeRange(
        startTime: Long,
        endTime: Long,
        tenantId: String?
    ): List<MetricOverviewRecord> = transaction {
        MetricDataPoints.selectAll().where {
            (MetricDataPoints.timestamp greaterEq startTime) and
            (MetricDataPoints.timestamp lessEq endTime) and
            (tenantId?.let { MetricDataPoints.tenantId eq it } ?: Op.TRUE)
        }.map { row ->
            MetricOverviewRecord(
                timestamp = row[MetricDataPoints.timestamp],
                flagId = row[MetricDataPoints.flagId],
                flagKey = row[MetricDataPoints.flagKey]
            )
        }
    }
    
    /**
     * Convert database row to MetricDataPoint entity
     */
    private fun rowToMetricDataPoint(row: ResultRow): MetricDataPoint {
        return MetricDataPoint(
            id = row[MetricDataPoints.id].value.toLong(),
            flagId = row[MetricDataPoints.flagId],
            flagKey = row[MetricDataPoints.flagKey],
            segmentId = row[MetricDataPoints.segmentId],
            variantId = row[MetricDataPoints.variantId],
            variantKey = row[MetricDataPoints.variantKey],
            metricType = MetricDataPoint.MetricType.valueOf(row[MetricDataPoints.metricType]),
            metricValue = row[MetricDataPoints.metricValue].toDouble(),
            timestamp = row[MetricDataPoints.timestamp],
            entityId = row[MetricDataPoints.entityId],
            tenantId = row[MetricDataPoints.tenantId]
        )
    }
    
    /**
     * Calculate standard deviation
     */
    private fun calculateStdDev(values: List<Double>, mean: Double): Double {
        if (values.size < 2) return 0.0
        
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }
}
