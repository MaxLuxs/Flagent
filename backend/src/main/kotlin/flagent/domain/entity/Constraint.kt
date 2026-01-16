package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Constraint entity - the unit of constraints
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class Constraint(
    val id: Int = 0,
    val segmentId: Int,
    val property: String,
    val operator: String,
    val value: String
) {
    companion object {
        /**
         * Operator to expression map
         * Maps from swagger model operator to condition operator
         */
        val operatorToExprMap = mapOf(
            "EQ" to "==",
            "NEQ" to "!=",
            "LT" to "<",
            "LTE" to "<=",
            "GT" to ">",
            "GTE" to ">=",
            "EREG" to "=~",
            "NEREG" to "!~",
            "IN" to "IN",
            "NOTIN" to "NOT IN",
            "CONTAINS" to "CONTAINS",
            "NOTCONTAINS" to "NOT CONTAINS"
        )
    }
    
    /**
     * Convert constraint to expression string
     */
    fun toExprStr(): String {
        if (property.isEmpty() || operator.isEmpty() || value.isEmpty()) {
            throw IllegalArgumentException("empty Property/Operator/Value: $property/$operator/$value")
        }
        
        val exprOperator = operatorToExprMap[operator]
            ?: throw IllegalArgumentException("not supported operator: $operator")
        
        return "({$property} $exprOperator $value)"
    }
    
    /**
     * Validate constraint
     * Checks that operator is supported and all fields are filled
     */
    fun validate() {
        if (property.isEmpty() || operator.isEmpty() || value.isEmpty()) {
            throw IllegalArgumentException("empty Property/Operator/Value: $property/$operator/$value")
        }
        
        if (!operatorToExprMap.containsKey(operator)) {
            throw IllegalArgumentException("not supported operator: $operator")
        }
    }
}
