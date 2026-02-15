package flagent.repository.impl

import flagent.domain.entity.CrashReport
import flagent.repository.Database
import flagent.repository.tables.CrashReports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.time.LocalDateTime

/**
 * OSS crash report repository. tenantId is always null for OSS (single-tenant).
 */
class CrashReportRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        Database.transaction { block() }
    }

    suspend fun save(crash: CrashReport): CrashReport = dbQuery {
        val id = CrashReports.insert {
            it[stackTrace] = crash.stackTrace
            it[message] = crash.message
            it[platform] = crash.platform
            it[appVersion] = crash.appVersion
            it[deviceInfo] = crash.deviceInfo
            it[breadcrumbs] = crash.breadcrumbs
            it[customKeys] = crash.customKeys
            it[activeFlagKeys] = crash.activeFlagKeys?.let { Json.encodeToString(it) }
            it[timestamp] = crash.timestamp
            it[tenantId] = crash.tenantId
            it[createdAt] = LocalDateTime.now()
        }[CrashReports.id].value
        crash.copy(id = id)
    }

    suspend fun saveBatch(crashes: List<CrashReport>): List<CrashReport> = dbQuery {
        crashes.map { c ->
            val id = CrashReports.insert {
                it[stackTrace] = c.stackTrace
                it[message] = c.message
                it[platform] = c.platform
                it[appVersion] = c.appVersion
                it[deviceInfo] = c.deviceInfo
                it[breadcrumbs] = c.breadcrumbs
                it[customKeys] = c.customKeys
                it[activeFlagKeys] = c.activeFlagKeys?.let { Json.encodeToString(it) }
                it[timestamp] = c.timestamp
                it[tenantId] = c.tenantId
                it[createdAt] = LocalDateTime.now()
            }[CrashReports.id].value
            c.copy(id = id)
        }
    }

    suspend fun list(
        tenantId: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): List<CrashReport> = dbQuery {
        val cond = buildList {
            add(tenantId?.let { CrashReports.tenantId eq it } ?: CrashReports.tenantId.isNull())
            startTime?.let { add(CrashReports.timestamp greaterEq it) }
            endTime?.let { add(CrashReports.timestamp lessEq it) }
        }
        val whereOp = cond.reduce { a, b -> a and b }
        CrashReports.selectAll()
            .where { whereOp }
            .orderBy(CrashReports.timestamp, SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { it.toCrashReport() }
    }

    suspend fun count(tenantId: String?, startTime: Long?, endTime: Long?): Long = dbQuery {
        val cond = buildList {
            add(tenantId?.let { CrashReports.tenantId eq it } ?: CrashReports.tenantId.isNull())
            startTime?.let { add(CrashReports.timestamp greaterEq it) }
            endTime?.let { add(CrashReports.timestamp lessEq it) }
        }
        val whereOp = cond.reduce { a, b -> a and b }
        CrashReports.select(CrashReports.id).where { whereOp }.count().toLong()
    }

    private fun ResultRow.toCrashReport(): CrashReport {
        val activeFlagKeysRaw = this[CrashReports.activeFlagKeys]
        val activeFlagKeys = activeFlagKeysRaw?.let {
            try {
                Json.decodeFromString<List<String>>(it)
            } catch (_: Exception) {
                emptyList<String>()
            }
        }
        return CrashReport(
            id = this[CrashReports.id].value,
            stackTrace = this[CrashReports.stackTrace],
            message = this[CrashReports.message],
            platform = this[CrashReports.platform],
            appVersion = this[CrashReports.appVersion],
            deviceInfo = this[CrashReports.deviceInfo],
            breadcrumbs = this[CrashReports.breadcrumbs],
            customKeys = this[CrashReports.customKeys],
            activeFlagKeys = activeFlagKeys,
            timestamp = this[CrashReports.timestamp],
            tenantId = this[CrashReports.tenantId]
        )
    }
}
