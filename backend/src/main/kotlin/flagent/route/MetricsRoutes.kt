package flagent.route

import flagent.domain.entity.MetricDataPoint
import flagent.service.GlobalMetricsOverview
import flagent.service.MetricsCollectionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Metrics routes - API for metrics collection and retrieval
 */
fun Routing.configureMetricsRoutes(metricsService: MetricsCollectionService) {
    route("/api/v1/metrics") {
        // Global overview: time series + top flags (must be before /{flagId})
        get("overview") {
            val startTime = call.request.queryParameters["start_time"]?.toLongOrNull()
                ?: (System.currentTimeMillis() - 86400_000) // Default: last 24h
            val endTime = call.request.queryParameters["end_time"]?.toLongOrNull()
                ?: System.currentTimeMillis()
            val bucketMinutes = call.request.queryParameters["bucket_minutes"]?.toIntOrNull() ?: 60
            val topFlagsLimit = call.request.queryParameters["top_flags_limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10
            val tenantId = call.request.queryParameters["tenant_id"]

            val overview = metricsService.getGlobalOverview(
                startTime = startTime,
                endTime = endTime,
                bucketMs = bucketMinutes * 60_000L,
                topFlagsLimit = topFlagsLimit,
                tenantId = tenantId
            )
            call.respond(overview)
        }

        // Collect a single metric
        post {
            val request = call.receive<MetricDataPointRequest>()
            
            val metric = MetricDataPoint(
                flagId = request.flagId,
                flagKey = request.flagKey,
                segmentId = request.segmentId,
                variantId = request.variantId,
                variantKey = request.variantKey,
                metricType = request.metricType,
                metricValue = request.metricValue,
                timestamp = request.timestamp ?: System.currentTimeMillis(),
                entityId = request.entityId,
                tenantId = request.tenantId
            )
            
            val result = metricsService.collectMetric(metric)
            
            if (result.isSuccess) {
                call.respond(HttpStatusCode.Created, mapMetricToResponse(result.getOrThrow()))
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (result.exceptionOrNull()?.message ?: "Invalid metric"))
                )
            }
        }
        
        // Collect multiple metrics in batch
        post("/batch") {
            val requests = call.receive<List<MetricDataPointRequest>>()
            
            val metrics = requests.map { request ->
                MetricDataPoint(
                    flagId = request.flagId,
                    flagKey = request.flagKey,
                    segmentId = request.segmentId,
                    variantId = request.variantId,
                    variantKey = request.variantKey,
                    metricType = request.metricType,
                    metricValue = request.metricValue,
                    timestamp = request.timestamp ?: System.currentTimeMillis(),
                    entityId = request.entityId,
                    tenantId = request.tenantId
                )
            }
            
            val result = metricsService.collectMetricsBatch(metrics)
            
            call.respond(HttpStatusCode.OK, mapOf(
                "successful" to result.successCount,
                "failed" to result.failureCount,
                "total" to result.totalCount,
                "success_rate" to result.successRate,
                "errors" to result.failed
            ))
        }
        
        // Get metrics for a flag
        get("/{flagId}") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            
            val startTime = call.request.queryParameters["start_time"]?.toLongOrNull()
                ?: (System.currentTimeMillis() - 3600_000) // Default: last hour
            val endTime = call.request.queryParameters["end_time"]?.toLongOrNull()
                ?: System.currentTimeMillis()
            val metricType = call.request.queryParameters["metric_type"]?.let {
                try {
                    MetricDataPoint.MetricType.valueOf(it)
                } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid metric type"))
                }
            }
            val variantId = call.request.queryParameters["variant_id"]?.toIntOrNull()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val metrics = metricsService.getMetricsByFlagId(
                flagId = flagId,
                startTime = startTime,
                endTime = endTime,
                metricType = metricType,
                variantId = variantId,
                tenantId = tenantId
            )
            
            call.respond(metrics.map { mapMetricToResponse(it) })
        }
        
        // Get aggregated metrics for a flag
        get("/{flagId}/aggregation") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            
            val metricTypeStr = call.request.queryParameters["metric_type"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "metric_type is required"))
            
            val metricType = try {
                MetricDataPoint.MetricType.valueOf(metricTypeStr)
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid metric type"))
            }
            
            val windowStartMs = call.request.queryParameters["window_start"]?.toLongOrNull()
                ?: (System.currentTimeMillis() - 3600_000) // Default: last hour
            val windowEndMs = call.request.queryParameters["window_end"]?.toLongOrNull()
                ?: System.currentTimeMillis()
            val variantId = call.request.queryParameters["variant_id"]?.toIntOrNull()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val aggregation = metricsService.getAggregation(
                flagId = flagId,
                metricType = metricType,
                windowStartMs = windowStartMs,
                windowEndMs = windowEndMs,
                variantId = variantId,
                tenantId = tenantId
            )
            
            if (aggregation != null) {
                call.respond(aggregation)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No metrics found"))
            }
        }
        
        // Count metrics for a flag
        get("/{flagId}/count") {
            val flagId = call.parameters["flagId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid flag ID"))
            
            val startTime = call.request.queryParameters["start_time"]?.toLongOrNull()
                ?: (System.currentTimeMillis() - 3600_000)
            val endTime = call.request.queryParameters["end_time"]?.toLongOrNull()
                ?: System.currentTimeMillis()
            val metricType = call.request.queryParameters["metric_type"]?.let {
                try {
                    MetricDataPoint.MetricType.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            val variantId = call.request.queryParameters["variant_id"]?.toIntOrNull()
            val tenantId = call.request.queryParameters["tenant_id"]
            
            val count = metricsService.countMetrics(
                flagId = flagId,
                startTime = startTime,
                endTime = endTime,
                metricType = metricType,
                variantId = variantId,
                tenantId = tenantId
            )
            
            call.respond(mapOf("count" to count))
        }
    }
}

/**
 * Request model for metric data point
 */
@Serializable
data class MetricDataPointRequest(
    val flagId: Int,
    val flagKey: String,
    val segmentId: Int? = null,
    val variantId: Int? = null,
    val variantKey: String? = null,
    val metricType: MetricDataPoint.MetricType,
    val metricValue: Double,
    val timestamp: Long? = null,
    val entityId: String? = null,
    val tenantId: String? = null
)

/**
 * Response model for metric data point
 */
@Serializable
data class MetricDataPointResponse(
    val id: Long,
    val flagId: Int,
    val flagKey: String,
    val segmentId: Int?,
    val variantId: Int?,
    val variantKey: String?,
    val metricType: MetricDataPoint.MetricType,
    val metricValue: Double,
    val timestamp: Long,
    val entityId: String?,
    val tenantId: String?
)

/**
 * Map MetricDataPoint to response
 */
private fun mapMetricToResponse(metric: MetricDataPoint): MetricDataPointResponse {
    return MetricDataPointResponse(
        id = metric.id,
        flagId = metric.flagId,
        flagKey = metric.flagKey,
        segmentId = metric.segmentId,
        variantId = metric.variantId,
        variantKey = metric.variantKey,
        metricType = metric.metricType,
        metricValue = metric.metricValue,
        timestamp = metric.timestamp,
        entityId = metric.entityId,
        tenantId = metric.tenantId
    )
}
