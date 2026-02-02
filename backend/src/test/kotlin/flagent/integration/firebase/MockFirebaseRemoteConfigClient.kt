package flagent.integration.firebase

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mock Firebase Remote Config client for tests.
 * Records calls and returns configurable responses.
 */
class MockFirebaseRemoteConfigClient(
    private var getResponse: Pair<String, String?> = """{"parameters":{}}""" to "etag-1",
    private var putSucceeds: Boolean = true
) : IFirebaseRemoteConfigClient {

    private val mutex = Mutex()
    var getCallCount = 0
        private set
    var putCallCount = 0
        private set
    var lastPutTemplate: String? = null
        private set
    var lastPutEtag: String? = null
        private set

    fun setGetResponse(body: String, etag: String? = "etag-1") {
        getResponse = body to etag
    }

    override suspend fun getRemoteConfig(): Pair<String, String?> = mutex.withLock {
        getCallCount++
        getResponse
    }

    override suspend fun updateRemoteConfig(templateJson: String, etag: String): String = mutex.withLock {
        putCallCount++
        lastPutTemplate = templateJson
        lastPutEtag = etag
        if (!putSucceeds) {
            throw FirebaseRcException("PUT failed (mock)")
        }
        """{"parameters":{}}"""
    }

    override fun close() {}
}
