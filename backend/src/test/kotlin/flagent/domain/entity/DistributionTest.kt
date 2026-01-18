package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class DistributionTest {
    @Test
    fun `create Distribution with all fields`() {
        val distribution = Distribution(
            id = 1,
            segmentId = 10,
            variantId = 20,
            variantKey = "control",
            percent = 50
        )
        
        assertEquals(1, distribution.id)
        assertEquals(10, distribution.segmentId)
        assertEquals(20, distribution.variantId)
        assertEquals("control", distribution.variantKey)
        assertEquals(50, distribution.percent)
    }
    
    @Test
    fun `create Distribution with default values`() {
        val distribution = Distribution(
            segmentId = 10,
            variantId = 20
        )
        
        assertEquals(0, distribution.id)
        assertEquals(null, distribution.variantKey)
        assertEquals(0, distribution.percent)
    }
    
    @Test
    fun `Distribution constants are correct`() {
        assertEquals(1000u, Distribution.TOTAL_BUCKET_NUM)
        assertEquals(10u, Distribution.PERCENT_MULTIPLIER)
    }
    
    @Test
    fun `create Distribution with different percent values`() {
        val percents = listOf(0, 25, 50, 75, 100)
        
        percents.forEach { percent ->
            val distribution = Distribution(
                segmentId = 10,
                variantId = 20,
                percent = percent
            )
            assertEquals(percent, distribution.percent)
        }
    }
}
