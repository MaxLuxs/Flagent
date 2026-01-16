package flagent.frontend.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import flagent.api.model.FlagResponse
import kotlinx.browser.window

/**
 * AppState - application state management with caching and optimistic updates
 */
object AppState {
    val flags = mutableStateListOf<FlagResponse>()
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // Cache for flags by ID
    private val flagCache = mutableMapOf<Int, FlagResponse>()

    // Cache timestamp
    private var cacheTimestamp: Double = 0.0
    private const val CACHE_TTL_MS = 5 * 60 * 1000.0 // 5 minutes

    fun clearError() {
        error.value = null
    }

    fun getFlag(id: Int): FlagResponse? {
        return flagCache[id]
    }

    fun cacheFlag(flag: FlagResponse) {
        flagCache[flag.id] = flag
        cacheTimestamp = window.performance.now()
    }

    fun cacheFlags(flags: List<FlagResponse>) {
        flags.forEach { flag -> cacheFlag(flag) }
    }

    fun isCacheValid(): Boolean {
        return (window.performance.now() - cacheTimestamp) < CACHE_TTL_MS
    }

    fun clearCache() {
        flagCache.clear()
        cacheTimestamp = 0.0
    }

    /**
     * Optimistic update - update flag in cache before API call completes
     */
    fun optimisticUpdateFlag(id: Int, update: (FlagResponse) -> FlagResponse) {
        flagCache[id]?.let { cached ->
            flagCache[id] = update(cached)
        }
        val index = flags.indexOfFirst { flag -> flag.id == id }
        if (index >= 0) {
            flags[index] = update(flags[index])
        }
    }

    /**
     * Rollback optimistic update if API call fails
     */
    fun rollbackOptimisticUpdate(id: Int, original: FlagResponse) {
        flagCache[id] = original
        val index = flags.indexOfFirst { flag -> flag.id == id }
        if (index >= 0) {
            flags[index] = original
        }
    }
}
