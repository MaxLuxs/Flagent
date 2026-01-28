package flagent.repository.impl

import flagent.domain.entity.AnomalyAlert
import flagent.domain.entity.AnomalyDetectionConfig
import flagent.domain.repository.IAnomalyAlertRepository
import flagent.repository.tables.AnomalyAlerts
import flagent.repository.tables.AnomalyDetectionConfigs
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

/**
 * AnomalyAlertRepository - implementation of IAnomalyAlertRepository
 * 
 * Infrastructure layer - uses Exposed for database access
 */
class AnomalyAlertRepository : IAnomalyAlertRepository {
    // ===== Anomaly Alerts =====
    
    override suspend fun saveAlert(alert: AnomalyAlert): AnomalyAlert = transaction {
        val id = AnomalyAlerts.insertAndGetId {
            it[flagId] = alert.flagId
            it[flagKey] = alert.flagKey
            it[variantId] = alert.variantId
            it[variantKey] = alert.variantKey
            it[anomalyType] = alert.anomalyType.name
            it[severity] = alert.severity.name
            it[detectedAt] = alert.detectedAt
            it[metricType] = alert.metricType.name
            it[metricValue] = alert.metricValue.toBigDecimal()
            it[expectedValue] = alert.expectedValue.toBigDecimal()
            it[zScore] = alert.zScore.toBigDecimal()
            it[message] = alert.message
            it[actionTaken] = alert.actionTaken?.name
            it[actionTakenAt] = alert.actionTakenAt
            it[resolved] = alert.resolved
            it[resolvedAt] = alert.resolvedAt
            it[tenantId] = alert.tenantId
            it[createdAt] = LocalDateTime.now()
        }
        alert.copy(id = id.value)
    }
    
    override suspend fun findAlertById(id: Int, tenantId: String?): AnomalyAlert? = transaction {
        AnomalyAlerts.selectAll()
            .where {
                (AnomalyAlerts.id eq id) and
                (tenantId?.let { AnomalyAlerts.tenantId eq it } ?: Op.TRUE)
            }
            .map { rowToAnomalyAlert(it) }
            .singleOrNull()
    }
    
    override suspend fun findAlertsByFlagId(
        flagId: Int,
        resolved: Boolean?,
        severity: AnomalyAlert.Severity?,
        limit: Int,
        tenantId: String?
    ): List<AnomalyAlert> = transaction {
        AnomalyAlerts.selectAll().where {
            (AnomalyAlerts.flagId eq flagId) and
            (resolved?.let { AnomalyAlerts.resolved eq it } ?: Op.TRUE) and
            (severity?.let { AnomalyAlerts.severity eq it.name } ?: Op.TRUE) and
            (tenantId?.let { AnomalyAlerts.tenantId eq it } ?: Op.TRUE)
        }
        .orderBy(AnomalyAlerts.detectedAt, SortOrder.DESC)
        .limit(limit)
        .map { rowToAnomalyAlert(it) }
    }
    
    override suspend fun findUnresolvedAlerts(
        flagId: Int?,
        tenantId: String?
    ): List<AnomalyAlert> = transaction {
        AnomalyAlerts.selectAll().where {
            (AnomalyAlerts.resolved eq false) and
            (flagId?.let { AnomalyAlerts.flagId eq it } ?: Op.TRUE) and
            (tenantId?.let { AnomalyAlerts.tenantId eq it } ?: Op.TRUE)
        }
        .orderBy(AnomalyAlerts.detectedAt, SortOrder.DESC)
        .map { rowToAnomalyAlert(it) }
    }
    
    override suspend fun updateAlert(alert: AnomalyAlert): AnomalyAlert = transaction {
        AnomalyAlerts.update({ AnomalyAlerts.id eq alert.id }) {
            it[actionTaken] = alert.actionTaken?.name
            it[actionTakenAt] = alert.actionTakenAt
            it[resolved] = alert.resolved
            it[resolvedAt] = alert.resolvedAt
            it[updatedAt] = LocalDateTime.now()
        }
        alert
    }
    
    override suspend fun markResolved(alertId: Int, tenantId: String?): AnomalyAlert? = newSuspendedTransaction {
        val alert = findAlertById(alertId, tenantId) ?: return@newSuspendedTransaction null
        
        val now = System.currentTimeMillis()
        AnomalyAlerts.update({ AnomalyAlerts.id eq alertId }) {
            it[resolved] = true
            it[resolvedAt] = now
            it[updatedAt] = LocalDateTime.now()
        }
        
        alert.copy(resolved = true, resolvedAt = now)
    }
    
    override suspend fun deleteOlderThan(timestamp: Long, tenantId: String?): Int = transaction {
        AnomalyAlerts.deleteWhere {
            (AnomalyAlerts.detectedAt lessEq timestamp) and
            (tenantId?.let { AnomalyAlerts.tenantId eq it } ?: Op.TRUE)
        }
    }
    
    // ===== Anomaly Detection Configs =====
    
    override suspend fun saveConfig(config: AnomalyDetectionConfig): AnomalyDetectionConfig = transaction {
        val id = AnomalyDetectionConfigs.insertAndGetId {
            it[flagId] = config.flagId
            it[enabled] = config.enabled
            it[zScoreThreshold] = config.zScoreThreshold.toBigDecimal()
            it[errorRateThreshold] = config.errorRateThreshold.toBigDecimal()
            it[successRateThreshold] = config.successRateThreshold.toBigDecimal()
            it[latencyThresholdMs] = config.latencyThresholdMs.toBigDecimal()
            it[conversionRateThreshold] = config.conversionRateThreshold.toBigDecimal()
            it[minSampleSize] = config.minSampleSize
            it[windowSizeMs] = config.windowSizeMs
            it[autoKillSwitch] = config.autoKillSwitch
            it[autoRollback] = config.autoRollback
            it[tenantId] = config.tenantId
            it[createdAt] = LocalDateTime.now()
        }
        config
    }
    
