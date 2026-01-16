package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {
    @Test
    fun testValidateFlagKey() {
        assertTrue(Validation.validateFlagKey("test_flag").isValid)
        assertTrue(Validation.validateFlagKey("test123").isValid)
        assertFalse(Validation.validateFlagKey("").isValid)
        assertFalse(Validation.validateFlagKey("ab").isValid)
        assertFalse(Validation.validateFlagKey("Test-Flag").isValid)
    }
    
    @Test
    fun testValidateDescription() {
        assertTrue(Validation.validateDescription(null).isValid)
        assertTrue(Validation.validateDescription("Short description").isValid)
        assertFalse(Validation.validateDescription("a".repeat(1001)).isValid)
    }
    
    @Test
    fun testValidateRolloutPercent() {
        assertTrue(Validation.validateRolloutPercent(50).isValid)
        assertTrue(Validation.validateRolloutPercent(0).isValid)
        assertTrue(Validation.validateRolloutPercent(100).isValid)
        assertFalse(Validation.validateRolloutPercent(-1).isValid)
        assertFalse(Validation.validateRolloutPercent(101).isValid)
    }
    
    @Test
    fun testValidateDistributionPercentages() {
        assertTrue(Validation.validateDistributionPercentages(mapOf("A" to 50, "B" to 50)).isValid)
        assertTrue(Validation.validateDistributionPercentages(mapOf("A" to 100)).isValid)
        assertFalse(Validation.validateDistributionPercentages(mapOf("A" to 50, "B" to 40)).isValid)
        assertFalse(Validation.validateDistributionPercentages(mapOf("A" to 50, "B" to 60)).isValid)
    }
    
    @Test
    fun testValidateConstraint() {
        assertTrue(Validation.validateConstraint("property", "value").isValid)
        assertFalse(Validation.validateConstraint("", "value").isValid)
        assertFalse(Validation.validateConstraint("property", "").isValid)
    }
}
