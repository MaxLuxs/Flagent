package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertEquals
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
    
    @Test
    fun testValidateFlagKeyEdgeCases() {
        // Test exact minimum length (3 chars)
        assertTrue(Validation.validateFlagKey("abc").isValid)
        
        // Test with numbers
        assertTrue(Validation.validateFlagKey("test123").isValid)
        
        // Test with underscores
        assertTrue(Validation.validateFlagKey("test_flag_key").isValid)
        
        // Test uppercase should fail
        assertFalse(Validation.validateFlagKey("TestFlag").isValid)
        
        // Test with hyphen should fail
        assertFalse(Validation.validateFlagKey("test-flag").isValid)
        
        // Test with spaces should fail
        assertFalse(Validation.validateFlagKey("test flag").isValid)
    }
    
    @Test
    fun testValidateDescriptionEdgeCases() {
        // Test exact maximum length (1000 chars)
        assertTrue(Validation.validateDescription("a".repeat(1000)).isValid)
        
        // Test empty string
        assertTrue(Validation.validateDescription("").isValid)
        
        // Test one character over limit
        assertFalse(Validation.validateDescription("a".repeat(1001)).isValid)
    }
    
    @Test
    fun testValidateDistributionPercentagesEdgeCases() {
        // Test empty map
        assertFalse(Validation.validateDistributionPercentages(emptyMap()).isValid)
        
        // Test three variants
        assertTrue(Validation.validateDistributionPercentages(mapOf("A" to 33, "B" to 33, "C" to 34)).isValid)
        
        // Test single variant at 100%
        assertTrue(Validation.validateDistributionPercentages(mapOf("A" to 100)).isValid)
        
        // Test percentages less than 100
        assertFalse(Validation.validateDistributionPercentages(mapOf("A" to 30, "B" to 40)).isValid)
        
        // Test percentages more than 100
        assertFalse(Validation.validateDistributionPercentages(mapOf("A" to 60, "B" to 50)).isValid)
    }
    
    @Test
    fun testValidationResultProperties() {
        val success = ValidationResult.Success
        val error = ValidationResult.Error("Test error")
        
        assertTrue(success.isValid)
        assertFalse(error.isValid)
        
        // Verify error message
        assertEquals("Test error", error.message)
    }
}
