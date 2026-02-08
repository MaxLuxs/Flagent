package flagent.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TrunkUtilsTest {

    @Test
    fun `branchToFlagKey strips refs heads prefix`() {
        assertEquals("feature_foo", TrunkUtils.branchToFlagKey("refs/heads/feature/foo"))
    }

    @Test
    fun `branchToFlagKey replaces slash with underscore`() {
        assertEquals("feature_new-payment", TrunkUtils.branchToFlagKey("feature/new-payment"))
    }

    @Test
    fun `branchToFlagKey lowercases`() {
        assertEquals("fix_flag-123", TrunkUtils.branchToFlagKey("fix/FLAG-123"))
    }

    @Test
    fun `branchToFlagKey returns unnamed for blank`() {
        assertEquals("unnamed", TrunkUtils.branchToFlagKey(""))
    }

    @Test
    fun `branchToFlagKey preserves valid chars`() {
        assertEquals("my_flag-key", TrunkUtils.branchToFlagKey("my/flag-key"))
    }

    @Test
    fun `branchToFlagKey replaces invalid chars with underscore`() {
        assertEquals("feature_foo_bar", TrunkUtils.branchToFlagKey("feature/foo!bar"))
    }
}
