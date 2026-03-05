package flagent.repository.impl

import flagent.repository.Database
import flagent.repository.tables.AnalyticsEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

/**
 * Repository for analytics events (Firebase-level: first_open, session_start, screen_view, custom).
 */
class AnalyticsEventRepository {

    suspend fun saveBatch(events: List<AnalyticsEventRecord>, tenantId: String? = null): Unit =
        withContext(Dispatchers.IO) {
            if (events.isEmpty()) return@withContext
            Database.transaction {
                events.forEach { e ->
                    AnalyticsEvents.insert {
                        it[AnalyticsEvents.eventName] = e.eventName
                        it[AnalyticsEvents.eventParams] = e.eventParams
                        it[AnalyticsEvents.flagId] = e.flagId
                        it[AnalyticsEvents.variantId] = e.variantId
                        it[AnalyticsEvents.userId] = e.userId
                        it[AnalyticsEvents.sessionId] = e.sessionId
                        it[AnalyticsEvents.platform] = e.platform
                        it[AnalyticsEvents.appVersion] = e.appVersion
                        it[AnalyticsEvents.timestampMs] = e.timestampMs
                        it[AnalyticsEvents.tenantId] = tenantId ?: e.tenantId
                    }
                }
            }
        }

    suspend fun getOverview(
        startMs: Long,
        endMs: Long,
        topLimit: Int = 20,
        timeBucketMs: Long = 3600_000,
        tenantId: String? = null,
        platform: String? = null,
        appVersion: String? = null,
        eventName: String? = null
    ): AnalyticsOverviewResult = withContext(Dispatchers.IO) {
        Database.transaction {
            val tenantCond = tenantId?.let { AnalyticsEvents.tenantId eq it } ?: AnalyticsEvents.tenantId.isNull()
            var whereOp = (AnalyticsEvents.timestampMs greaterEq startMs) and
                (AnalyticsEvents.timestampMs lessEq endMs) and
                tenantCond
            platform?.takeIf { it.isNotBlank() }?.let { p ->
                whereOp = whereOp and (AnalyticsEvents.platform eq p)
            }
            appVersion?.takeIf { it.isNotBlank() }?.let { v ->
                whereOp = whereOp and (AnalyticsEvents.appVersion eq v)
            }
            eventName?.takeIf { it.isNotBlank() }?.let { n ->
                whereOp = whereOp and (AnalyticsEvents.eventName eq n)
            }

            val rows = AnalyticsEvents
                .select(AnalyticsEvents.eventName, AnalyticsEvents.timestampMs, AnalyticsEvents.userId)
                .where { whereOp }
                .map { Triple(it[AnalyticsEvents.eventName], it[AnalyticsEvents.timestampMs], it[AnalyticsEvents.userId]) }

            val totalEvents = rows.size.toLong()
            val distinctUserIds = rows.map { it.third }.filterNotNull().toSet()
            val uniqueUsers = distinctUserIds.size + if (rows.any { it.third == null }) 1 else 0

            val topEvents = rows
                .groupBy { it.first }
                .mapValues { (_, list) -> list.size.toLong() }
                .toList()
                .sortedByDescending { it.second }
                .take(topLimit)
                .map { AnalyticsTopEventEntry(it.first, it.second) }

            val timeSeries = rows
                .groupBy { (_, ts, _) -> (ts / timeBucketMs) * timeBucketMs }
                .map { (bucket, bucketRows) -> TimeSeriesEntry(bucket, bucketRows.size.toLong()) }
                .sortedBy { it.timestamp }

            val dauByDay = rows
                .groupBy { (_, ts, _) -> ts / 86400_000 } // day bucket (ms)
                .map { (dayBucket, list) ->
                    val distinctInDay = list.map { it.third }.filterNotNull().toSet().size
                    val hasAnonymous = list.any { it.third == null }
                    DauEntry(dayBucket * 86400_000, (distinctInDay + if (hasAnonymous) 1 else 0).toLong())
                }
                .sortedBy { it.timestamp }

            AnalyticsOverviewResult(
                totalEvents = totalEvents,
                uniqueUsers = uniqueUsers,
                topEvents = topEvents,
                timeSeries = timeSeries,
                dauByDay = dauByDay
            )
        }
    }

