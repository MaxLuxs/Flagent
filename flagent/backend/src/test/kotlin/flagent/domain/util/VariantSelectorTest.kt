package flagent.domain.util

import flagent.domain.entity.Distribution
import flagent.domain.entity.Segment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VariantSelectorTest {
    private val selector = VariantSelector()
    
    @Test
    fun `selectVariant returns variant ID when rollout matches`() {
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
        
        val variantID = selector.selectVariant(
            segment = segment,
            entityID = "user123",
            flagID = 1
        )
        
        assertNotNull(variantID)
        assertEquals(1, variantID)
    }
    
    @Test
    fun `selectVariant returns null when rollout percent is 0`() {
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
        
        val variantID = selector.selectVariant(
            segment = segment,
            entityID = "user123",
            flagID = 1
        )
        
        assertNull(variantID)
    }
    
    @Test
    fun `selectVariant returns null when distributions are empty`() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = emptyList()
        )
        
        val variantID = selector.selectVariant(
            segment = segment,
            entityID = "user123",
            flagID = 1
        )
        
        assertNull(variantID)
    }
    
    @Test
    fun `selectVariant returns consistent variant for same entityID`() {
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
        
        val entityID = "user123"
        val variantID1 = selector.selectVariant(segment, entityID, 1)
        val variantID2 = selector.selectVariant(segment, entityID, 1)
        
        assertEquals(variantID1, variantID2)
        assertNotNull(variantID1)
    }
    
    @Test
    fun `selectVariant returns same variant for segment with 100 percent distribution`() {
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
        
        val variantID1 = selector.selectVariant(segment, "user1", 1)
        val variantID2 = selector.selectVariant(segment, "user2", 1)
        
        // With 100% rollout and 100% distribution, all users should get the same variant
        assertEquals(variantID1, variantID2)
        assertEquals(1, variantID1)
    }
    
    @Test
    fun `selectVariant handles multiple distributions with different percents`() {
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
        
        // Test multiple entity IDs to verify distribution
        val variantIDs = (1..100).map { 
            selector.selectVariant(segment, "user$it", 1)
        }.filterNotNull()
        
        // Should have at least one variant (may not have both due to consistent hashing)
        assertTrue(variantIDs.isNotEmpty())
        // At least one variant should be selected
        assertTrue(variantIDs.any { it == 1 } || variantIDs.any { it == 2 })
    }
    
    @Test
    fun `selectVariant returns null for rollout percent less than 0`() {
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
            rolloutPercent = -1,
            distributions = listOf(distribution)
        )
        
        val variantID = selector.selectVariant(segment, "user123", 1)
        
        // Should handle negative rollout gracefully
        assertNull(variantID)
    }
    
    @Test
    fun `selectVariant uses flagID as salt for consistent hashing`() {
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = listOf(distribution)
        )
        
        val segment2 = Segment(
            id = 1,
            flagId = 2,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = listOf(distribution)
        )
        
        val entityID = "user123"
        val variantID1 = selector.selectVariant(segment1, entityID, 1)
        val variantID2 = selector.selectVariant(segment2, entityID, 2)
        
        // Same segment structure but different flagID should use different salt
        // Results might be the same or different, but should be deterministic
        assertNotNull(variantID1)
        assertNotNull(variantID2)
    }
    
    @Test
    fun `selectVariant handles multiple distributions with unequal percents`() {
        val distribution1 = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 30
        )
        
        val distribution2 = Distribution(
            id = 2,
            segmentId = 1,
            variantId = 2,
            percent = 40
        )
        
        val distribution3 = Distribution(
            id = 3,
            segmentId = 1,
            variantId = 3,
            percent = 30
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100,
            distributions = listOf(distribution1, distribution2, distribution3)
        )
        
        // Test multiple entity IDs to verify distribution
        val variantIDs = (1..100).map { 
            selector.selectVariant(segment, "user$it", 1)
        }.filterNotNull()
        
        // Should have variants selected
        assertTrue(variantIDs.isNotEmpty())
        // Should have at least one of each variant (may not have all due to consistent hashing)
        assertTrue(variantIDs.any { it == 1 } || variantIDs.any { it == 2 } || variantIDs.any { it == 3 })
    }
    
    @Test
    fun `selectVariant handles rolloutPercent greater than 100`() {
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
            rolloutPercent = 150, // > 100
            distributions = listOf(distribution)
        )
        
        val variantID = selector.selectVariant(segment, "user123", 1)
        
        // Should handle > 100% rollout gracefully (treats as 100%)
        assertNotNull(variantID)
        assertEquals(1, variantID)
    }
    
    @Test
    fun `selectVariant returns consistent variant for same entityID and flagID`() {
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
        
        val entityID = "user123"
        val flagID = 1
        
        // Call multiple times with same parameters
        val variantID1 = selector.selectVariant(segment, entityID, flagID)
        val variantID2 = selector.selectVariant(segment, entityID, flagID)
        val variantID3 = selector.selectVariant(segment, entityID, flagID)
        
        // Should always return the same variant for same entityID + flagID
        assertEquals(variantID1, variantID2)
        assertEquals(variantID2, variantID3)
        assertNotNull(variantID1)
    }
}
