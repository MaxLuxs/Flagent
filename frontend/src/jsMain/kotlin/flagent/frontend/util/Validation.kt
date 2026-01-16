package flagent.frontend.util

/**
 * Validation utilities for forms
 */
object Validation {
    fun validateFlagKey(key: String): ValidationResult {
        return when {
            key.isBlank() -> ValidationResult.Error("Flag key is required")
            key.length < 3 -> ValidationResult.Error("Flag key must be at least 3 characters")
            !key.matches(Regex("^[a-z0-9_]+$")) -> ValidationResult.Error("Flag key can only contain lowercase letters, numbers, and underscores")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDescription(description: String?): ValidationResult {
        return if (description != null && description.length > 1000) {
            ValidationResult.Error("Description must be less than 1000 characters")
        } else {
            ValidationResult.Success
        }
    }
    
    fun validateRolloutPercent(percent: Int): ValidationResult {
        return when {
            percent < 0 -> ValidationResult.Error("Rollout percent cannot be negative")
            percent > 100 -> ValidationResult.Error("Rollout percent cannot exceed 100%")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDistributionPercentages(percentages: Map<String, Int>): ValidationResult {
        val total = percentages.values.sum()
        return if (total != 100) {
            ValidationResult.Error("Distribution percentages must add up to 100% (currently at ${total}%)")
        } else {
            ValidationResult.Success
        }
    }
    
    fun validateConstraint(property: String, value: String): ValidationResult {
        return when {
            property.isBlank() -> ValidationResult.Error("Property is required")
            value.isBlank() -> ValidationResult.Error("Value is required")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Success
}
