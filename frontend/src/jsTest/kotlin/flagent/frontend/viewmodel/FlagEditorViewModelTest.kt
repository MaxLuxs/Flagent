package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class FlagEditorViewModelTest {

    @Test
    fun testInitialStateWithFlagId() {
        val vm = FlagEditorViewModel(flagId = 1)
        assertNull(vm.flag)
        assertEquals(0, vm.segments.size)
        assertEquals(0, vm.variants.size)
        assertFalse(vm.isLoading)
        assertFalse(vm.isSaving)
        assertNull(vm.error)
        assertEquals("config", vm.currentTab)
    }

    @Test
    fun testInitialStateCreateMode() {
        val vm = FlagEditorViewModel(flagId = null)
        assertNull(vm.flag)
        assertEquals(0, vm.segments.size)
        assertEquals(0, vm.variants.size)
        assertEquals("config", vm.currentTab)
    }

    @Test
    fun testCurrentTabSetter() {
        val vm = FlagEditorViewModel(flagId = 1)
        vm.currentTab = "segments"
        assertEquals("segments", vm.currentTab)
        vm.currentTab = "variants"
        assertEquals("variants", vm.currentTab)
    }

    @Test
    fun testClearError() {
        val vm = FlagEditorViewModel(flagId = 1)
        vm.clearError()
        assertNull(vm.error)
    }
}
