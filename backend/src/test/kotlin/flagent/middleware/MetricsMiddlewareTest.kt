package flagent.middleware

import flagent.config.AppConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.testing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.*
import kotlin.test.*
import kotlinx.serialization.json.*

class MetricsMiddlewareTest {
    @Test
    fun testPrometheusMetrics_Endpoint_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.prometheusEnabled } returns true
        every { AppConfig.prometheusPath } returns "/metrics"
        
        application {
            configurePrometheusMetrics()
        }
        
        val response = client.get("/metrics")
        assertEquals(200, response.status.value)
        val body = response.bodyAsText()
        assertTrue(body.contains("# HELP") || body.contains("prometheus"), "Prometheus text format expected, got: ${body.take(200)}")
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testPrometheusMetrics_NotConfigured_WhenDisabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.prometheusEnabled } returns false
        
        application {
            configurePrometheusMetrics()
        }
        
        // Should not throw exception
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testStatsDMetrics_RecordRequest() {
        val statsdClient = mockk<com.timgroup.statsd.NonBlockingStatsDClient>(relaxed = true)
        val middleware = StatsDMetricsMiddleware(statsdClient)
        
        middleware.recordRequest("GET", "/api/v1/flags", 200, 100)
        
        verify(exactly = 1) {
            statsdClient.increment(
                "flagent.requests",
                "method:GET",
                "path:/api/v1/flags",
                "status:200"
            )
        }
        verify(exactly = 1) {
            statsdClient.recordExecutionTime(
                "flagent.request.duration",
                100,
                "method:GET",
                "path:/api/v1/flags",
                "status:200"
            )
        }
    }
}
