package flagent.domain.value

/**
 * EntityID value object - represents an entity identifier for evaluation
 * Domain layer - no framework dependencies
 */
@JvmInline
value class EntityID(val value: String) {
    init {
        require(value.isNotBlank()) { "EntityID cannot be blank" }
    }
    
    override fun toString(): String = value
}
