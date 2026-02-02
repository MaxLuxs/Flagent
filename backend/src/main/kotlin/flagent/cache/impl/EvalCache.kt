package flagent.cache.impl

import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

/**
 * EvalCache - in-memory cache for evaluation
 * Maps to pkg/handler/eval_cache.go from original project
 * 
 * Thread-safe cache with periodic refresh
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
    
    // Cache containers
    private val idCache = ConcurrentHashMap<String, Flag>()
    private val keyCache = ConcurrentHashMap<String, Flag>()
    private val tagCache = ConcurrentHashMap<String, MutableMap<Int, Flag>>()
    
    private val cacheMutex = java.util.concurrent.locks.ReentrantReadWriteLock()
    
    /**
     * Start periodic cache refresh
     */
    fun start() {
        // Initial load
        runBlocking {
            reloadCache()
        }
        
        // Periodic refresh
        cacheScope.launch {
            while (isActive) {
                delay(AppConfig.evalCacheRefreshInterval)
                try {
                    withTimeout(AppConfig.evalCacheRefreshTimeout) {
                        reloadCache()
                    }
                } catch (e: Exception) {
                    // During shutdown/tests DB may be closed; avoid noisy stack trace
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
     * Get flag by ID or Key
     */
    fun getByFlagKeyOrID(keyOrID: Any): Flag? {
        val key = keyOrID.toString()
        
        cacheMutex.readLock().lock()
        try {
            return idCache[key] ?: keyCache[key]
        } finally {
            cacheMutex.readLock().unlock()
        }
    }
    
    /**
     * Get flags by tags
     */
    fun getByTags(tags: List<String>, operator: String?): List<Flag> {
        cacheMutex.readLock().lock()
        try {
            val results = when (operator) {
                "ALL" -> getByTagsALL(tags)
                else -> getByTagsANY(tags)
            }
            return results.values.toList()
        } finally {
            cacheMutex.readLock().unlock()
        }
    }
    
    private fun getByTagsANY(tags: List<String>): Map<Int, Flag> {
        val results = mutableMapOf<Int, Flag>()
        
        tags.forEach { tag ->
            tagCache[tag]?.forEach { (flagId, flag) ->
                results[flagId] = flag
            }
        }
        
        return results
    }
    
    private fun getByTagsALL(tags: List<String>): Map<Int, Flag> {
        val results = mutableMapOf<Int, Flag>()
        
        tags.forEachIndexed { index, tag ->
            val flagSet = tagCache[tag] ?: return emptyMap()
            
            if (index == 0) {
                // Store all flags from first tag
                results.putAll(flagSet)
            } else {
                // Keep only flags that exist in all tags
                results.keys.removeAll { flagId -> flagId !in flagSet.keys }
                if (results.isEmpty()) {
                    return emptyMap()
                }
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
     * Reload cache from database
     */
    private suspend fun reloadCache() {
        val idCacheNew = ConcurrentHashMap<String, Flag>()
        val keyCacheNew = ConcurrentHashMap<String, Flag>()
        val tagCacheNew = ConcurrentHashMap<String, MutableMap<Int, Flag>>()
        
        try {
            val flags = actualFetcher.fetch()
            
            flags.forEach { flag ->
                if (!flag.enabled) return@forEach
                
                // Index by ID
                idCacheNew[flag.id.toString()] = flag
                
                // Index by Key
                if (flag.key.isNotEmpty()) {
                    keyCacheNew[flag.key] = flag
                }
                
                // Index by Tags
                flag.tags.forEach { tag ->
                    tagCacheNew.getOrPut(tag.value) { mutableMapOf() }[flag.id] = flag
                }
            }
            
            // Swap caches atomically
            cacheMutex.writeLock().lock()
            try {
                idCache.clear()
                keyCache.clear()
                tagCache.clear()
                
                idCache.putAll(idCacheNew)
                keyCache.putAll(keyCacheNew)
                tagCache.putAll(tagCacheNew)
            } finally {
                cacheMutex.writeLock().unlock()
            }
            
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
     * Export cache to JSON format
     * Maps to EvalCache.export() from pkg/handler/eval_cache_fetcher.go
     */
    fun export(): EvalCacheJSON {
        cacheMutex.readLock().lock()
        try {
            val flags = idCache.values.toList()
            return EvalCacheJSON(flags = flags.map { it.toEvalCacheExport() })
        } finally {
            cacheMutex.readLock().unlock()
        }
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