    /**
     * Load events for funnel analysis: event names in steps, optional filters.
     * Returns rows ordered by timestamp for in-memory funnel computation.
     */
    suspend fun getEventsForFunnel(
        startMs: Long,
        endMs: Long,
        eventNames: List<String>,
        platform: String? = null,
        appVersion: String? = null,
        flagId: Int? = null,
        variantId: Int? = null,
        tenantId: String? = null
    ): List<FunnelEventRow> = withContext(Dispatchers.IO) {
        if (eventNames.isEmpty()) return@withContext emptyList()
        Database.transaction {
            val tenantCond = tenantId?.let { AnalyticsEvents.tenantId eq it } ?: AnalyticsEvents.tenantId.isNull()
            var whereOp = (AnalyticsEvents.timestampMs greaterEq startMs) and
                (AnalyticsEvents.timestampMs lessEq endMs) and
                tenantCond and
                (AnalyticsEvents.eventName inList eventNames.distinct())
            platform?.takeIf { it.isNotBlank() }?.let { p ->
                whereOp = whereOp and (AnalyticsEvents.platform eq p)
            }
            appVersion?.takeIf { it.isNotBlank() }?.let { v ->
                whereOp = whereOp and (AnalyticsEvents.appVersion eq v)
            }
            flagId?.let { whereOp = whereOp and (AnalyticsEvents.flagId eq it) }
            variantId?.let { whereOp = whereOp and (AnalyticsEvents.variantId eq it) }
            AnalyticsEvents
                .select(
                    AnalyticsEvents.eventName,
                    AnalyticsEvents.eventParams,
                    AnalyticsEvents.userId,
                    AnalyticsEvents.sessionId,
                    AnalyticsEvents.timestampMs
                )
                .where { whereOp }
                .orderBy(AnalyticsEvents.timestampMs)
                .map {
                    FunnelEventRow(
                        eventName = it[AnalyticsEvents.eventName],
                        eventParams = it[AnalyticsEvents.eventParams],
                        userId = it[AnalyticsEvents.userId],
                        sessionId = it[AnalyticsEvents.sessionId],
                        timestampMs = it[AnalyticsEvents.timestampMs]
                    )
                }
        }
    }

    /**
     * Delete analytics events older than cutoffMs (for retention/cleanup).
     * @return number of deleted rows
     */
    suspend fun deleteOlderThan(cutoffMs: Long): Int = withContext(Dispatchers.IO) {
        Database.transaction {
            AnalyticsEvents.deleteWhere {
                AnalyticsEvents.timestampMs less cutoffMs
            }
        }
    }
}

/** Single row for funnel computation (event name, params, entity ids, timestamp). */
data class FunnelEventRow(
    val eventName: String,
    val eventParams: String?,
    val userId: String?,
    val sessionId: String?,
    val timestampMs: Long
)

@Serializable
data class AnalyticsEventRecord(
    val eventName: String,
    val eventParams: String? = null,
    val flagId: Int? = null,
    val variantId: Int? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val platform: String? = null,
    val appVersion: String? = null,
    val timestampMs: Long,
    val tenantId: String? = null
)

@Serializable
data class AnalyticsOverviewResult(
    val totalEvents: Long,
    val uniqueUsers: Int,
    val topEvents: List<AnalyticsTopEventEntry>,
    val timeSeries: List<TimeSeriesEntry>,
    val dauByDay: List<DauEntry>
)

@Serializable
data class AnalyticsTopEventEntry(
    val eventName: String,
    val count: Long
)

@Serializable
data class DauEntry(
    val timestamp: Long,
    val dau: Long
)
