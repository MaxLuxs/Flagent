package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.NonBlockingStatsDClientBuilder
import io.ktor.http.*
import io.ktor.server.request.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Prometheus metrics middleware
 * Maps to pkg/config/middleware.go prometheusMiddleware
 */
fun Application.configurePrometheusMetrics() {
    if (!AppConfig.prometheusEnabled) return
    
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    
    routing {
        get(AppConfig.prometheusPath) {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
    
    logger.info { "Prometheus metrics enabled at ${AppConfig.prometheusPath}" }
}

/**
 * StatsD metrics middleware
 * Maps to pkg/config/middleware.go statsdMiddleware
 */
class StatsDMetricsMiddleware(
    private val statsdClient: NonBlockingStatsDClient
) {
    fun recordRequest(method: String, path: String, statusCode: Int, duration: Long) {
        val tags = arrayOf(
            "method:$method",
            "path:$path",
            "status:${statusCode}"
        )
        
        statsdClient.increment("flagent.requests", *tags)
        statsdClient.recordExecutionTime("flagent.request.duration", duration, *tags)
        
        if (statusCode >= 400) {
            statsdClient.increment("flagent.errors", *tags)
        }
    }
    
    fun recordEvaluation(flagID: Int, variantID: Int?) {
        val tags = arrayOf(
            "flag_id:$flagID",
            "variant_id:${variantID ?: "none"}"
        )
        statsdClient.increment("flagent.evaluations", *tags)
    }
}

fun Application.configureStatsDMetrics() {
    if (!AppConfig.statsdEnabled) return
    
    val statsdClient = NonBlockingStatsDClientBuilder()
        .prefix(AppConfig.statsdPrefix)
        .hostname(AppConfig.statsdHost)
        .port(AppConfig.statsdPort.toInt())
        .build()
    
    environment.monitor.subscribe(io.ktor.server.application.ApplicationStopped) {
        statsdClient.close()
    }
    
    logger.info { "StatsD metrics enabled: ${AppConfig.statsdHost}:${AppConfig.statsdPort}" }
}
