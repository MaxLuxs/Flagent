package flagent.service

import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentDebugLogTest {
    @Test
    fun `create SegmentDebugLog with all fields`() {
        val log = SegmentDebugLog(
            segmentID = 1,
            message = "Segment matched successfully"
        )
        
        assertEquals(1, log.segmentID)
        assertEquals("Segment matched successfully", log.message)
    }
    
    @Test
    fun `SegmentDebugLog data class equality`() {
        val log1 = SegmentDebugLog(segmentID = 1, message = "Test")
        val log2 = SegmentDebugLog(segmentID = 1, message = "Test")
        
        assertEquals(log1, log2)
        assertEquals(log1.hashCode(), log2.hashCode())
    }
    
    @Test
    fun `SegmentDebugLog with different messages are not equal`() {
        val log1 = SegmentDebugLog(segmentID = 1, message = "Message 1")
        val log2 = SegmentDebugLog(segmentID = 1, message = "Message 2")
        
        assert(!log1.equals(log2)) { "Logs with different messages should not be equal" }
    }
}
