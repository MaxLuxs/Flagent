package flagent.service

import kotlin.test.Test
import kotlin.test.assertEquals

class EvalDebugLogTest {
    @Test
    fun `create EvalDebugLog with all fields`() {
        val segmentLogs = listOf(
            SegmentDebugLog(segmentID = 1, message = "Segment matched"),
            SegmentDebugLog(segmentID = 2, message = "Segment not matched")
        )
        
        val debugLog = EvalDebugLog(
            message = "Evaluation completed",
            segmentDebugLogs = segmentLogs
        )
        
        assertEquals("Evaluation completed", debugLog.message)
        assertEquals(2, debugLog.segmentDebugLogs.size)
        assertEquals(1, debugLog.segmentDebugLogs[0].segmentID)
        assertEquals("Segment matched", debugLog.segmentDebugLogs[0].message)
    }
    
    @Test
    fun `create EvalDebugLog with default values`() {
        val debugLog = EvalDebugLog()
        
        assertEquals("", debugLog.message)
        assertEquals(0, debugLog.segmentDebugLogs.size)
    }
    
    @Test
    fun `EvalDebugLog data class equality`() {
        val log1 = EvalDebugLog(
            message = "Test",
            segmentDebugLogs = listOf(SegmentDebugLog(1, "Message"))
        )
        val log2 = EvalDebugLog(
            message = "Test",
            segmentDebugLogs = listOf(SegmentDebugLog(1, "Message"))
        )
        
        assertEquals(log1, log2)
        assertEquals(log1.hashCode(), log2.hashCode())
    }
}
