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
        tenantId: String? = null
    ): AnalyticsOverviewResult = withContext(Dispatchers.IO) {
        Database.transaction {
            val tenantCond = tenantId?.let { AnalyticsEvents.tenantId eq it } ?: AnalyticsEvents.tenantId.isNull()
            val whereOp = (AnalyticsEvents.timestampMs greaterEq startMs) and
                (AnalyticsEvents.timestampMs lessEq endMs) and
                tenantCond

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

@Serializable
data class AnalyticsEventRecord(
    val eventName: String,
    val eventParams: String? = null,
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