    override suspend fun findConfigByFlagId(flagId: Int, tenantId: String?): AnomalyDetectionConfig? = transaction {
        AnomalyDetectionConfigs.selectAll().where {
            (AnomalyDetectionConfigs.flagId eq flagId) and
            (tenantId?.let { AnomalyDetectionConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToAnomalyDetectionConfig(it) }.singleOrNull()
    }
    
    override suspend fun findEnabledConfigs(tenantId: String?): List<AnomalyDetectionConfig> = transaction {
        AnomalyDetectionConfigs.selectAll().where {
            (AnomalyDetectionConfigs.enabled eq true) and
            (tenantId?.let { AnomalyDetectionConfigs.tenantId eq it } ?: Op.TRUE)
        }.map { rowToAnomalyDetectionConfig(it) }
    }
    
    override suspend fun updateConfig(config: AnomalyDetectionConfig): AnomalyDetectionConfig = transaction {
        AnomalyDetectionConfigs.update({ AnomalyDetectionConfigs.flagId eq config.flagId }) {
            it[enabled] = config.enabled
            it[zScoreThreshold] = config.zScoreThreshold.toBigDecimal()
            it[errorRateThreshold] = config.errorRateThreshold.toBigDecimal()
            it[successRateThreshold] = config.successRateThreshold.toBigDecimal()
            it[latencyThresholdMs] = config.latencyThresholdMs.toBigDecimal()
            it[conversionRateThreshold] = config.conversionRateThreshold.toBigDecimal()
            it[minSampleSize] = config.minSampleSize
            it[windowSizeMs] = config.windowSizeMs
            it[autoKillSwitch] = config.autoKillSwitch
            it[autoRollback] = config.autoRollback
            it[updatedAt] = LocalDateTime.now()
        }
        config
    }
    
    override suspend fun deleteConfig(flagId: Int, tenantId: String?): Boolean = transaction {
        AnomalyDetectionConfigs.deleteWhere {
            (AnomalyDetectionConfigs.flagId eq flagId) and
            (tenantId?.let { AnomalyDetectionConfigs.tenantId eq it } ?: Op.TRUE)
        } > 0
    }
    
    /**
     * Convert database row to AnomalyAlert entity
     */
    private fun rowToAnomalyAlert(row: ResultRow): AnomalyAlert {
        return AnomalyAlert(
            id = row[AnomalyAlerts.id].value,
            flagId = row[AnomalyAlerts.flagId],
            flagKey = row[AnomalyAlerts.flagKey],
            variantId = row[AnomalyAlerts.variantId],
            variantKey = row[AnomalyAlerts.variantKey],
            anomalyType = AnomalyAlert.AnomalyType.valueOf(row[AnomalyAlerts.anomalyType]),
            severity = AnomalyAlert.Severity.valueOf(row[AnomalyAlerts.severity]),
            detectedAt = row[AnomalyAlerts.detectedAt],
            metricType = flagent.domain.entity.MetricDataPoint.MetricType.valueOf(row[AnomalyAlerts.metricType]),
            metricValue = row[AnomalyAlerts.metricValue].toDouble(),
            expectedValue = row[AnomalyAlerts.expectedValue].toDouble(),
            zScore = row[AnomalyAlerts.zScore].toDouble(),
            message = row[AnomalyAlerts.message],
            actionTaken = row[AnomalyAlerts.actionTaken]?.let { AnomalyAlert.ActionTaken.valueOf(it) },
            actionTakenAt = row[AnomalyAlerts.actionTakenAt],
            resolved = row[AnomalyAlerts.resolved],
            resolvedAt = row[AnomalyAlerts.resolvedAt],
            tenantId = row[AnomalyAlerts.tenantId]
        )
    }
    
    /**
     * Convert database row to AnomalyDetectionConfig entity
     */
    private fun rowToAnomalyDetectionConfig(row: ResultRow): AnomalyDetectionConfig {
        return AnomalyDetectionConfig(
            flagId = row[AnomalyDetectionConfigs.flagId],
            enabled = row[AnomalyDetectionConfigs.enabled],
            zScoreThreshold = row[AnomalyDetectionConfigs.zScoreThreshold].toDouble(),
            errorRateThreshold = row[AnomalyDetectionConfigs.errorRateThreshold].toDouble(),
            successRateThreshold = row[AnomalyDetectionConfigs.successRateThreshold].toDouble(),
            latencyThresholdMs = row[AnomalyDetectionConfigs.latencyThresholdMs].toDouble(),
            conversionRateThreshold = row[AnomalyDetectionConfigs.conversionRateThreshold].toDouble(),
            minSampleSize = row[AnomalyDetectionConfigs.minSampleSize],
            windowSizeMs = row[AnomalyDetectionConfigs.windowSizeMs],
            autoKillSwitch = row[AnomalyDetectionConfigs.autoKillSwitch],
            autoRollback = row[AnomalyDetectionConfigs.autoRollback],
            tenantId = row[AnomalyDetectionConfigs.tenantId]
        )
    }
}
