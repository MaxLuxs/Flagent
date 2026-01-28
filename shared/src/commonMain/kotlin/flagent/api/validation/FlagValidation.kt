package flagent.api.validation

/**
 * Validation utilities for feature flags
 * Shared across backend and frontend
 */
object FlagValidation {
    // Constants
    const val MIN_KEY_LENGTH = 3
    const val MAX_KEY_LENGTH = 63
    const val KEY_REGEX_PATTERN = "^[a-z0-9_-]+\$"
    const val MAX_DESCRIPTION_LENGTH = 1000
    const val MIN_ROLLOUT_PERCENT = 0
    const val MAX_ROLLOUT_PERCENT = 100
    const val TOTAL_DISTRIBUTION_PERCENT = 100
    
    /**
     * Validate flag key
     */
    fun validateFlagKey(key: String): ValidationResult {
        return when {
            key.isBlank() -> ValidationResult.Error("Flag key is required")
            key.length < MIN_KEY_LENGTH -> ValidationResult.Error("Flag key must be at least $MIN_KEY_LENGTH characters")
            key.length > MAX_KEY_LENGTH -> ValidationResult.Error("Flag key must not exceed $MAX_KEY_LENGTH characters")
            !key.matches(Regex(KEY_REGEX_PATTERN)) -> ValidationResult.Error("Flag key can only contain lowercase letters, numbers, hyphens, and underscores")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate variant key
     */
    fun validateVariantKey(key: String): ValidationResult {
        return when {
            key.isBlank() -> ValidationResult.Error("Variant key is required")
            key.length < MIN_KEY_LENGTH -> ValidationResult.Error("Variant key must be at least $MIN_KEY_LENGTH characters")
            key.length > MAX_KEY_LENGTH -> ValidationResult.Error("Variant key must not exceed $MAX_KEY_LENGTH characters")
            !key.matches(Regex(KEY_REGEX_PATTERN)) -> ValidationResult.Error("Variant key can only contain lowercase letters, numbers, hyphens, and underscores")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate description
     */
    fun validateDescription(description: String?): ValidationResult {
        return if (description != null && description.length > MAX_DESCRIPTION_LENGTH) {
            ValidationResult.Error("Description must be less than $MAX_DESCRIPTION_LENGTH characters")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * Validate rollout percent
     */
    fun validateRolloutPercent(percent: Int): ValidationResult {
        return when {
            percent < MIN_ROLLOUT_PERCENT -> ValidationResult.Error("Rollout percent cannot be negative")
            percent > MAX_ROLLOUT_PERCENT -> ValidationResult.Error("Rollout percent cannot exceed $MAX_ROLLOUT_PERCENT%")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate distribution percentages
     */
    fun validateDistributionPercentages(percentages: Map<String, Int>): ValidationResult {
        val total = percentages.values.sum()
        return if (total != TOTAL_DISTRIBUTION_PERCENT) {
            ValidationResult.Error("Distribution percentages must add up to $TOTAL_DISTRIBUTION_PERCENT% (currently at ${total}%)")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * Validate distribution percentages (list version)
     */
    fun validateDistributionPercentages(percentages: List<Int>): ValidationResult {
        val total = percentages.sum()
        return if (total != TOTAL_DISTRIBUTION_PERCENT) {
            ValidationResult.Error("Distribution percentages must add up to $TOTAL_DISTRIBUTION_PERCENT% (currently at ${total}%)")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * Validate constraint
     */
    fun validateConstraint(property: String, value: String): ValidationResult {
        return when {
            property.isBlank() -> ValidationResult.Error("Property is required")
            value.isBlank() -> ValidationResult.Error("Value is required")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate constraint operator
     */
    fun validateConstraintOperator(operator: String): ValidationResult {
        val validOperators = setOf(
            "EQ", "NEQ", "LT", "LTE", "GT", "GTE",
            "EREG", "NEREG", "IN", "NOTIN",
            "CONTAINS", "NOTCONTAINS"
        )
        return if (operator in validOperators) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Invalid constraint operator: $operator")
        }
    }
    
    /**
     * Validate segment rank
     */
    fun validateSegmentRank(rank: Int): ValidationResult {
        return when {
            rank < 0 -> ValidationResult.Error("Segment rank cannot be negative")
            rank > 9999 -> ValidationResult.Error("Segment rank must not exceed 9999")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Validation result
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Success
    
    val errorMessage: String?
        get() = (this as? Error)?.message
}
