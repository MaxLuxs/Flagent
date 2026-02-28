package flagent.performance

import flagent.cache.impl.EvalCache
import flagent.domain.entity.*
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.EvaluationService
import flagent.service.adapter.SharedFlagEvaluatorAdapter
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis
import kotlin.test.*
import org.junit.jupiter.api.Tag

/**
 * Performance tests for Flagent.
 * Excluded from default CI via excludeTags("performance"); run with
 * ./gradlew :backend:test --tests "*PerformanceTest*" when needed.
 */
@Tag("performance")
class PerformanceTest {
    
    @BeforeTest
    fun setup() {
        Database.init()
    }
    
    @AfterTest
    fun cleanup() {
        Database.close()
    }
    
    @Test
    fun testEvaluationPerformance_Single() = runBlocking {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
        val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
        
        // Create test flag with segments and constraints
        val flag = createTestFlag()
        
        // Warm up cache
        evalCache.refresh()
        
        val iterations = 1000
        val time = measureTimeMillis {
            repeat(iterations) {
                evaluationService.evaluateFlag(
                    flagID = flag.id,
                    flagKey = null,
                    entityID = "user$it",
                    entityType = null,
                    entityContext = mapOf("country" to "US", "age" to 25),
                    enableDebug = false
                )
            }
        }
        
        val avgTime = time.toDouble() / iterations
        println("Single evaluation: $iterations iterations in ${time}ms, avg: ${avgTime}ms per evaluation")
        
        val threshold = System.getenv("RUN_PERF_TESTS")?.let { 
            System.getenv("PERF_EVAL_THRESHOLD_MS")?.toDoubleOrNull() ?: 50.0 
        } ?: 10.0
        assertTrue(avgTime < threshold, "Average evaluation time should be < ${threshold}ms, got ${avgTime}ms")
    }
    
    @Test
    fun testEvaluationPerformance_Batch() = runBlocking {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
        val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
        
        // Create test flag
        val flag = createTestFlag()
        
        // Warm up cache
        evalCache.refresh()
        
        val batchSize = 100
        val batches = 10
        val totalEvaluations = batchSize * batches
        
        val time = measureTimeMillis {
            repeat(batches) { batch ->
                val entities = (0 until batchSize).map { i ->
                    mapOf(
                        "flagID" to flag.id,
                        "entityID" to "user${batch * batchSize + i}",
                        "entityContext" to mapOf("country" to "US", "age" to 25)
                    )
                }
                
                entities.forEach { entity ->
                    evaluationService.evaluateFlag(
                        flagID = entity["flagID"] as Int,
                        flagKey = null,
                        entityID = entity["entityID"] as String,
                        entityType = null,
                        entityContext = @Suppress("UNCHECKED_CAST") (entity["entityContext"] as Map<String, Any>),
                        enableDebug = false
                    )
                }
            }
        }
        
        val avgTime = time.toDouble() / totalEvaluations
        println("Batch evaluation: $totalEvaluations evaluations in ${time}ms, avg: ${avgTime}ms per evaluation")
        
        val threshold = System.getenv("RUN_PERF_TESTS")?.let { 
            System.getenv("PERF_EVAL_THRESHOLD_MS")?.toDoubleOrNull() ?: 50.0 
        } ?: 10.0
        assertTrue(avgTime < threshold, "Average batch evaluation time should be < ${threshold}ms, got ${avgTime}ms")
    }
    
    @Test
    fun testCachePerformance() = runBlocking {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        
        // Create multiple flags
        repeat(100) { i ->
            val flag = Flag(
                key = "flag_$i",
                description = "Test flag $i",
                enabled = true
            )
            flagRepository.create(flag)
        }
        
        // Measure cache refresh time
        val time = measureTimeMillis {
            evalCache.refresh()
        }
        
        println("Cache refresh: 100 flags in ${time}ms")
        
        // Should be reasonably fast (< 5 seconds for 100 flags)
        assertTrue(time < 5000, "Cache refresh should be < 5s for 100 flags, got ${time}ms")
        
        // Measure cache lookup time
        val lookupTime = measureTimeMillis {
            repeat(1000) { i ->
                evalCache.getByFlagKeyOrID("flag_${i % 100}")
            }
        }
        
        val avgLookupTime = lookupTime.toDouble() / 1000
        println("Cache lookup: 1000 lookups in ${lookupTime}ms, avg: ${avgLookupTime}ms per lookup")
        
        // Should be very fast (< 1ms per lookup)
        assertTrue(avgLookupTime < 1.0, "Average cache lookup time should be < 1ms, got ${avgLookupTime}ms")
    }
    
    @Test
    fun testDatabaseQueryPerformance() = runBlocking {
        val flagRepository = FlagRepository()
        
        // Create test data
        repeat(100) { i ->
            val flag = Flag(
                key = "flag_$i",
                description = "Test flag $i",
                enabled = true
            )
            flagRepository.create(flag)
        }
        
        // Measure query time
        val time = measureTimeMillis {
            repeat(100) {
                flagRepository.findAll()
            }
        }
        
        val avgTime = time.toDouble() / 100
        println("Database query: 100 queries in ${time}ms, avg: ${avgTime}ms per query")
        
        // Should be reasonably fast (< 100ms per query)
        assertTrue(avgTime < 100.0, "Average query time should be < 100ms, got ${avgTime}ms")
    }
    
