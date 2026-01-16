package flagent.domain.value

/**
 * FlagKey value object - represents a flag key
 * Domain layer - no framework dependencies
 */
@JvmInline
value class FlagKey(val value: String) {
    init {
        require(value.isNotBlank()) { "FlagKey cannot be blank" }
    }
    
    override fun toString(): String = value
}
