package flagent.service

import flagent.repository.impl.AnalyticsEventRepository
import flagent.repository.impl.FunnelEventRow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Funnel analytics: compute step progression (event sequence) per entity (user or session).
 */
class FunnelAnalyticsService(
    private val analyticsEventRepository: AnalyticsEventRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun computeFunnel(request: FunnelRequest): FunnelResult {
        if (request.steps.isEmpty()) {
            return FunnelResult(steps = emptyList())
        }
        val eventNames = request.steps.map { it.eventName }.distinct()
        val maxRangeMs = 90 * 24 * 3600 * 1000L
        if (request.endMs - request.startMs > maxRangeMs) {
            throw IllegalArgumentException("Time range must not exceed 90 days")
        }
        val rows = analyticsEventRepository.getEventsForFunnel(
            startMs = request.startMs,
            endMs = request.endMs,
            eventNames = eventNames,
            platform = request.platform,
            appVersion = request.appVersion,
            flagId = request.flagId,
            variantId = request.variantId,
            tenantId = request.tenantId
        )
        val entityKeyFn: (FunnelEventRow) -> String = when (request.entityDimension.uppercase()) {
            "USER_ID" -> { r -> r.userId ?: "anon_${r.sessionId ?: r.timestampMs}" }
            "SESSION_ID" -> { r -> r.sessionId ?: "anon_${r.userId ?: r.timestampMs}" }
            else -> { r -> r.userId ?: r.sessionId ?: "anon_${r.timestampMs}" }
        }
        val byEntity = rows.groupBy(entityKeyFn).mapValues { (_, list) -> list.sortedBy { it.timestampMs } }
        val stepResults = request.steps.mapIndexed { index, step ->
            val reachedCount = byEntity.count { (_, events) ->
                matchesFunnelUpToStep(events, request.steps, index + 1)
            }
            val prevReached = if (index == 0) reachedCount else byEntity.count { (_, events) ->
                matchesFunnelUpToStep(events, request.steps, index)
            }
            val conversionFromPrevious = if (prevReached > 0) reachedCount.toDouble() / prevReached else 0.0
            FunnelStepResult(
                stepIndex = index,
                eventName = step.eventName,
                reachedCount = reachedCount,
                conversionFromPrevious = conversionFromPrevious
            )
        }
        return FunnelResult(steps = stepResults)
    }

    private fun matchesFunnelUpToStep(events: List<FunnelEventRow>, steps: List<FunnelStepRequest>, upToStep: Int): Boolean {
        var lastMatchedTime = 0L
        for (i in 0 until upToStep) {
            val step = steps.getOrNull(i) ?: return false
            val idx = events.indexOfFirst { e ->
                e.timestampMs >= lastMatchedTime &&
                    e.eventName == step.eventName &&
                    matchEventParamFilter(e.eventParams, step.eventParamFilter)
            }
            if (idx < 0) return false
            lastMatchedTime = events[idx].timestampMs
        }
        return true
    }

    private fun matchEventParamFilter(eventParams: String?, filter: Map<String, String>?): Boolean {
        if (filter.isNullOrEmpty()) return true
        if (eventParams.isNullOrBlank()) return false
        return try {
            val obj = json.parseToJsonElement(eventParams).jsonObject
            filter.entries.all { (key, value) ->
                obj[key]?.jsonPrimitive?.content == value
            }
        } catch (_: Exception) {
            false
        }
    }
}

@Serializable
data class FunnelStepRequest(
    val eventName: String,
    val eventParamFilter: Map<String, String>? = null
)

@Serializable
data class FunnelRequest(
    val steps: List<FunnelStepRequest>,
    val startMs: Long,
    val endMs: Long,
    val entityDimension: String = "USER_ID",
    val platform: String? = null,
    val appVersion: String? = null,
    val flagId: Int? = null,
    val variantId: Int? = null,
    val tenantId: String? = null
)

@Serializable
data class FunnelStepResult(
    val stepIndex: Int,
    val eventName: String,
    val reachedCount: Int,
    val conversionFromPrevious: Double
)

@Serializable
data class FunnelResult(
    val steps: List<FunnelStepResult>
)
