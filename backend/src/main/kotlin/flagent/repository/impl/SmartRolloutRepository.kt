package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.SmartRolloutConfig
import flagent.domain.entity.SmartRolloutHistory
import flagent.domain.repository.ISmartRolloutRepository
import flagent.repository.tables.SmartRolloutConfigs
import flagent.repository.tables.SmartRolloutHistory as SmartRolloutHistoryTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime

/**
 * SmartRolloutRepository - implementation of ISmartRolloutRepository
 * 
 * Infrastructure layer - uses Exposed for database access
 */
class SmartRolloutRepository : ISmartRolloutRepository {
    // ===== Smart Rollout Configs =====
    
    override suspend fun saveConfig(config: SmartRolloutConfig): SmartRolloutConfig = transaction {
        val id = SmartRolloutConfigs.insertAndGetId {
            it[flagId] = config.flagId
            it[segmentId] = config.segmentId
            it[enabled] = config.enabled
            it[targetRolloutPercent] = config.targetRolloutPercent
            it[currentRolloutPercent] = config.currentRolloutPercent
            it[incrementPercent] = config.incrementPercent
            it[incrementIntervalMs] = config.incrementIntervalMs
            it[successRateThreshold] = config.successRateThreshold.toBigDecimal()
            it[errorRateThreshold] = config.errorRateThreshold.toBigDecimal()
            it[conversionRateThreshold] = config.conversionRateThreshold?.toBigDecimal()
            it[minSampleSize] = config.minSampleSize
            it[autoRollback] = config.autoRollback
            it[rollbackOnAnomaly] = config.rollbackOnAnomaly
            it[pauseOnAnomaly] = config.pauseOnAnomaly
            it[notifyOnIncrement] = config.notifyOnIncrement
            it[lastIncrementAt] = config.lastIncrementAt
            it[completedAt] = config.completedAt
            it[status] = config.status.name
            it[tenantId] = config.tenantId
            it[createdAt] = LocalDateTime.now()
        }
        config.copy(id = id.value)
    }
    
    override suspend fun findConfigById(id: Int, tenantId: String?): SmartRolloutConfig? = transaction {
        SmartRolloutConfigs.selectAll().where {
            (SmartRolloutConfigs.id eq id) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToSmartRolloutConfig(it) }.singleOrNull()
    }
    
    override suspend fun findConfigByFlagAndSegment(
        flagId: Int,
        segmentId: Int,
        tenantId: String?
    ): SmartRolloutConfig? = transaction {
        SmartRolloutConfigs.selectAll().where {
            (SmartRolloutConfigs.flagId eq flagId) and
            (SmartRolloutConfigs.segmentId eq segmentId) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToSmartRolloutConfig(it) }.singleOrNull()
    }
    
    override suspend fun findConfigsByFlagId(
        flagId: Int,
        tenantId: String?
    ): List<SmartRolloutConfig> = transaction {
        SmartRolloutConfigs.selectAll().where {
            (SmartRolloutConfigs.flagId eq flagId) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToSmartRolloutConfig(it) }
    }
    
    override suspend fun findActiveConfigs(tenantId: String?): List<SmartRolloutConfig> = transaction {
        SmartRolloutConfigs.selectAll().where {
            (SmartRolloutConfigs.status eq SmartRolloutConfig.RolloutStatus.IN_PROGRESS.name) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToSmartRolloutConfig(it) }
    }
    
    override suspend fun findEnabledConfigs(tenantId: String?): List<SmartRolloutConfig> = transaction {
        SmartRolloutConfigs.selectAll().where {
            (SmartRolloutConfigs.enabled eq true) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToSmartRolloutConfig(it) }
    }
    
    override suspend fun updateConfig(config: SmartRolloutConfig): SmartRolloutConfig = transaction {
        SmartRolloutConfigs.update({ SmartRolloutConfigs.id eq config.id }) {
            it[enabled] = config.enabled
            it[targetRolloutPercent] = config.targetRolloutPercent
            it[currentRolloutPercent] = config.currentRolloutPercent
            it[incrementPercent] = config.incrementPercent
            it[incrementIntervalMs] = config.incrementIntervalMs
            it[successRateThreshold] = config.successRateThreshold.toBigDecimal()
            it[errorRateThreshold] = config.errorRateThreshold.toBigDecimal()
            it[conversionRateThreshold] = config.conversionRateThreshold?.toBigDecimal()
            it[minSampleSize] = config.minSampleSize
            it[autoRollback] = config.autoRollback
            it[rollbackOnAnomaly] = config.rollbackOnAnomaly
            it[pauseOnAnomaly] = config.pauseOnAnomaly
            it[notifyOnIncrement] = config.notifyOnIncrement
            it[lastIncrementAt] = config.lastIncrementAt
            it[completedAt] = config.completedAt
            it[status] = config.status.name
            it[updatedAt] = LocalDateTime.now()
        }
        config
    }
    
    override suspend fun deleteConfig(id: Int, tenantId: String?): Boolean = transaction {
        SmartRolloutConfigs.deleteWhere {
            (SmartRolloutConfigs.id eq id) and
            (tenantId?.let { SmartRolloutConfigs.tenantId eq it } ?: Op.TRUE)
        } > 0
    }
    
    // ===== Smart Rollout History =====
    
