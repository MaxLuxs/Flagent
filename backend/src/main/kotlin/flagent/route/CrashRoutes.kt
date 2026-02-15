package flagent.route

import flagent.api.constants.ApiConstants
import flagent.domain.entity.CrashReport
import flagent.service.CrashReportService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("CrashRoutes")

@Serializable
data class CrashReportRequest(
    val stackTrace: String,
    val message: String,
    val platform: String,
    val appVersion: String? = null,
    val deviceInfo: String? = null,
    val breadcrumbs: String? = null,
    val customKeys: String? = null,
    /** Keys of flags that were active when the crash occurred. */
    val activeFlagKeys: List<String>? = null,
    val timestamp: Long? = null
)

@Serializable
data class CrashReportResponse(
    val id: Long,
    val stackTrace: String,
    val message: String,
    val platform: String,
    val appVersion: String?,
    val deviceInfo: String?,
    val breadcrumbs: String?,
    val customKeys: String?,
    val activeFlagKeys: List<String>? = null,
    val timestamp: Long,
    val tenantId: String?
)

@Serializable
data class CrashListResponse(
    val items: List<CrashReportResponse>,
    val total: Long
)

fun Routing.configureCrashRoutes(crashReportService: CrashReportService) {
    route(ApiConstants.API_BASE_PATH) {
        post("/crashes") {
            try {
                val body = call.receive<CrashReportRequest>()
                val timestamp = body.timestamp ?: System.currentTimeMillis()
                val crash = CrashReport(
                    stackTrace = body.stackTrace,
                    message = body.message,
                    platform = body.platform,
                    appVersion = body.appVersion,
                    deviceInfo = body.deviceInfo,
                    breadcrumbs = body.breadcrumbs,
                    customKeys = body.customKeys,
                    activeFlagKeys = body.activeFlagKeys,
                    timestamp = timestamp,
                    tenantId = null
                )
                val saved = crashReportService.save(crash)
                call.respond(HttpStatusCode.Created, toResponse(saved))
            } catch (e: kotlinx.serialization.SerializationException) {
                logger.warn("POST /crashes invalid JSON: {}", e.message)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid JSON: ${e.message}")
                )
            } catch (e: Exception) {
                logger.error("POST /crashes failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to save crash report")
                )
            }
        }

        post("/crashes/batch") {
            try {
                val body = call.receive<List<CrashReportRequest>>()
                val timestamp = System.currentTimeMillis()
                val crashes = body.map { req ->
                    CrashReport(
                        stackTrace = req.stackTrace,
                        message = req.message,
                        platform = req.platform,
                        appVersion = req.appVersion,
                        deviceInfo = req.deviceInfo,
                        breadcrumbs = req.breadcrumbs,
                        customKeys = req.customKeys,
                        activeFlagKeys = req.activeFlagKeys,
                        timestamp = req.timestamp ?: timestamp,
                        tenantId = null
                    )
                }
                val saved = crashReportService.saveBatch(crashes)
                call.respond(HttpStatusCode.Created, saved.map { toResponse(it) })
            } catch (e: kotlinx.serialization.SerializationException) {
                logger.warn("POST /crashes/batch invalid JSON: {}", e.message)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid JSON: ${e.message}")
                )
            } catch (e: Exception) {
                logger.error("POST /crashes/batch failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to save crash reports")
                )
            }
        }

        get("/crashes") {
            try {
                val startTime = call.request.queryParameters["start"]?.toLongOrNull()
                val endTime = call.request.queryParameters["end"]?.toLongOrNull()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val items = crashReportService.list(null, startTime, endTime, limit, offset)
                val total = crashReportService.count(null, startTime, endTime)
                call.respond(
                    HttpStatusCode.OK, CrashListResponse(
                        items = items.map { toResponse(it) },
                        total = total
                    )
                )
            } catch (e: Exception) {
                logger.error("GET /crashes failed", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to list crash reports")
                )
            }
        }
    }
}

private fun toResponse(c: CrashReport) = CrashReportResponse(
    id = c.id,
    stackTrace = c.stackTrace,
    message = c.message,
    platform = c.platform,
    appVersion = c.appVersion,
    deviceInfo = c.deviceInfo,
    breadcrumbs = c.breadcrumbs,
    customKeys = c.customKeys,
    activeFlagKeys = c.activeFlagKeys,
    timestamp = c.timestamp,
    tenantId = c.tenantId
)
