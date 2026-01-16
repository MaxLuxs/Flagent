package flagent.domain.value

/**
 * EvaluationContext value object - represents context for flag evaluation
 * Domain layer - no framework dependencies
 */
data class EvaluationContext(
    val entityID: EntityID,
    val entityType: String? = null,
    val entityContext: Map<String, Any>? = null
) {
    /**
     * Get property value from entity context
     */
    fun getProperty(property: String): Any? {
        return entityContext?.get(property)
    }
    
    /**
     * Check if property exists in entity context
     */
    fun hasProperty(property: String): Boolean {
        return entityContext?.containsKey(property) == true
    }
}
