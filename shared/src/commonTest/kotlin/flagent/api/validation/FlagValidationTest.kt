package flagent.api.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class FlagValidationTest {
    
    @Test
    fun testValidFlagKey() {
        val result = FlagValidation.validateFlagKey("my_feature_flag")
        assertTrue(result.isValid)
    }
    
    @Test
    fun testFlagKeyTooShort() {
        val result = FlagValidation.validateFlagKey("ab")
        assertFalse(result.isValid)
        assertEquals("Flag key must be at least 3 characters", result.errorMessage)
    }
    
    @Test
    fun testFlagKeyTooLong() {
        val result = FlagValidation.validateFlagKey("a".repeat(64))
        assertFalse(result.isValid)
        assertEquals("Flag key must not exceed 63 characters", result.errorMessage)
    }
    
    @Test
    fun testFlagKeyInvalidCharacters() {
        val result = FlagValidation.validateFlagKey("My Feature Flag")
        assertFalse(result.isValid)
    }
    
    @Test
    fun testValidRolloutPercent() {
        assertTrue(FlagValidation.validateRolloutPercent(0).isValid)
        assertTrue(FlagValidation.validateRolloutPercent(50).isValid)
        assertTrue(FlagValidation.validateRolloutPercent(100).isValid)
    }
    
    @Test
    fun testInvalidRolloutPercent() {
        assertFalse(FlagValidation.validateRolloutPercent(-1).isValid)
        assertFalse(FlagValidation.validateRolloutPercent(101).isValid)
    }
    
    @Test
    fun testValidDistributionPercentages() {
        val percentages = mapOf("control" to 50, "variant_a" to 30, "variant_b" to 20)
        val result = FlagValidation.validateDistributionPercentages(percentages)
        assertTrue(result.isValid)
    }
    
    @Test
    fun testInvalidDistributionPercentages() {
        val percentages = mapOf("control" to 50, "variant_a" to 30)
        val result = FlagValidation.validateDistributionPercentages(percentages)
        assertFalse(result.isValid)
        assertEquals("Distribution percentages must add up to 100% (currently at 80%)", result.errorMessage)
    }
    
    @Test
    fun testValidConstraintOperator() {
        val operators = listOf("EQ", "NEQ", "LT", "LTE", "GT", "GTE", "IN", "NOTIN")
        operators.forEach { op ->
            assertTrue(FlagValidation.validateConstraintOperator(op).isValid)
        }
    }
    
    @Test
    fun testInvalidConstraintOperator() {
        val result = FlagValidation.validateConstraintOperator("INVALID")
        assertFalse(result.isValid)
        assertEquals("Invalid constraint operator: INVALID", result.errorMessage)
    }
    
    @Test
    fun testValidDescription() {
        assertTrue(FlagValidation.validateDescription("Short description").isValid)
        assertTrue(FlagValidation.validateDescription(null).isValid)
    }
    
    @Test
    fun testDescriptionTooLong() {
        val longDesc = "a".repeat(1001)
        val result = FlagValidation.validateDescription(longDesc)
        assertFalse(result.isValid)
    }
}
