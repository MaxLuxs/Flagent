package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SegmentTest {
    @Test
    fun `prepareEvaluation creates SegmentEvaluation with distribution array`() {
        val distribution1 = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 50
        )
        val distribution2 = Distribution(
            id = 2,
            segmentId = 1,
            variantId = 2,
            percent = 50
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = listOf(distribution1, distribution2)
        )
        
        val evaluation = segment.prepareEvaluation()
        
        assertNotNull(evaluation)
        assertNotNull(evaluation.distributionArray)
        assertEquals(2, evaluation.distributionArray.variantIds.size)
        assertEquals(listOf(1, 2), evaluation.distributionArray.variantIds)
        assertEquals(listOf(500, 1000), evaluation.distributionArray.percentsAccumulated)
    }
    
    @Test
    fun `prepareEvaluation with empty distributions creates empty arrays`() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = emptyList()
        )
        
        val evaluation = segment.prepareEvaluation()
        
        assertNotNull(evaluation)
        assertNotNull(evaluation.distributionArray)
        assertTrue(evaluation.distributionArray.variantIds.isEmpty())
        assertTrue(evaluation.distributionArray.percentsAccumulated.isEmpty())
    }
    
    @Test
    fun `rollout returns variant ID when rollout matches`() {
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
            distributions = listOf(distribution)
        )
        
        val evaluation = segment.prepareEvaluation()
        val (variantID, log) = evaluation.distributionArray.rollout(
            entityID = "test_entity",
            salt = "1",
            rolloutPercent = 100
        )
        
        assertNotNull(variantID)
        assertEquals(1, variantID)
        assertTrue(log.contains("rollout yes"))
    }
    
    @Test
    fun `rollout returns null when rollout percent is 0`() {
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
            rolloutPercent = 0,
            distributions = listOf(distribution)
        )
        
        val evaluation = segment.prepareEvaluation()
        val (variantID, log) = evaluation.distributionArray.rollout(
            entityID = "test_entity",
            salt = "1",
            rolloutPercent = 0
        )
        
        assertEquals(null, variantID)
        assertTrue(log.contains("rollout no"))
    }
}
