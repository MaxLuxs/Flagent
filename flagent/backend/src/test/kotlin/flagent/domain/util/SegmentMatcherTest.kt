package flagent.domain.util

import flagent.domain.entity.Constraint
import flagent.domain.entity.Segment
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SegmentMatcherTest {
    private val matcher = SegmentMatcher()
    
    @Test
    fun `matches returns true for segment with no constraints`() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            constraints = emptyList()
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        assertTrue(matcher.matches(segment, context))
    }
    
    @Test
    fun `matches returns true when all constraints match`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            constraints = listOf(constraint)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "US")
        )
        
        assertTrue(matcher.matches(segment, context))
    }
    
    @Test
    fun `matches returns false when constraint does not match`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            constraints = listOf(constraint)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "CA")
        )
        
        assertFalse(matcher.matches(segment, context))
    }
    
    @Test
    fun `matches returns false when entity context is null and constraints exist`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            constraints = listOf(constraint)
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        assertFalse(matcher.matches(segment, context))
    }
}