    override suspend fun saveHistory(history: SmartRolloutHistory): SmartRolloutHistory = transaction {
        val id = SmartRolloutHistoryTable.insertAndGetId {
            it[rolloutConfigId] = history.rolloutConfigId
            it[flagId] = history.flagId
            it[segmentId] = history.segmentId
            it[previousPercent] = history.previousPercent
            it[newPercent] = history.newPercent
            it[reason] = history.reason
            it[successRate] = history.successRate?.toBigDecimal()
            it[errorRate] = history.errorRate?.toBigDecimal()
            it[sampleSize] = history.sampleSize
            it[timestamp] = history.timestamp
            it[tenantId] = history.tenantId
            it[createdAt] = LocalDateTime.now()
        }
        history.copy(id = id.value)
    }
    
    override suspend fun findHistoryByConfigId(
        rolloutConfigId: Int,
        limit: Int,
        tenantId: String?
    ): List<SmartRolloutHistory> = transaction {
        SmartRolloutHistoryTable.selectAll().where {
            (SmartRolloutHistoryTable.rolloutConfigId eq rolloutConfigId) and
            (tenantId?.let { SmartRolloutHistoryTable.tenantId eq it } ?: Op.TRUE)
        }
        .orderBy(SmartRolloutHistoryTable.timestamp, SortOrder.DESC)
        .limit(limit)
        .map { rowToSmartRolloutHistory(it) }
    }
    
    override suspend fun findHistoryByFlagId(
        flagId: Int,
        limit: Int,
        tenantId: String?
    ): List<SmartRolloutHistory> = transaction {
        SmartRolloutHistoryTable.selectAll().where {
            (SmartRolloutHistoryTable.flagId eq flagId) and
            (tenantId?.let { SmartRolloutHistoryTable.tenantId eq it } ?: Op.TRUE)
        }
        .orderBy(SmartRolloutHistoryTable.timestamp, SortOrder.DESC)
        .limit(limit)
        .map { rowToSmartRolloutHistory(it) }
    }
    
    override suspend fun deleteHistoryOlderThan(timestamp: Long, tenantId: String?): Int = transaction {
        SmartRolloutHistoryTable.deleteWhere {
            (SmartRolloutHistoryTable.timestamp lessEq timestamp) and
            (tenantId?.let { SmartRolloutHistoryTable.tenantId eq it } ?: Op.TRUE)
        }
    }
    
    /**
     * Convert database row to SmartRolloutConfig entity
     */
    private fun rowToSmartRolloutConfig(row: ResultRow): SmartRolloutConfig {
        return SmartRolloutConfig(
            id = row[SmartRolloutConfigs.id].value,
            flagId = row[SmartRolloutConfigs.flagId],
            segmentId = row[SmartRolloutConfigs.segmentId],
            enabled = row[SmartRolloutConfigs.enabled],
            targetRolloutPercent = row[SmartRolloutConfigs.targetRolloutPercent],
            currentRolloutPercent = row[SmartRolloutConfigs.currentRolloutPercent],
            incrementPercent = row[SmartRolloutConfigs.incrementPercent],
            incrementIntervalMs = row[SmartRolloutConfigs.incrementIntervalMs],
            successRateThreshold = row[SmartRolloutConfigs.successRateThreshold].toDouble(),
            errorRateThreshold = row[SmartRolloutConfigs.errorRateThreshold].toDouble(),
            conversionRateThreshold = row[SmartRolloutConfigs.conversionRateThreshold]?.toDouble(),
            minSampleSize = row[SmartRolloutConfigs.minSampleSize],
            autoRollback = row[SmartRolloutConfigs.autoRollback],
            rollbackOnAnomaly = row[SmartRolloutConfigs.rollbackOnAnomaly],
            pauseOnAnomaly = row[SmartRolloutConfigs.pauseOnAnomaly],
            notifyOnIncrement = row[SmartRolloutConfigs.notifyOnIncrement],
            createdAt = row[SmartRolloutConfigs.createdAt].atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            updatedAt = row[SmartRolloutConfigs.updatedAt]?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() 
                ?: row[SmartRolloutConfigs.createdAt].atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            lastIncrementAt = row[SmartRolloutConfigs.lastIncrementAt],
            completedAt = row[SmartRolloutConfigs.completedAt],
            status = SmartRolloutConfig.RolloutStatus.valueOf(row[SmartRolloutConfigs.status]),
            tenantId = row[SmartRolloutConfigs.tenantId]
        )
    }
    
    /**
     * Convert database row to SmartRolloutHistory entity
     */
    private fun rowToSmartRolloutHistory(row: ResultRow): SmartRolloutHistory {
        return SmartRolloutHistory(
            id = row[SmartRolloutHistoryTable.id].value,
            rolloutConfigId = row[SmartRolloutHistoryTable.rolloutConfigId],
            flagId = row[SmartRolloutHistoryTable.flagId],
            segmentId = row[SmartRolloutHistoryTable.segmentId],
            previousPercent = row[SmartRolloutHistoryTable.previousPercent],
            newPercent = row[SmartRolloutHistoryTable.newPercent],
            reason = row[SmartRolloutHistoryTable.reason],
            successRate = row[SmartRolloutHistoryTable.successRate]?.toDouble(),
            errorRate = row[SmartRolloutHistoryTable.errorRate]?.toDouble(),
            sampleSize = row[SmartRolloutHistoryTable.sampleSize],
            timestamp = row[SmartRolloutHistoryTable.timestamp],
            tenantId = row[SmartRolloutHistoryTable.tenantId]
        )
    }
}
