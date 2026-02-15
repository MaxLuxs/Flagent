package flagent.middleware

import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.NonBlockingStatsDClientBuilder
import flagent.config.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Prometheus metrics middleware
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

    environment.monitor.subscribe(ApplicationStopped) {
        statsdClient.close()
    }

    logger.info { "StatsD metrics enabled: ${AppConfig.statsdHost}:${AppConfig.statsdPort}" }
}
