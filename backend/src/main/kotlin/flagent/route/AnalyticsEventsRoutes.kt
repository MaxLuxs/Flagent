package flagent.route

import flagent.api.constants.ApiConstants
import flagent.repository.impl.AnalyticsEventRecord
import flagent.service.AnalyticsEventsService
import flagent.service.FunnelAnalyticsService
import flagent.service.FunnelRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AnalyticsEventsRoutes")

@Serializable
data class AnalyticsEventRequest(
    val eventName: String,
    val eventParams: String? = null,
    val flagId: Int? = null,
    val variantId: Int? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val platform: String? = null,
    val appVersion: String? = null,
    val timestampMs: Long? = null
)

@Serializable
data class AnalyticsEventsBatchRequest(
    val events: List<AnalyticsEventRequest>
)

/**
 * Analytics events routes - Firebase-level event tracking.
 * POST /analytics/events (batch), GET /analytics/overview.
 */
fun Routing.configureAnalyticsEventsRoutes(
    analyticsEventsService: AnalyticsEventsService,
    tenantIdProvider: (ApplicationCall) -> String? = { null }
) {
    route(ApiConstants.API_BASE_PATH) {
        post("/analytics/events") {
            try {
                val tenantId = tenantIdProvider(call)
                val body = call.receive<AnalyticsEventsBatchRequest>()
                if (body.events.isEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "events list is empty")
                    )
                    return@post
                }
                val now = System.currentTimeMillis()
                val records = body.events.map { req ->
                    AnalyticsEventRecord(
                        eventName = req.eventName.take(128),
                        eventParams = req.eventParams?.take(65535)?.takeIf { it.isNotEmpty() },
                        flagId = req.flagId,
                        variantId = req.variantId,
                        userId = req.userId?.take(255),
                        sessionId = req.sessionId?.take(255),
                        platform = req.platform?.take(32),
                        appVersion = req.appVersion?.take(64),
                        timestampMs = req.timestampMs ?: now,
                        tenantId = tenantId
                    )
                }
                analyticsEventsService.collectEvents(records, tenantId)
                call.respond(HttpStatusCode.OK, mapOf("accepted" to records.size))
            } catch (e: Exception) {
                logger.error("Analytics events batch failed", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid request"))
                )
            }
        }

        get("/analytics/overview") {
            try {
                val tenantId = tenantIdProvider(call)
                val start = call.request.queryParameters["start"]?.toLongOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing start"))
                        return@get
                    }
                val end = call.request.queryParameters["end"]?.toLongOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing end"))
                        return@get
                    }
                val topLimit = call.request.queryParameters["topLimit"]?.toIntOrNull() ?: 20
                val timeBucketMs =
                    call.request.queryParameters["timeBucketMs"]?.toLongOrNull() ?: 3600_000
                val platform = call.request.queryParameters["platform"]?.takeIf { it.isNotBlank() }
                val appVersion = call.request.queryParameters["appVersion"]?.takeIf { it.isNotBlank() }
                val eventName = call.request.queryParameters["eventName"]?.takeIf { it.isNotBlank() }

                val overview = analyticsEventsService.getOverview(
                    start, end, topLimit, timeBucketMs, tenantId,
                    platform, appVersion, eventName
                )
                call.respond(HttpStatusCode.OK, overview)
            } catch (e: Exception) {
                logger.error("Analytics overview failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Overview failed")
                )
            }
        }
    }
}

/**
 * Funnel analytics routes. POST /analytics/funnel with body FunnelRequest.
 */
fun Routing.configureAnalyticsFunnelRoutes(
    funnelAnalyticsService: FunnelAnalyticsService,
    tenantIdProvider: (ApplicationCall) -> String? = { null }
) {
    route(ApiConstants.API_BASE_PATH) {
        post("/analytics/funnel") {
            try {
                val tenantId = tenantIdProvider(call)
                val body = call.receive<FunnelRequest>()
                if (body.steps.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "steps must not be empty"))
                    return@post
                }
                if (body.startMs >= body.endMs) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "start must be less than end"))
                    return@post
                }
                val request = body.copy(tenantId = tenantId ?: body.tenantId)
                val result = funnelAnalyticsService.computeFunnel(request)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
            } catch (e: Exception) {
                logger.error("Funnel computation failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Funnel computation failed")
                )
            }
        }
    }
}
