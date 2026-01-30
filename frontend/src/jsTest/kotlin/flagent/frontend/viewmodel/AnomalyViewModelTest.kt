package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AnomalyViewModelTest {

    @Test
    fun testInitialStateWithFlagId() {
        val vm = AnomalyViewModel(flagId = 1)
        assertNull(vm.config)
        assertEquals(0, vm.alerts.size)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
    }

    @Test
    fun testInitialStateWithoutFlagId() {
        val vm = AnomalyViewModel(flagId = null)
        assertNull(vm.config)
        assertEquals(0, vm.alerts.size)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
    }

    @Test
    fun testClearError() {
        val vm = AnomalyViewModel(flagId = 1)
        vm.clearError()
        assertNull(vm.error)
    }
}
