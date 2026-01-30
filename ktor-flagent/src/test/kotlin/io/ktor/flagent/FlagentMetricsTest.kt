package io.ktor.flagent

import kotlin.time.Duration.Companion.milliseconds
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class FlagentMetricsTest {

    @Test
    fun `FlagentMetrics with null registries does not throw on recordEvaluation`() {
        val metrics = FlagentMetrics(prometheusRegistry = null, statsdClient = null)
        metrics.recordEvaluation(flagID = 1, flagKey = "test", duration = 10.milliseconds, fromCache = false)
        metrics.recordEvaluation(flagID = 2, flagKey = "other", duration = 5.milliseconds, fromCache = true)
    }

    @Test
    fun `FlagentMetrics with null registries does not throw on recordEvaluationError`() {
        val metrics = FlagentMetrics(prometheusRegistry = null, statsdClient = null)
        metrics.recordEvaluationError(flagID = 1, flagKey = "test", errorType = "timeout")
    }

    @Test
    fun `FlagentMetrics with null registries does not throw on recordBatchEvaluation`() {
        val metrics = FlagentMetrics(prometheusRegistry = null, statsdClient = null)
        metrics.recordBatchEvaluation(count = 5, duration = 100.milliseconds, errors = 0)
        metrics.recordBatchEvaluation(count = 3, duration = 50.milliseconds, errors = 1)
    }

    @Test
    fun `FlagentMetricsConfig has sensible defaults`() {
        val config = FlagentMetricsConfig()
        assertFalse(config.enablePrometheus)
        assertFalse(config.enableStatsD)
        assertEquals("/metrics", config.prometheusPath)
        assertEquals("localhost", config.statsdHost)
        assertEquals(8125, config.statsdPort)
    }
}
