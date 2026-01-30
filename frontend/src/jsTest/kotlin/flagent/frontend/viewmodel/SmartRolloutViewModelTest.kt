package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SmartRolloutViewModelTest {

    @Test
    fun testInitialState() {
        val vm = SmartRolloutViewModel(flagId = 1)
        assertEquals(0, vm.configs.size)
        assertEquals(0, vm.history.size)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
    }

    @Test
    fun testClearError() {
        val vm = SmartRolloutViewModel(flagId = 1)
        vm.clearError()
        assertNull(vm.error)
    }
}
