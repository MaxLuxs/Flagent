package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BillingViewModelTest {

    @Test
    fun testInitialState() {
        val vm = BillingViewModel()
        assertNull(vm.subscription)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
        assertNull(vm.errorHint)
    }

    @Test
    fun testClearError() {
        val vm = BillingViewModel()
        vm.clearError()
        assertNull(vm.error)
        assertNull(vm.errorHint)
    }
}
