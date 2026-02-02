package flagent.integration.firebase

import flagent.cache.impl.EvalCache
import flagent.cache.impl.EvalCacheFlagExport
import flagent.cache.impl.EvalCacheJSON
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirebaseRCSyncServiceTest {

    private fun createMockEvalCache(flags: List<EvalCacheFlagExport>): EvalCache {
        val cache = mockk<EvalCache>(relaxed = true)
        coEvery { cache.export() } returns EvalCacheJSON(flags = flags)
        return cache
    }

    @Test
    fun `performSync calls client getRemoteConfig and updateRemoteConfig when flags exist`() = runBlocking {
        val mockClient = MockFirebaseRemoteConfigClient()
        val cache = createMockEvalCache(
            listOf(
                EvalCacheFlagExport(key = "test_flag", description = "", enabled = true, variants = emptyList())
            )
        )
        val service = FirebaseRCSyncService(
            evalCache = cache,
            client = mockClient,
            syncInterval = 10.seconds
        )
        service.start()
        delay(100)
        service.stop()

        assertTrue(mockClient.getCallCount >= 1)
        assertTrue(mockClient.putCallCount >= 1)
        assertTrue(mockClient.lastPutTemplate?.contains("test_flag") == true)
        assertEquals("etag-1", mockClient.lastPutEtag)
    }

    @Test
    fun `performSync does not call update when flags are empty`() = runBlocking {
        val mockClient = MockFirebaseRemoteConfigClient()
        val cache = createMockEvalCache(emptyList())
        val service = FirebaseRCSyncService(
            evalCache = cache,
            client = mockClient,
            syncInterval = 10.seconds
        )
        service.start()
        delay(100)
        service.stop()

        assertEquals(0, mockClient.putCallCount)
        assertEquals(0, mockClient.getCallCount)
    }

    @Test
    fun `performSync applies parameter prefix`() = runBlocking {
        val mockClient = MockFirebaseRemoteConfigClient()
        val cache = createMockEvalCache(
            listOf(
                EvalCacheFlagExport(key = "foo", description = "", enabled = true, variants = emptyList())
            )
        )
        val service = FirebaseRCSyncService(
            evalCache = cache,
            client = mockClient,
            syncInterval = 10.seconds,
            parameterPrefix = "flagent_"
        )
        service.start()
        delay(100)
        service.stop()

        assertTrue(mockClient.lastPutTemplate?.contains("flagent_foo") == true)
    }

    @Test
    fun `performSync continues when updateRemoteConfig throws`() = runBlocking {
        val mockClient = MockFirebaseRemoteConfigClient(putSucceeds = false)
        val cache = createMockEvalCache(
            listOf(
                EvalCacheFlagExport(key = "test_flag", description = "", enabled = true, variants = emptyList())
            )
        )
        val service = FirebaseRCSyncService(
            evalCache = cache,
            client = mockClient,
            syncInterval = 10.seconds
        )
        service.start()
        delay(250)
        service.stop()

        assertTrue(mockClient.getCallCount >= 1)
        assertTrue(mockClient.putCallCount >= 1)
    }
}
