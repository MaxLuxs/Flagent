package flagent.api.constants

/**
 * Operator for flag tags filter in batch evaluation.
 * ANY = match flags with any of the tags, ALL = match flags with all tags.
 */
enum class FlagTagsOperator(val value: String) {
    ANY("ANY"),
    ALL("ALL");

    companion object {
        fun fromString(s: String?): FlagTagsOperator? = entries.find { it.value == s }
        const val ANY_STR = "ANY"
        const val ALL_STR = "ALL"
    }
}
