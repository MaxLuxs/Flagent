package flagent.frontend.viewmodel

import flagent.frontend.state.Tenant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class TenantViewModelTest {

    @Test
    fun testInitialState() {
        val vm = TenantViewModel()
        assertNull(vm.currentTenant)
        assertEquals(0, vm.tenants.size)
        assertFalse(vm.isLoading)
        assertNull(vm.error)
    }

    @Test
    fun testSwitchTenant() {
        val vm = TenantViewModel()
        val t = Tenant(id = "1", key = "acme", name = "Acme")
        vm.switchTenant(t)
        assertEquals(t, vm.currentTenant)
        assertEquals("1", vm.getTenantId())
    }

    @Test
    fun testGetTenantIdWhenNoTenant() {
        val vm = TenantViewModel()
        assertNull(vm.getTenantId())
    }

    @Test
    fun testClearError() {
        val vm = TenantViewModel()
        vm.clearError()
        assertNull(vm.error)
    }
}
