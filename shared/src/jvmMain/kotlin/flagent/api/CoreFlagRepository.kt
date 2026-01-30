package flagent.api

/**
 * Minimal flag repository interface for enterprise (anomaly kill switch).
 * Implemented by backend; enterprise uses it via CoreDependencies.
 */
interface CoreFlagRepository {
    suspend fun findById(id: Int): FlagInfo?
    suspend fun update(flag: FlagInfo)
}

data class FlagInfo(
    val id: Int,
    val key: String,
    val enabled: Boolean
)
