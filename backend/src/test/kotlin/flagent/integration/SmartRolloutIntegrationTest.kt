package flagent.integration

import flagent.domain.entity.MetricDataPoint
import flagent.domain.entity.SmartRolloutConfig
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.route.SmartRolloutConfigRequest
import flagent.service.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for Smart Rollout functionality
 * 
 * Tests complete rollout flow including:
 * 1. Create rollout config
 * 2. Submit metrics
 * 3. Execute rollout (increment/pause/rollback)
 * 4. Check history
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartRolloutIntegrationTest {
    
    private var testFlagId: Int = 0
    private var testSegmentId: Int = 0
    
    @BeforeAll
    fun setup() {
        // Initialize test database
        Database.init()
        
        // Create test flag and segment
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagService = FlagService(flagRepository)
        val segmentService = SegmentService(segmentRepository)
        
        testApplication {
            kotlinx.coroutines.runBlocking {
                val flag = flagService.createFlag(
                    flagent.domain.entity.Flag(
                        key = "test_rollout_flag",
                        description = "Test flag for smart rollout",
                        enabled = true
                    )
                )
                testFlagId = flag.id
                
                val segment = segmentService.createSegment(
                    flagent.domain.entity.Segment(
                        flagId = testFlagId,
                        description = "Test segment",
                        rank = 1,
                        rolloutPercent = 10 // Start with 10%
                    )
                )
                testSegmentId = segment.id
            }
        }
    }
    
    @AfterAll
    fun teardown() {
        Database.close()
    }
    
    @Test
    fun `should create smart rollout config`() = testApplication {
        // Given
        val configRequest = SmartRolloutConfigRequest(
            flagId = testFlagId,
            segmentId = testSegmentId,
            enabled = true,
            targetRolloutPercent = 100,
            currentRolloutPercent = 10,
            incrementPercent = 10,
            incrementIntervalMs = 3600_000,
            successRateThreshold = 0.95,
            errorRateThreshold = 0.05,
            minSampleSize = 100,
            autoRollback = true,
            rollbackOnAnomaly = true
        )
        
        // When
        val response = client.post("/api/v1/smart-rollout") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(SmartRolloutConfigRequest.serializer(), configRequest))
        }
        
        // Then
        assertEquals(HttpStatusCode.Created, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"flagId\":$testFlagId"))
        assertTrue(responseBody.contains("\"targetRolloutPercent\":100"))
    }
    
    @Test
    fun `should execute rollout with good metrics and increment`() = testApplication {
        // Given - create rollout config
        val smartRolloutRepository = SmartRolloutRepository()
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    enabled = true,
                    targetRolloutPercent = 100,
                    currentRolloutPercent = 10,
                    incrementPercent = 10,
                    successRateThreshold = 0.95,
                    errorRateThreshold = 0.05,
                    minSampleSize = 50,
                    lastIncrementAt = System.currentTimeMillis() - 7200_000 // 2 hours ago
                )
            )
        }
        
        // Submit good metrics
        val metricsRepository = MetricsRepository()
        val now = System.currentTimeMillis()
        
        kotlinx.coroutines.runBlocking {
            // Submit 100 good metrics
            for (i in 1..100) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                        metricValue = 0.98, // High success rate
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
                
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.ERROR_RATE,
                        metricValue = 0.01, // Low error rate
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
        }
        
        // When - execute rollout
        val response = client.post("/api/v1/smart-rollout/${config.id}/execute")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"success\":true"))
        assertTrue(responseBody.contains("\"action\":\"INCREMENT\""))
        assertTrue(responseBody.contains("\"newRolloutPercent\":20")) // 10 + 10
    }
    
    @Test
    fun `should pause rollout when insufficient metrics`() = testApplication {
        // Given - create rollout config
        val smartRolloutRepository = SmartRolloutRepository()
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    enabled = true,
                    currentRolloutPercent = 20,
                    minSampleSize = 200, // High requirement
                    lastIncrementAt = System.currentTimeMillis() - 7200_000
                )
            )
        }
        
        // Submit only 50 metrics (less than minSampleSize)
        val metricsRepository = MetricsRepository()
        val now = System.currentTimeMillis()
        
        kotlinx.coroutines.runBlocking {
            for (i in 1..50) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                        metricValue = 0.98,
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
        }
        
        // When - execute rollout
        val response = client.post("/api/v1/smart-rollout/${config.id}/execute")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"success\":true"))
        assertTrue(responseBody.contains("\"action\":\"PAUSE\""))
        assertTrue(responseBody.contains("insufficient samples"))
    }
    
    @Test
    fun `should rollback when metrics are bad`() = testApplication {
        // Given - create rollout config
        val smartRolloutRepository = SmartRolloutRepository()
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    enabled = true,
                    currentRolloutPercent = 50,
                    incrementPercent = 10,
                    successRateThreshold = 0.95,
                    errorRateThreshold = 0.05,
                    minSampleSize = 50,
                    autoRollback = true,
                    lastIncrementAt = System.currentTimeMillis() - 7200_000
                )
            )
        }
        
        // Submit bad metrics
        val metricsRepository = MetricsRepository()
        val now = System.currentTimeMillis()
        
        kotlinx.coroutines.runBlocking {
            for (i in 1..100) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                        metricValue = 0.80, // Low success rate (below 95%)
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
                
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.ERROR_RATE,
                        metricValue = 0.15, // High error rate (above 5%)
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
        }
        
        // When - execute rollout
        val response = client.post("/api/v1/smart-rollout/${config.id}/execute")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"success\":true"))
        assertTrue(responseBody.contains("\"action\":\"ROLLBACK\""))
        assertTrue(responseBody.contains("\"newRolloutPercent\":40")) // 50 - 10
    }
    
    @Test
    fun `should get rollout history`() = testApplication {
        // Given - create config and execute rollout
        val smartRolloutRepository = SmartRolloutRepository()
        val metricsRepository = MetricsRepository()
        val segmentRepository = SegmentRepository()
        val smartRolloutService = SmartRolloutService(
            smartRolloutRepository,
            metricsRepository,
            SegmentService(segmentRepository)
        )
        
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    currentRolloutPercent = 30,
                    lastIncrementAt = System.currentTimeMillis() - 7200_000
                )
            )
        }
        
        // Submit good metrics and execute
        val now = System.currentTimeMillis()
        kotlinx.coroutines.runBlocking {
            for (i in 1..100) {
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.SUCCESS_RATE,
                        metricValue = 0.97,
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
                
                metricsRepository.save(
                    MetricDataPoint(
                        flagId = testFlagId,
                        flagKey = "test_rollout_flag",
                        segmentId = null,
                        variantId = null,
                        variantKey = null,
                        metricType = MetricDataPoint.MetricType.ERROR_RATE,
                        metricValue = 0.02,
                        timestamp = now - (i * 1000),
                        entityId = null
                    )
                )
            }
            
            // Execute rollout to create history
            smartRolloutService.executeRollout(config.id)
        }
        
        // When - get history
        val response = client.get("/api/v1/smart-rollout/${config.id}/history")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"rolloutConfigId\":${config.id}"))
        assertTrue(responseBody.contains("\"previousPercent\""))
        assertTrue(responseBody.contains("\"newPercent\""))
    }
    
    @Test
    fun `should get configs for flag`() = testApplication {
        // Given - create multiple configs
        val smartRolloutRepository = SmartRolloutRepository()
        
        kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    currentRolloutPercent = 40
                )
            )
        }
        
        // When - get configs for flag
        val response = client.get("/api/v1/smart-rollout/flag/$testFlagId")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"flagId\":$testFlagId"))
    }
    
    @Test
    fun `should update rollout config`() = testApplication {
        // Given - create config
        val smartRolloutRepository = SmartRolloutRepository()
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId,
                    enabled = true,
                    incrementPercent = 10
                )
            )
        }
        
        // When - update config
        val updateRequest = SmartRolloutConfigRequest(
            flagId = testFlagId,
            segmentId = testSegmentId,
            incrementPercent = 20 // Change from 10 to 20
        )
        
        val response = client.put("/api/v1/smart-rollout/${config.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(SmartRolloutConfigRequest.serializer(), updateRequest))
        }
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("\"incrementPercent\":20"))
    }
    
    @Test
    fun `should delete rollout config`() = testApplication {
        // Given - create config
        val smartRolloutRepository = SmartRolloutRepository()
        val config = kotlinx.coroutines.runBlocking {
            smartRolloutRepository.saveConfig(
                SmartRolloutConfig(
                    flagId = testFlagId,
                    segmentId = testSegmentId
                )
            )
        }
        
        // When - delete config
        val response = client.delete("/api/v1/smart-rollout/${config.id}")
        
        // Then
        assertEquals(HttpStatusCode.NoContent, response.status)
        
        // Verify config is deleted
        val getResponse = client.get("/api/v1/smart-rollout/${config.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
