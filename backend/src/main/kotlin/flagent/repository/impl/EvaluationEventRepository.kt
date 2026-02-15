package flagent.repository.impl

import flagent.repository.Database
import flagent.repository.tables.EvaluationEvents
import flagent.repository.tables.Flags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

/**
 * Core metrics: evaluation events for API evaluation count.
 * Events may include optional clientId (from X-Client-Id) for "who uses this flag" analytics.
 */
class EvaluationEventRepository {

    suspend fun saveBatch(events: List<EvaluationEventRecord>): Unit = withContext(Dispatchers.IO) {
        if (events.isEmpty()) return@withContext
        Database.transaction {
            events.forEach { ev ->
                EvaluationEvents.insert {
                    it[EvaluationEvents.flagId] = ev.flagId
                    it[EvaluationEvents.timestampMs] = ev.timestampMs
                    ev.clientId?.let { cid -> it[EvaluationEvents.clientId] = cid.take(255) }
                }
            }
        }
    }

    suspend fun getOverview(
        startMs: Long,
        endMs: Long,
        topLimit: Int,
        timeBucketMs: Long
    ): MetricsOverviewResult = withContext(Dispatchers.IO) {
        Database.transaction {
            val rows = EvaluationEvents
                .select(EvaluationEvents.flagId, EvaluationEvents.timestampMs)
                .where {
                    (EvaluationEvents.timestampMs greaterEq startMs) and
                        (EvaluationEvents.timestampMs lessEq endMs)
                }
                .map { Pair(it[EvaluationEvents.flagId], it[EvaluationEvents.timestampMs]) }

            val totalEvaluations = rows.size.toLong()
            val uniqueFlags = rows.map { it.first }.toSet().size

            val topFlags = rows
                .groupBy { it.first }
                .mapValues { (_, list) -> list.size.toLong() }
                .toList()
                .sortedByDescending { it.second }
                .take(topLimit)

            val flagIds = topFlags.map { it.first }
            val flagKeys = if (flagIds.isEmpty()) emptyMap<Int, String>()
            else Flags
                .select(Flags.id, Flags.key)
                .where { Flags.id inList flagIds.map { id -> EntityID(id, Flags) } }
                .associate { row -> row[Flags.id].value to row[Flags.key] }

            val topFlagEntries = topFlags.map { (fid, count) ->
                TopFlagEntry(fid, flagKeys[fid] ?: "flag_$fid", count)
            }

            val timeSeries = rows
                .groupBy { (_, ts) -> (ts / timeBucketMs) * timeBucketMs }
                .map { (bucket, bucketRows) -> TimeSeriesEntry(bucket, bucketRows.size.toLong()) }
                .sortedBy { it.timestamp }

            MetricsOverviewResult(
                totalEvaluations = totalEvaluations,
                uniqueFlags = uniqueFlags,
                topFlags = topFlagEntries,
                timeSeries = timeSeries
            )
        }
    }

    /**
     * Delete evaluation events older than cutoffMs (for retention/cleanup).
     * @return number of deleted rows
     */
    suspend fun deleteOlderThan(cutoffMs: Long): Int = withContext(Dispatchers.IO) {
        Database.transaction {
            EvaluationEvents.deleteWhere {
                EvaluationEvents.timestampMs less cutoffMs
            }
        }
    }

    suspend fun getStatsForFlag(
        flagId: Int,
        startMs: Long,
        endMs: Long,
        timeBucketMs: Long
    ): FlagEvaluationStatsResult = withContext(Dispatchers.IO) {
        Database.transaction {
            val rows = EvaluationEvents
                .select(EvaluationEvents.timestampMs)
                .where {
                    (EvaluationEvents.flagId eq flagId) and
                        (EvaluationEvents.timestampMs greaterEq startMs) and
                        (EvaluationEvents.timestampMs lessEq endMs)
                }
                .map { it[EvaluationEvents.timestampMs] }

            val evaluationCount = rows.size.toLong()
            val timeSeries = rows
                .groupBy { (it / timeBucketMs) * timeBucketMs }
                .map { (bucket, list) -> TimeSeriesEntry(bucket, list.size.toLong()) }
                .sortedBy { it.timestamp }

            FlagEvaluationStatsResult(
                flagId = flagId,
                evaluationCount = evaluationCount,
                timeSeries = timeSeries
            )
        }
    }

    /**
     * Returns usage by client (clientId -> evaluation count) for a flag in the time window.
     * Only events with non-null client_id are grouped; total includes all events.
     */
    suspend fun getUsageByClient(
        flagId: Int,
        startMs: Long,
        endMs: Long
    ): FlagUsageByClientResult = withContext(Dispatchers.IO) {
        Database.transaction {
            val rows = EvaluationEvents
                .select(EvaluationEvents.clientId, EvaluationEvents.timestampMs)
                .where {
                    (EvaluationEvents.flagId eq flagId) and
                        (EvaluationEvents.timestampMs greaterEq startMs) and
                        (EvaluationEvents.timestampMs lessEq endMs)
                }
                .map { it[EvaluationEvents.clientId] to it[EvaluationEvents.timestampMs] }

            val totalCount = rows.size.toLong()
            val byClient = rows
                .filter { (clientId, _) -> !clientId.isNullOrBlank() }
                .groupBy { (clientId, _) -> clientId!! }
                .mapValues { (_, list) -> list.size.toLong() }
                .toList()
                .sortedByDescending { it.second }
                .map { (clientId, count) -> ClientUsageEntry(clientId, count) }

            FlagUsageByClientResult(
                flagId = flagId,
                startMs = startMs,
                endMs = endMs,
                totalEvaluationCount = totalCount,
                clients = byClient
            )
        }
    }
}

data class EvaluationEventRecord(
    val flagId: Int,
    val timestampMs: Long,
    val clientId: String? = null
)

@Serializable
data class ClientUsageEntry(
    val clientId: String,
    val evaluationCount: Long
)

@Serializable
data class FlagUsageByClientResult(
    val flagId: Int,
    val startMs: Long,
    val endMs: Long,
    val totalEvaluationCount: Long,
    val clients: List<ClientUsageEntry>
)

@Serializable
data class FlagEvaluationStatsResult(
    val flagId: Int,
    val evaluationCount: Long,
    val timeSeries: List<TimeSeriesEntry>
)

@Serializable
data class MetricsOverviewResult(
    val totalEvaluations: Long,
    val uniqueFlags: Int,
    val topFlags: List<TopFlagEntry>,
    val timeSeries: List<TimeSeriesEntry>
)

@Serializable
data class TopFlagEntry(
    val flagId: Int,
    val flagKey: String,
    val evaluationCount: Long
)

@Serializable
data class TimeSeriesEntry(
    val timestamp: Long,
    val count: Long
)
