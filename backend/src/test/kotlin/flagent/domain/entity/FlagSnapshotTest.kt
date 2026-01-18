package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FlagSnapshotTest {
    @Test
    fun `create FlagSnapshot with all fields`() {
        val snapshot = FlagSnapshot(
            id = 1,
            flagId = 10,
            updatedBy = "admin",
            flag = """{"key":"test_flag","enabled":true}""",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        assertEquals(1, snapshot.id)
        assertEquals(10, snapshot.flagId)
        assertEquals("admin", snapshot.updatedBy)
        assertEquals("""{"key":"test_flag","enabled":true}""", snapshot.flag)
        assertEquals("2024-01-01T00:00:00Z", snapshot.updatedAt)
    }
    
    @Test
    fun `create FlagSnapshot with default values`() {
        val snapshot = FlagSnapshot(
            flagId = 10,
            flag = """{"key":"test_flag"}"""
        )
        
        assertEquals(0, snapshot.id)
        assertNull(snapshot.updatedBy)
        assertNull(snapshot.updatedAt)
    }
    
    @Test
    fun `FlagSnapshot data class equality`() {
        val snapshot1 = FlagSnapshot(
            id = 1,
            flagId = 10,
            flag = """{"key":"test"}"""
        )
        val snapshot2 = FlagSnapshot(
            id = 1,
            flagId = 10,
            flag = """{"key":"test"}"""
        )
        
        assertEquals(snapshot1, snapshot2)
        assertEquals(snapshot1.hashCode(), snapshot2.hashCode())
    }
    
    @Test
    fun `FlagSnapshot with different flag JSON are not equal`() {
        val snapshot1 = FlagSnapshot(
            id = 1,
            flagId = 10,
            flag = """{"key":"test1"}"""
        )
        val snapshot2 = FlagSnapshot(
            id = 1,
            flagId = 10,
            flag = """{"key":"test2"}"""
        )
        
        assert(!snapshot1.equals(snapshot2)) { "Snapshots with different flag JSON should not be equal" }
    }
}
