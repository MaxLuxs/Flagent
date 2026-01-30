package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RealtimeViewModelTest {

    @Test
    fun testInitialState() {
        val vm = RealtimeViewModel(onNotification = {})
        assertFalse(vm.isConnected)
        assertEquals(0, vm.recentEvents.size)
    }

    @Test
    fun testDisconnectWithoutConnectDoesNotThrow() {
        val vm = RealtimeViewModel(onNotification = {})
        vm.disconnect()
    }
}
