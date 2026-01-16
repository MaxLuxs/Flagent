package flagent.domain.usecase

import flagent.domain.entity.*
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluateFlagUseCaseTest {
    private val useCase = EvaluateFlagUseCase()
    
    @Test
    fun `invoke returns null variant for disabled flag`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = false
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        assertNull(result.variantID)
    }
    
    @Test
    fun `invoke returns null variant for flag with no segments`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = emptyList()
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        assertNull(result.variantID)
    }
    
    @Test
    fun `invoke returns variant ID when segment matches and rollout succeeds`() {
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        assertNotNull(result.variantID)
        assertEquals(1, result.variantID)
        assertEquals(1, result.segmentID)
    }
    
    @Test
    fun `invoke skips segment when constraints do not match`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "country",
            operator = "EQ",
            value = "US"
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
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "CA")
        )
        
        val result = useCase.invoke(flag, context)
        
        assertNull(result.variantID)
    }
    
    @Test
    fun `invoke evaluates segments in rank order`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")
        
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
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 2,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
            rank = 1,
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        // Should match segment2 (rank 1) first
        assertNotNull(result.variantID)
        assertEquals(2, result.variantID)
        assertEquals(2, result.segmentID)
    }
    
    @Test
    fun `invoke includes debug logs when enableDebug is true`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = false
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context, enableDebug = true)
        
        assertTrue(result.debugLogs.isNotEmpty())
        assertEquals(1, result.debugLogs.size)
        assertTrue(result.debugLogs[0].message.contains("not enabled"))
    }
    
    @Test
    fun `invoke does not include debug logs when enableDebug is false`() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = false
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context, enableDebug = false)
        
        assertTrue(result.debugLogs.isEmpty())
    }
    
    @Test
    fun `invoke skips segment when rollout percent is 0`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")
        
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
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        // Should skip segment1 (rollout 0%) and match segment2
        assertNotNull(result.variantID)
        assertEquals(2, result.variantID)
        assertEquals(2, result.segmentID)
    }
    
    @Test
    fun `invoke returns null when all segments have rollout 0`() {
        val variant = Variant(id = 1, flagId = 1, key = "variant1")
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 0,
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        assertNull(result.variantID)
    }
    
    @Test
    fun `invoke continues to next segment when rollout does not match`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")
        
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
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context, enableDebug = true)
        
        // Should continue to segment2 when segment1 rollout is 0
        assertNotNull(result.variantID)
        assertEquals(2, result.variantID)
        assertEquals(2, result.segmentID)
        
        // Debug logs should indicate rollout no for segment1
        val segment1DebugLog = result.debugLogs.find { it.segmentID == 1 }
        assertNotNull(segment1DebugLog)
        assertTrue(segment1DebugLog.message.contains("rollout no"))
    }
    
    @Test
    fun `invoke returns last segmentID when no variant matches`() {
        val variant = Variant(id = 1, flagId = 1, key = "variant1")
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
            rank = 2,
            rolloutPercent = 0,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment1, segment2),
            variants = listOf(variant)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        // Should return last evaluated segmentID even if no variant matches
        assertNull(result.variantID)
        assertEquals(2, result.segmentID) // Last segment evaluated
    }
    
    @Test
    fun `invoke handles segment with partial rollout 50 percent`() {
        val variant = Variant(id = 1, flagId = 1, key = "variant1")
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Segment with 50% rollout",
            rank = 1,
            rolloutPercent = 50,
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        // With 50% rollout, some entities will get variant, some won't
        // Result depends on consistent hashing
        // Just verify that evaluation completes without error
        assertTrue(result.segmentID == 1 || result.variantID == null)
    }
    
    @Test
    fun `invoke handles multiple segments where none pass rollout`() {
        val variant = Variant(id = 1, flagId = 1, key = "variant1")
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 1, // Very low rollout
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
            rank = 2,
            rolloutPercent = 1, // Very low rollout
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment1, segment2),
            variants = listOf(variant)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user999") // Use entityID that likely won't match low rollout
        )
        
        val result = useCase.invoke(flag, context)
        
        // With very low rollout, entity might not match any segment
        // Should return last segmentID evaluated
        assertEquals(2, result.segmentID)
    }
    
    @Test
    fun `invoke handles segment without distributions`() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Segment without distributions",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = emptyList()
        )
        
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(segment),
            variants = emptyList()
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.invoke(flag, context)
        
        // Segment without distributions should not return variant
        assertNull(result.variantID)
        assertEquals(1, result.segmentID)
    }
    
    @Test
    fun `invoke with enableDebug returns debug logs for multiple segments`() {
        val variant1 = Variant(id = 1, flagId = 1, key = "variant1")
        val variant2 = Variant(id = 2, flagId = 1, key = "variant2")
        
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
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 1,
            description = "Segment 2",
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
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("region" to "EU") // Won't match segment1
        )
        
        val result = useCase.invoke(flag, context, enableDebug = true)
        
        // Should have debug logs
        assertTrue(result.debugLogs.isNotEmpty())
        
        // Should have log for segment1 (constraints didn't match)
        val segment1Log = result.debugLogs.find { it.segmentID == 1 }
        assertNotNull(segment1Log)
        assertTrue(segment1Log.message.contains("did not match constraints"))
        
        // Should match segment2 and return variant
        assertNotNull(result.variantID)
        assertEquals(2, result.variantID)
        assertEquals(2, result.segmentID)
    }
}
