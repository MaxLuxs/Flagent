package flagent.cache.impl

import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import kotlinx.coroutines.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Immutable snapshot for lock-free read path.
 * Readers get the current snapshot via volatile read; writers swap atomically.
 */
private data class CacheSnapshot(
    val idCache: Map<String, Flag>,
    val keyCache: Map<String, Flag>,
    val tagCache: Map<String, Map<Int, Flag>>
)

/**
 * In-memory evaluation cache with periodic refresh.
 * Uses lock-free read path: volatile snapshot swap on refresh, no lock on getByFlagKeyOrID/getByTags.
 */
class EvalCache(
    private val flagRepository: IFlagRepository? = null,
    private val fetcher: EvalCacheFetcher? = null
) {
    init {
        // Ensure either repository or fetcher is provided
        require(flagRepository != null || fetcher != null) {
            "Either flagRepository or fetcher must be provided"
        }
    }
    
    private val actualFetcher: EvalCacheFetcher = fetcher ?: createEvalCacheFetcher(flagRepository!!)
    private val cacheScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    @Volatile
    private var snapshot: CacheSnapshot = CacheSnapshot(emptyMap(), emptyMap(), emptyMap())
    
    /**
     * Start periodic cache refresh. Initial load and subsequent refreshes run asynchronously.
     * First requests may see empty cache until initial load completes (cold start).
     */
    fun start() {
        cacheScope.launch {
            try {
                withTimeout(AppConfig.evalCacheRefreshTimeout) {
                    reloadCache()
                }
            } catch (e: Exception) {
                val isClosed = generateSequence<Throwable>(e) { it.cause }.any { it.message?.contains("has been closed") == true }
                if (isClosed) {
                    logger.debug { "EvalCache initial load skipped: DB closed" }
                } else {
                    logger.error(e) { "Failed to reload evaluation cache on start" }
                }
            }
            while (isActive) {
                delay(AppConfig.evalCacheRefreshInterval)
                try {
                    withTimeout(AppConfig.evalCacheRefreshTimeout) {
                        reloadCache()
                    }
                } catch (e: Exception) {
                    val isClosed = generateSequence<Throwable>(e) { it.cause }.any { it.message?.contains("has been closed") == true }
                    if (isClosed) {
                        logger.debug { "EvalCache refresh skipped: DB closed" }
                    } else {
                        logger.error(e) { "Failed to reload evaluation cache" }
                    }
                }
            }
        }
    }
    
    /**
     * Get flag by ID or Key. Lock-free read from volatile snapshot.
     */
    fun getByFlagKeyOrID(keyOrID: Any): Flag? {
        val key = keyOrID.toString()
        val s = snapshot
        return s.idCache[key] ?: s.keyCache[key]
    }
    
    /**
     * Get flags by tags. Lock-free read from volatile snapshot.
     */
    fun getByTags(tags: List<String>, operator: String?): List<Flag> {
        val s = snapshot
        val results = when (operator) {
            "ALL" -> getByTagsALL(s.tagCache, tags)
            else -> getByTagsANY(s.tagCache, tags)
        }
        return results.values.toList()
    }
    
    private fun getByTagsANY(tagCache: Map<String, Map<Int, Flag>>, tags: List<String>): Map<Int, Flag> {
        val results = mutableMapOf<Int, Flag>()
        tags.forEach { tag ->
            tagCache[tag]?.forEach { (flagId, flag) ->
                results[flagId] = flag
            }
        }
        return results
    }
    
    private fun getByTagsALL(tagCache: Map<String, Map<Int, Flag>>, tags: List<String>): Map<Int, Flag> {
        val results = mutableMapOf<Int, Flag>()
        tags.forEachIndexed { index, tag ->
            val flagSet = tagCache[tag] ?: return emptyMap()
            if (index == 0) {
                results.putAll(flagSet)
            } else {
                results.keys.removeAll { flagId -> flagId !in flagSet.keys }
                if (results.isEmpty()) return emptyMap()
            }
        }
        return results
    }
    
    /**
     * Refresh cache from database (public method for testing and manual refresh)
     */
    suspend fun refresh() {
        reloadCache()
    }
    
    /**
     * Reload cache from database. Builds new snapshot and swaps atomically (volatile write).
     */
    private suspend fun reloadCache() {
        try {
            val flags = actualFetcher.fetch()
            val idCacheNew = mutableMapOf<String, Flag>()
            val keyCacheNew = mutableMapOf<String, Flag>()
            val tagCacheNew = mutableMapOf<String, MutableMap<Int, Flag>>()
            
            flags.forEach { flag ->
                if (!flag.enabled) return@forEach
                idCacheNew[flag.id.toString()] = flag
                if (flag.key.isNotEmpty()) {
                    keyCacheNew[flag.key] = flag
                }
                flag.tags.forEach { tag ->
                    tagCacheNew.getOrPut(tag.value) { mutableMapOf() }[flag.id] = flag
                }
            }
            
            // Atomic swap: readers see either old or new snapshot, never partial state
            snapshot = CacheSnapshot(
                idCache = idCacheNew,
                keyCache = keyCacheNew,
                tagCache = tagCacheNew.mapValues { (_, m) -> m.toMap() }
            )
            
            logger.debug { "EvalCache reloaded: ${flags.size} flags" }
        } catch (e: Exception) {
            val isClosed = generateSequence<Throwable>(e) { it.cause }.any { it.message?.contains("has been closed") == true }
            if (!isClosed) {
                logger.error(e) { "Error reloading EvalCache" }
            }
            throw e
        }
    }
    
    /**
     * Stop cache refresh
     */
    fun stop() {
        cacheScope.cancel()
    }
    
    /**
     * Export cache to JSON format. Lock-free read from current snapshot.
     */
    fun export(): EvalCacheJSON {
        val s = snapshot
        val flags = s.idCache.values.toList()
        return EvalCacheJSON(flags = flags.map { it.toEvalCacheExport() })
    }
}

/**
 * EvalCacheJSON - JSON serialization format of EvalCache's flags
 * Uses serializable DTOs (domain entities have no @Serializable)
 */
@kotlinx.serialization.Serializable
data class EvalCacheJSON(
    val flags: List<EvalCacheFlagExport>
)