    @Test
    fun testConcurrentRequests() = runBlocking {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
        val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
        
        // Create test flag
        val flag = createTestFlag()
        evalCache.refresh()
        
        val concurrentRequests = 100
        val requestsPerCoroutine = 10
        
        val time = measureTimeMillis {
            coroutineScope {
                repeat(concurrentRequests) { i ->
                    launch {
                        repeat(requestsPerCoroutine) { j ->
                            evaluationService.evaluateFlag(
                                flagID = flag.id,
                                flagKey = null,
                                entityID = "user${i * requestsPerCoroutine + j}",
                                entityType = null,
                                entityContext = mapOf("country" to "US", "age" to 25),
                                enableDebug = false
                            )
                        }
                    }
                }
            }
        }
        
        val totalRequests = concurrentRequests * requestsPerCoroutine
        val avgTime = time.toDouble() / totalRequests
        val throughput = totalRequests.toDouble() / (time / 1000.0)
        
        println("Concurrent requests: $totalRequests requests in ${time}ms, avg: ${avgTime}ms per request, throughput: ${throughput} req/s")
        
        // Should handle concurrent requests well
        assertTrue(avgTime < 20.0, "Average concurrent request time should be < 20ms, got ${avgTime}ms")
        assertTrue(throughput > 100.0, "Throughput should be > 100 req/s, got ${throughput} req/s")
    }
    
    @Test
    fun testConstraintEvaluationPerformance() = runBlocking {
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val variantRepository = VariantRepository()
        val constraintRepository = ConstraintRepository()
        val distributionRepository = DistributionRepository()
        val evalCache = EvalCache(flagRepository)
        val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
        val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
        
        // Create flag first
        val flag = Flag(
            key = "test_flag",
            description = "Test",
            enabled = true
        )
        val createdFlag = flagRepository.create(flag)
        
        // Create variant
        val variant = Variant(flagId = createdFlag.id, key = "treatment")
        val createdVariant = variantRepository.create(variant)
        
        // Create segment
        val segment = Segment(
            flagId = createdFlag.id,
            description = "US premium users",
            rank = 0,
            rolloutPercent = 100
        )
        val createdSegment = segmentRepository.create(segment)
        
        // Create constraints
        constraintRepository.create(Constraint(segmentId = createdSegment.id, property = "country", operator = "EQ", value = "US"))
        constraintRepository.create(Constraint(segmentId = createdSegment.id, property = "age", operator = "GT", value = "18"))
        constraintRepository.create(Constraint(segmentId = createdSegment.id, property = "tier", operator = "IN", value = "premium,gold"))
        
        // Create distribution
        distributionRepository.updateDistributions(createdSegment.id, listOf(Distribution(segmentId = createdSegment.id, variantId = createdVariant.id, percent = 100)))
        
        evalCache.refresh()
        
        val iterations = 1000
        val time = measureTimeMillis {
            repeat(iterations) {
                evaluationService.evaluateFlag(
                    flagID = createdFlag.id,
                    flagKey = null,
                    entityID = "user$it",
                    entityType = null,
                    entityContext = mapOf(
                        "country" to "US",
                        "age" to 25,
                        "tier" to "premium"
                    ),
                    enableDebug = false
                )
            }
        }
        
        val avgTime = time.toDouble() / iterations
        println("Constraint evaluation: $iterations iterations in ${time}ms, avg: ${avgTime}ms per evaluation")
        
        // Should still be fast even with multiple constraints
        assertTrue(avgTime < 15.0, "Average constraint evaluation time should be < 15ms, got ${avgTime}ms")
    }
    
    private suspend fun createTestFlag(): Flag {
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val variantRepository = VariantRepository()
        val constraintRepository = ConstraintRepository()
        val distributionRepository = DistributionRepository()
        
        // Create flag first
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        val createdFlag = flagRepository.create(flag)
        
        // Create variant
        val variant = Variant(flagId = createdFlag.id, key = "control")
        val createdVariant = variantRepository.create(variant)
        
        // Create segment
        val segment = Segment(
            flagId = createdFlag.id,
            description = "Test segment",
            rank = 0,
            rolloutPercent = 100
        )
        val createdSegment = segmentRepository.create(segment)
        
        // Create constraint
        constraintRepository.create(Constraint(segmentId = createdSegment.id, property = "country", operator = "EQ", value = "US"))
        
        // Create distribution
        distributionRepository.updateDistributions(createdSegment.id, listOf(Distribution(segmentId = createdSegment.id, variantId = createdVariant.id, percent = 100)))
        
        // Return flag with all relations loaded
        return flagRepository.findById(createdFlag.id)!!
    }
}
