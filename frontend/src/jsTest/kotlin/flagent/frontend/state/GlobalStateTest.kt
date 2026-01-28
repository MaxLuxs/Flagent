package flagent.frontend.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlobalStateTest {
    
    @Test
    fun testInitialState() {
        val state = GlobalState()
        assertEquals(0, state.flagsCache.size)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
        assertFalse(state.isAuthenticated)
    }
    
    @Test
    fun testAddNotification() {
        val state = GlobalState()
        val notification = Notification(
            message = "Test notification",
            type = NotificationType.INFO
        )
        
        state.addNotification(notification)
        assertEquals(1, state.notifications.size)
        assertEquals("Test notification", state.notifications[0].message)
    }
    
    @Test
    fun testRemoveNotification() {
        val state = GlobalState()
        val notification = Notification(
            id = "test-id",
            message = "Test notification"
        )
        
        state.addNotification(notification)
        assertEquals(1, state.notifications.size)
        
        state.removeNotification("test-id")
        assertEquals(0, state.notifications.size)
    }
    
    @Test
    fun testClearError() {
        val state = GlobalState()
        state.error = "Test error"
        assertEquals("Test error", state.error)
        
        state.clearError()
        assertEquals(null, state.error)
    }
}
