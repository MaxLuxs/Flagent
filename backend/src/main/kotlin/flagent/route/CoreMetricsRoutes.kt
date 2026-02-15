package flagent.route

import flagent.api.constants.ApiConstants
import flagent.service.CoreMetricsService
import flagent.service.FlagService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Core metrics routes - evaluation count overview (OSS only, when enterprise absent).
 */
fun Routing.configureCoreMetricsRoutes(
    coreMetricsService: CoreMetricsService,
    flagService: FlagService? = null
) {
    route(ApiConstants.API_BASE_PATH) {
        get("/metrics/overview") {
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
            val topLimit = call.request.queryParameters["topLimit"]?.toIntOrNull() ?: 10
            val timeBucketMs =
                call.request.queryParameters["timeBucketMs"]?.toLongOrNull() ?: 3600_000

            val overview = coreMetricsService.getOverview(start, end, topLimit, timeBucketMs)
            call.respond(HttpStatusCode.OK, overview)
        }
        get("/flags/{flagId}/evaluation-stats") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flagId"))
                    return@get
                }
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
            val timeBucketMs =
                call.request.queryParameters["timeBucketMs"]?.toLongOrNull() ?: 3600_000

            if (flagService != null && flagService.getFlag(flagId) == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Flag not found"))
                return@get
            }
            val stats = coreMetricsService.getFlagStats(flagId, start, end, timeBucketMs)
            call.respond(HttpStatusCode.OK, stats)
        }
        get("/flags/{flagId}/usage") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flagId"))
                    return@get
                }
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
            if (flagService != null && flagService.getFlag(flagId) == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Flag not found"))
                return@get
            }
            val usage = coreMetricsService.getFlagUsage(flagId, start, end)
            call.respond(HttpStatusCode.OK, usage)
        }
    }
}
