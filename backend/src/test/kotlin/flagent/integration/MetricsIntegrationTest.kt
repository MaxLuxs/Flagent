package flagent.integration

import flagent.domain.entity.MetricDataPoint
import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.MetricsRepository
import flagent.route.MetricDataPointRequest
import flagent.service.FlagService
import flagent.service.MetricsCollectionService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for Metrics Collection API
 * 
 * Tests end-to-end flow: HTTP Request → Route → Service → Repository → Database
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetricsIntegrationTest {
    
    @BeforeAll
    fun setup() {
        // Initialize test database
        Database.init()
        
        // Create test flag
        val flagRepository = FlagRepository()
        val flagService = FlagService(flagRepository)
        
        testApplication {
            kotlinx.coroutines.runBlocking {
                flagService.createFlag(
                    flagent.domain.entity.Flag(
                        key = "test_metrics_flag",
                        description = "Test flag for metrics integration test"
                    )
                )
            }
        }
    }
    
    @AfterAll
    fun teardown() {
        Database.close()
    }
    
    @Test
    fun `should collect single metric successfully`() = testApplication {
        // Given
        val metricRequest = MetricDataPointRequest(
            flagId = 1,
            flagKey = "test_metrics_flag",
            segmentId = null,
            variantId = null,
            variantKey = null,
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            metricValue = 0.95,
            timestamp = System.currentTimeMillis(),
            entityId = "test_user_1"
        )
        
        // When
        val response = client.post("/api/v1/metrics") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(MetricDataPointRequest.serializer(), metricRequest))
        }
        
        // Then
        assertEquals(HttpStatusCode.Created, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"flagId\":1"))
        assertTrue(responseBody.contains("\"metricType\":\"SUCCESS_RATE\""))
    }
    
    @Test
    fun `should collect batch metrics successfully`() = testApplication {
        // Given
        val metrics = listOf(
            MetricDataPointRequest(
                flagId = 1,
                flagKey = "test_metrics_flag",
                metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                metricValue = 0.96,
                timestamp = System.currentTimeMillis()
            ),
            MetricDataPointRequest(
                flagId = 1,
                flagKey = "test_metrics_flag",
                metricType = MetricDataPoint.MetricType.ERROR_RATE,
                metricValue = 0.02,
                timestamp = System.currentTimeMillis()
            ),
            MetricDataPointRequest(
                flagId = 1,
                flagKey = "test_metrics_flag",
                metricType = MetricDataPoint.MetricType.LATENCY_MS,
                metricValue = 125.5,
                timestamp = System.currentTimeMillis()
            )
        )
        
        // When
        val response = client.post("/api/v1/metrics/batch") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(MetricDataPointRequest.serializer()), metrics))
        }
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"successful\":3"))
        assertTrue(responseBody.contains("\"failed\":0"))
    }
    
    @Test
    fun `should reject invalid metric value`() = testApplication {
        // Given - invalid success rate (> 1.0)
        val invalidMetric = MetricDataPointRequest(
            flagId = 1,
            flagKey = "test_metrics_flag",
            metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
            metricValue = 1.5, // Invalid: should be 0-1
            timestamp = System.currentTimeMillis()
        )
        
        // When
        val response = client.post("/api/v1/metrics") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(MetricDataPointRequest.serializer(), invalidMetric))
        }
        
        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
    
    @Test
    fun `should retrieve metrics for flag`() = testApplication {
        // Given - first submit some metrics
        val now = System.currentTimeMillis()
        val metricsRepository = MetricsRepository()
        
        kotlinx.coroutines.runBlocking {
            metricsRepository.save(
                MetricDataPoint(
                    flagId = 1,
                    flagKey = "test_metrics_flag",
                    segmentId = null,
                    variantId = null,
                    variantKey = null,
                    metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                    metricValue = 0.97,
                    timestamp = now,
                    entityId = null
                )
            )
        }
        
        // When
        val startTime = now - 3600_000 // 1 hour ago
        val endTime = now + 1000
        val response = client.get("/api/v1/metrics/1?start_time=$startTime&end_time=$endTime")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"flagId\":1"))
        assertTrue(responseBody.contains("\"metricType\":\"SUCCESS_RATE\""))
    }
    
    @Test
    fun `should get aggregated metrics`() = testApplication {
        // Given - submit multiple metrics
        val now = System.currentTimeMillis()
        val metricsRepository = MetricsRepository()
        
        kotlinx.coroutines.runBlocking {
            // Submit 10 metrics with varying success rates
            for (i in 1..10) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = 1,
                        flagKey = "test_metrics_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                        metricValue = 0.90 + (i * 0.01), // 0.91 to 1.00
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
        }
        
        // When
        val windowStart = now - 60_000 // 1 minute ago
        val windowEnd = now + 1000
        val response = client.get(
            "/api/v1/metrics/1/aggregation?metric_type=SUCCESS_RATE&window_start=$windowStart&window_end=$windowEnd"
        )
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"flagId\":1"))
        assertTrue(responseBody.contains("\"metricType\":\"SUCCESS_RATE\""))
        assertTrue(responseBody.contains("\"avgValue\""))
        assertTrue(responseBody.contains("\"count\":10"))
    }
    
    @Test
    fun `should count metrics for flag`() = testApplication {
        // Given - submit metrics
        val now = System.currentTimeMillis()
        val metricsRepository = MetricsRepository()
        
        kotlinx.coroutines.runBlocking {
            for (i in 1..5) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = 1,
                        flagKey = "test_metrics_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.ERROR_RATE,
                        metricValue = 0.01,
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
        }
        
        // When
        val startTime = now - 10_000
        val endTime = now + 1000
        val response = client.get(
            "/api/v1/metrics/1/count?start_time=$startTime&end_time=$endTime&metric_type=ERROR_RATE"
        )
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"count\":5"))
    }
    
    @Test
    fun `should filter metrics by variant`() = testApplication {
        // Given - submit metrics for different variants
        val now = System.currentTimeMillis()
        val metricsRepository = MetricsRepository()
        
        kotlinx.coroutines.runBlocking {
            // Variant 1 metrics
            metricsRepository.save(
                MetricDataPoint(
                    flagId = 1,
                    flagKey = "test_metrics_flag",
                    segmentId = null,
                    variantId = 1,
                    variantKey = "variant_a",
                    metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                    metricValue = 0.95,
                    timestamp = now,
                    entityId = null
                )
            )
            
            // Variant 2 metrics
            metricsRepository.save(
                MetricDataPoint(
                    flagId = 1,
                    flagKey = "test_metrics_flag",
                    segmentId = null,
                    variantId = 2,
                    variantKey = "variant_b",
                    metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                    metricValue = 0.90,
                    timestamp = now,
                    entityId = null
                )
            )
        }
        
        // When - get metrics for variant 1 only
        val startTime = now - 10_000
        val endTime = now + 1000
        val response = client.get(
            "/api/v1/metrics/1?start_time=$startTime&end_time=$endTime&variant_id=1"
        )
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"variantId\":1"))
        assertTrue(responseBody.contains("\"variantKey\":\"variant_a\""))
        assertTrue(!responseBody.contains("\"variantId\":2")) // Should not include variant 2
    }
}
