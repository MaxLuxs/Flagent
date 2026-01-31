package flagent.api.constants

/**
 * Constraint operators - single source of truth for validation and UI.
 * Used by FlagValidation, ConstraintEvaluator, and frontend components.
 */
object ConstraintOperators {
    val VALID_OPERATORS: Set<String> = setOf(
        "EQ", "NEQ", "LT", "LTE", "GT", "GTE",
        "EREG", "NEREG", "IN", "NOTIN",
        "CONTAINS", "NOTCONTAINS"
    )

    /**
     * Operator to display string for UI (e.g. dropdown labels).
     */
    val OPERATOR_DISPLAY_PAIRS: List<Pair<String, String>> = listOf(
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

    fun isValid(operator: String): Boolean = operator in VALID_OPERATORS
}
