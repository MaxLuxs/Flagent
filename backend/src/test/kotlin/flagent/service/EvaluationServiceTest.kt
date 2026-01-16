package flagent.service

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import flagent.domain.entity.*
import flagent.recorder.DataRecordingService
import io.mockk.*
import kotlin.test.*
import kotlinx.serialization.json.*

class EvaluationServiceTest {
    private lateinit var evalCache: EvalCache
    private lateinit var dataRecordingService: DataRecordingService
    private lateinit var evaluationService: EvaluationService
    
    @BeforeTest
    fun setup() {
        evalCache = mockk()
        dataRecordingService = mockk(relaxed = true)
        evaluationService = EvaluationService(evalCache, dataRecordingService)
    }
    
    @Test
    fun testEvaluateFlag_ReturnsResult_WhenFlagFoundByID() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        assertEquals("test_flag", result.flagKey)
        assertNotNull(result.variantID)
        assertEquals(1, result.variantID)
        assertEquals(1, result.segmentID)
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_ReturnsResult_WhenFlagFoundByKey() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID("test_flag") } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = null,
            flagKey = "test_flag",
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        assertEquals("test_flag", result.flagKey)
        assertNotNull(result.variantID)
        verify { evalCache.getByFlagKeyOrID("test_flag") }
    }
    
    @Test
    fun testEvaluateFlag_ReturnsBlankResult_WhenFlagNotFound() {
        every { evalCache.getByFlagKeyOrID(999) } returns null
        
        val result = evaluationService.evaluateFlag(
            flagID = 999,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(999, result.flagID)
        assertNull(result.variantID)
        assertNull(result.segmentID)
        assertNotNull(result.evalDebugLog)
        assertTrue(result.evalDebugLog!!.message.contains("not found"))
        verify { evalCache.getByFlagKeyOrID(999) }
    }
    
    @Test
    fun testEvaluateFlag_ReturnsBlankResult_WhenFlagDisabled() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = false
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        assertNull(result.variantID)
        assertNotNull(result.evalDebugLog)
        assertTrue(result.evalDebugLog!!.message.contains("not enabled"))
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_ReturnsBlankResult_WhenFlagHasNoSegments() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = emptyList()
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        assertNull(result.variantID)
        assertNotNull(result.evalDebugLog)
        assertTrue(result.evalDebugLog!!.message.contains("no segments"))
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_GeneratesEntityID_WhenEntityIDNotProvided() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = null,
            entityType = null,
            entityContext = null,
            enableDebug = false
        )
        
        assertNotNull(result.evalContext.entityID)
        assertTrue(result.evalContext.entityID!!.startsWith("randomly_generated_"))
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_HandlesConstraints_WhenConstraintsMatch() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("region" to "US"),
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        assertNotNull(result.variantID)
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_HandlesConstraints_WhenConstraintsDoNotMatch() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("region" to "EU"),
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        // Should not match because constraint doesn't match
        assertNull(result.variantID)
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_HandlesConstraints_WhenEntityContextIsNull() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        // Should not match because entityContext is null but constraints are present
        assertNull(result.variantID)
        verify { evalCache.getByFlagKeyOrID(1) }
    }
    
    @Test
    fun testEvaluateFlag_EnablesDebug_WhenDebugEnabled() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        // Mock AppConfig.evalDebugEnabled
        mockkObject(AppConfig)
        every { AppConfig.evalDebugEnabled } returns true
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = true
        )
        
        assertNotNull(result.evalDebugLog)
        assertNotNull(result.evalDebugLog!!.segmentDebugLogs)
        assertTrue(result.evalDebugLog!!.segmentDebugLogs.isNotEmpty())
        verify { evalCache.getByFlagKeyOrID(1) }
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testEvaluateFlag_RecordsData_WhenRecordingEnabled() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        // Mock AppConfig for recording
        mockkObject(AppConfig)
        every { AppConfig.recorderEnabled } returns true
        every { AppConfig.evalLoggingEnabled } returns true
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        verify(exactly = 1) { dataRecordingService.recordAsync(result) }
        verify { evalCache.getByFlagKeyOrID(1) }
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testEvaluateFlagsByTags_ReturnsResults_WhenFlagsFound() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val tag = Tag(
            id = 1,
            value = "test_tag"
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant),
            tags = listOf(tag)
        )
        
        every { evalCache.getByTags(listOf("test_tag"), null) } returns listOf(flag)
        
        val results = evaluationService.evaluateFlagsByTags(
            tags = listOf("test_tag"),
            operator = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, results.size)
        assertEquals(1, results[0].flagID)
        assertNotNull(results[0].variantID)
        verify { evalCache.getByTags(listOf("test_tag"), null) }
    }
    
    @Test
    fun testEvaluateFlagsByTags_ReturnsEmptyList_WhenNoFlagsFound() {
        every { evalCache.getByTags(listOf("non_existent_tag"), null) } returns emptyList()
        
        val results = evaluationService.evaluateFlagsByTags(
            tags = listOf("non_existent_tag"),
            operator = null,
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertTrue(results.isEmpty())
        verify { evalCache.getByTags(listOf("non_existent_tag"), null) }
    }
    
    @Test
    fun testEvaluateFlagsByTags_UsesOperator_WhenOperatorProvided() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val tag = Tag(
            id = 1,
            value = "test_tag"
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant),
            tags = listOf(tag)
        )
        
        every { evalCache.getByTags(listOf("test_tag"), "ALL") } returns listOf(flag)
        
        val results = evaluationService.evaluateFlagsByTags(
            tags = listOf("test_tag"),
            operator = "ALL",
            entityID = "user123",
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, results.size)
        verify { evalCache.getByTags(listOf("test_tag"), "ALL") }
    }
    
    @Test
    fun testEvaluateFlag_HandlesMultipleSegments_WhenFirstSegmentDoesNotMatch() {
        val variant1 = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val variant2 = Variant(
            id = 2,
            flagId = 1,
            key = "variant2"
        )
        
        val distribution1 = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val distribution2 = Distribution(
            id = 2,
            segmentId = 2,
            variantId = 2,
            percent = 100
        )
        
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "First segment",
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Second segment",
            rank = 2,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution2)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment1, segment2),
            variants = listOf(variant1, variant2)
        )
        
        every { evalCache.getByFlagKeyOrID(1) } returns flag
        
        val result = evaluationService.evaluateFlag(
            flagID = 1,
            flagKey = null,
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("region" to "EU"), // Doesn't match first segment
            enableDebug = false
        )
        
        assertEquals(1, result.flagID)
        // Should match second segment
        assertNotNull(result.variantID)
        assertEquals(2, result.variantID)
        assertEquals(2, result.segmentID)
        verify { evalCache.getByFlagKeyOrID(1) }
    }
}
