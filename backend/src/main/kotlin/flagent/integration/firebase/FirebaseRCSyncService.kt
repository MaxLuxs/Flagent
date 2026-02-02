package flagent.integration.firebase

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Periodically syncs EvalCache to Firebase Remote Config.
 */
class FirebaseRCSyncService(
    private val evalCache: EvalCache,
    private val client: IFirebaseRemoteConfigClient = FirebaseRemoteConfigClient(),
    private val syncInterval: kotlin.time.Duration = AppConfig.firebaseRcSyncInterval,
    private val parameterPrefix: String = AppConfig.firebaseRcParameterPrefix
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var syncJob: Job? = null
    private var lastEtag: String? = null

    fun start() {
        syncJob = scope.launch {
            while (isActive) {
                try {
                    performSync()
                } catch (e: Exception) {
                    logger.error(e) { "Firebase RC sync failed" }
                }
                delay(syncInterval)
            }
        }
        logger.info { "Firebase RC sync started, interval=$syncInterval" }
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
        client.close()
        logger.info { "Firebase RC sync stopped" }
    }

    private suspend fun performSync() = withContext(Dispatchers.IO) {
        val cacheJson = evalCache.export()
        if (cacheJson.flags.isEmpty()) {
            logger.debug { "Firebase RC sync: no flags to sync" }
            return@withContext
        }
        if (cacheJson.flags.size > EvalCacheToFirebaseRCMapper.FIREBASE_RC_MAX_PARAMETERS) {
            logger.warn {
                "Firebase RC sync: ${cacheJson.flags.size} flags exceed limit " +
                    EvalCacheToFirebaseRCMapper.FIREBASE_RC_MAX_PARAMETERS + ", truncating"
            }
        }
        val templateJson = EvalCacheToFirebaseRCMapper.map(cacheJson, parameterPrefix)
        val etag = lastEtag ?: run {
            val (_, e) = client.getRemoteConfig()
            e ?: "*"
        }.also { lastEtag = it }
        client.updateRemoteConfig(templateJson, etag)
        val (_, newEtag) = client.getRemoteConfig()
        lastEtag = newEtag
        logger.info { "Firebase RC sync: published ${cacheJson.flags.size} flags" }
    }
}
