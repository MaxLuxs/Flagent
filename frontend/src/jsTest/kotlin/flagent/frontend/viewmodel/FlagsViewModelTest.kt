package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class FlagsViewModelTest {
    
    @Test
    fun testInitialState() {
        val viewModel = FlagsViewModel()
        
        assertEquals(0, viewModel.flags.size)
        assertEquals(0, viewModel.deletedFlags.size)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
        assertEquals("", viewModel.searchQuery)
        assertNull(viewModel.statusFilter)
        assertNull(viewModel.sortColumn)
    }
    
    @Test
    fun testSearchQueryFilter() {
        val viewModel = FlagsViewModel()
        viewModel.searchQuery = "test"
        
        assertEquals("test", viewModel.searchQuery)
    }
    
    @Test
    fun testStatusFilter() {
        val viewModel = FlagsViewModel()
        
        viewModel.statusFilter = true
        assertEquals(true, viewModel.statusFilter)
        
        viewModel.statusFilter = false
        assertEquals(false, viewModel.statusFilter)
        
        viewModel.statusFilter = null
        assertNull(viewModel.statusFilter)
    }
    
    @Test
    fun testSetSortColumn() {
        val viewModel = FlagsViewModel()
        
        viewModel.setSortColumn("id")
        assertEquals("id", viewModel.sortColumn)
        assertEquals(true, viewModel.sortAscending)
        
        viewModel.setSortColumn("id")
        assertEquals("id", viewModel.sortColumn)
        assertEquals(false, viewModel.sortAscending)
        
        viewModel.setSortColumn("description")
        assertEquals("description", viewModel.sortColumn)
        assertEquals(true, viewModel.sortAscending)
    }
    
    @Test
    fun testClearError() {
        val viewModel = FlagsViewModel()
        viewModel.error = "Test error"
        
        assertEquals("Test error", viewModel.error)
        
        viewModel.clearError()
        assertNull(viewModel.error)
    }
}
