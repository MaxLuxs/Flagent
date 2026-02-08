package flagent.util

/**
 * Utilities for trunk-based development workflow.
 * Converts branch names to valid flag keys.
 */
object TrunkUtils {

    /**
     * Converts a branch name to a valid flag key.
     * - Strips `refs/heads/` prefix
     * - Replaces `/` with `_`
     * - Replaces invalid chars with `_`
     * - Lowercases
     *
     * Examples:
     * - `feature/new-payment` -> `feature_new-payment`
     * - `fix/FLAG-123` -> `fix_flag-123`
     */
    fun branchToFlagKey(branch: String): String {
        val normalized = branch.replace(Regex("^refs/heads/"), "")
        return normalized
            .replace("/", "_")
            .replace(Regex("[^a-zA-Z0-9_\\-.:]"), "_")
            .lowercase()
            .takeIf { it.isNotBlank() } ?: "unnamed"
    }
}
