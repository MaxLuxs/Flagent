package flagent.cache.impl

import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.net.URL
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * EvalCacheFetcher interface - fetches flags for evaluation cache
 */
interface EvalCacheFetcher {
    suspend fun fetch(): List<Flag>
}

/**
 * Create appropriate fetcher based on configuration
 */
fun createEvalCacheFetcher(flagRepository: IFlagRepository): EvalCacheFetcher {
    if (!AppConfig.evalOnlyMode) {
        return DbFetcher(flagRepository)
    }
    
    return when (AppConfig.dbDriver) {
        "json_file" -> JsonFileFetcher(filePath = AppConfig.dbConnectionStr)
        "json_http" -> JsonHttpFetcher(url = AppConfig.dbConnectionStr)
        else -> throw IllegalArgumentException(
            "failed to create evaluation cache fetcher. DBDriver:${AppConfig.dbDriver} is not supported"
        )
    }
}

/**
 * DbFetcher - fetches flags from database
 */
class DbFetcher(
    private val flagRepository: IFlagRepository
) : EvalCacheFetcher {
    override suspend fun fetch(): List<Flag> = withContext(Dispatchers.IO) {
        flagRepository.findAll(preload = true)
    }
}

/**
 * JsonFileFetcher - fetches flags from JSON file
 */
class JsonFileFetcher(
    private val filePath: String
) : EvalCacheFetcher {
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun fetch(): List<Flag> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("JSON file not found: $filePath")
            }
            
            val content = file.readText()
            val evalCacheJson = json.decodeFromString<EvalCacheJSON>(content)
            evalCacheJson.flags
        } catch (e: Exception) {
            logger.error(e) { "Failed to load flags from JSON file: $filePath" }
            throw e
        }
    }
}

/**
 * JsonHttpFetcher - fetches flags from HTTP endpoint
 */
class JsonHttpFetcher(
    private val url: String
) : EvalCacheFetcher {
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun fetch(): List<Flag> = withContext(Dispatchers.IO) {
        try {
            val timeout = AppConfig.evalCacheRefreshTimeout.inWholeMilliseconds.toInt()
            val connection = URL(url).openConnection()
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            
            val content = connection.getInputStream().bufferedReader().use { it.readText() }
            val evalCacheJson = json.decodeFromString<EvalCacheJSON>(content)
            evalCacheJson.flags
        } catch (e: Exception) {
            logger.error(e) { "Failed to load flags from HTTP URL: $url" }
            throw e
        }
    }
}
