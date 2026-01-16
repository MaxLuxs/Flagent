package io.ktor.flagent

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.NonBlockingStatsDClientBuilder
import mu.KotlinLogging
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

/**
 * FlagentMetrics - metrics collection for FlagentPlugin
 * Provides Prometheus and StatsD metrics for evaluation operations
 */
class FlagentMetricsConfig {
    /**
     * Enable Prometheus metrics
     */
    var enablePrometheus: Boolean = false
    
    /**
     * Prometheus scrape path
     */
    var prometheusPath: String = "/metrics"
    
    /**
     * Enable StatsD metrics
     */
    var enableStatsD: Boolean = false
    
    /**
     * StatsD host
     */
    var statsdHost: String = "localhost"
    
    /**
     * StatsD port
     */
    var statsdPort: Int = 8125
    
    /**
     * StatsD prefix
     */
    var statsdPrefix: String = "flagent"
}

/**
 * FlagentMetrics - metrics collection for evaluation operations
 */
class FlagentMetrics(
    private val prometheusRegistry: PrometheusMeterRegistry? = null,
    private val statsdClient: NonBlockingStatsDClient? = null
) {
    private val evaluationCounter: Counter? = prometheusRegistry?.let {
        Counter.builder("flagent.evaluations.total")
            .description("Total number of evaluation requests")
            .register(it)
    }
    
    private val evaluationTimer: Timer? = prometheusRegistry?.let {
        Timer.builder("flagent.evaluations.duration")
            .description("Evaluation request duration")
            .register(it)
    }
    
    private val evaluationErrorCounter: Counter? = prometheusRegistry?.let {
        Counter.builder("flagent.evaluations.errors")
            .description("Total number of evaluation errors")
            .register(it)
    }
    
    private val cacheHitCounter: Counter? = prometheusRegistry?.let {
        Counter.builder("flagent.cache.hits")
            .description("Total number of cache hits")
            .register(it)
    }
    
    private val cacheMissCounter: Counter? = prometheusRegistry?.let {
        Counter.builder("flagent.cache.misses")
            .description("Total number of cache misses")
            .register(it)
    }
    
    /**
     * Record evaluation request
     */
    fun recordEvaluation(flagID: Int?, flagKey: String?, duration: kotlin.time.Duration, fromCache: Boolean = false) {
        // Prometheus metrics
        evaluationCounter?.increment()
        evaluationTimer?.record(duration)
        
        if (fromCache) {
            cacheHitCounter?.increment()
        } else {
            cacheMissCounter?.increment()
        }
        
        // StatsD metrics
        statsdClient?.let { client ->
            val tags = mutableListOf<String>()
            flagID?.let { tags.add("flag_id:$it") }
            flagKey?.let { tags.add("flag_key:$it") }
            tags.add("from_cache:${if (fromCache) "true" else "false"}")
            
            client.increment("flagent.evaluations", *tags.toTypedArray())
            client.recordExecutionTime("flagent.evaluations.duration", duration.inWholeMilliseconds, *tags.toTypedArray())
            
            if (fromCache) {
                client.increment("flagent.cache.hits", *tags.toTypedArray())
            } else {
                client.increment("flagent.cache.misses", *tags.toTypedArray())
            }
        }
    }
    
    /**
     * Record evaluation error
     */
    fun recordEvaluationError(flagID: Int?, flagKey: String?, errorType: String = "unknown") {
        // Prometheus metrics
        evaluationErrorCounter?.increment(
            io.micrometer.core.instrument.Tags.of(
                "error_type", errorType,
                "flag_id", flagID?.toString() ?: "unknown",
                "flag_key", flagKey ?: "unknown"
            )
        )
        
        // StatsD metrics
        statsdClient?.let { client ->
            val tags = mutableListOf<String>()
            flagID?.let { tags.add("flag_id:$it") }
            flagKey?.let { tags.add("flag_key:$it") }
            tags.add("error_type:$errorType")
            
            client.increment("flagent.evaluations.errors", *tags.toTypedArray())
        }
    }
    
    /**
     * Record batch evaluation
     */
    fun recordBatchEvaluation(count: Int, duration: kotlin.time.Duration, errors: Int = 0) {
        // Prometheus metrics
        prometheusRegistry?.let { registry ->
            Counter.builder("flagent.evaluations.batch.total")
                .description("Total number of batch evaluation requests")
                .register(registry)
                .increment()
            
            Counter.builder("flagent.evaluations.batch.size")
                .description("Batch evaluation size")
                .register(registry)
                .increment(count.toDouble())
            
            if (errors > 0) {
                Counter.builder("flagent.evaluations.batch.errors")
                    .description("Total number of batch evaluation errors")
                    .register(registry)
                    .increment(errors.toDouble())
            }
        }
        
        // StatsD metrics
        statsdClient?.let { client ->
            val tags = arrayOf(
                "batch_size:$count",
                "errors:$errors"
            )
            
            client.increment("flagent.evaluations.batch", *tags)
            client.recordExecutionTime("flagent.evaluations.batch.duration", duration.inWholeMilliseconds, *tags)
            
            if (errors > 0) {
                client.increment("flagent.evaluations.batch.errors", *tags)
            }
        }
    }
}

/**
 * Configure FlagentMetrics in Application
 */
fun Application.configureFlagentMetrics(config: FlagentMetricsConfig): FlagentMetrics? {
    if (!config.enablePrometheus && !config.enableStatsD) {
        return null
    }
    
    val prometheusRegistry = if (config.enablePrometheus) {
        val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        
        install(MicrometerMetrics) {
            this.registry = registry
        }
        
        // Ensure Routing plugin is installed
        if (application.pluginOrNull(Routing) == null) {
            application.install(Routing)
        }
        
        application.routing {
            get(config.prometheusPath) {
                call.respond(registry.scrape())
            }
        }
        
        logger.info { "Prometheus metrics enabled at ${config.prometheusPath}" }
        registry
    } else {
        null
    }
    
    val statsdClient = if (config.enableStatsD) {
        val client = NonBlockingStatsDClientBuilder()
            .prefix(config.statsdPrefix)
            .hostname(config.statsdHost)
            .port(config.statsdPort)
            .build()
        
        environment.monitor.subscribe(io.ktor.server.application.ApplicationStopped) {
            client.close()
        }
        
        logger.info { "StatsD metrics enabled: ${config.statsdHost}:${config.statsdPort}" }
        client
    } else {
        null
    }
    
    return FlagentMetrics(prometheusRegistry, statsdClient)
}
