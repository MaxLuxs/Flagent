package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SsoViewModelTest {

    @Test
    fun testInitialState() {
        val vm = SsoViewModel()
        assertEquals(0, vm.providers.size)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
        assertNull(vm.errorHint)
    }

    @Test
    fun testClearError() {
        val vm = SsoViewModel()
        vm.clearError()
        assertNull(vm.error)
        assertNull(vm.errorHint)
    }
}
