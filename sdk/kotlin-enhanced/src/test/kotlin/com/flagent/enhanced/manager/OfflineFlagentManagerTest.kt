package com.flagent.enhanced.manager

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.client.infrastructure.BodyProvider
import com.flagent.client.infrastructure.HttpResponse
import com.flagent.client.models.Constraint
import com.flagent.client.models.Distribution
import com.flagent.client.models.Flag
import com.flagent.client.models.Segment
import com.flagent.client.models.Variant
import com.flagent.enhanced.config.OfflineFlagentConfig
import io.ktor.client.statement.HttpResponse as KtorResponse
import io.ktor.util.reflect.TypeInfo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private fun <T : Any> httpResponseWithBody(ktorResponse: KtorResponse, body: T): HttpResponse<T> {
    val provider = object : BodyProvider<T> {
        override suspend fun body(response: KtorResponse): T = body
        @Suppress("UNCHECKED_CAST")
        override suspend fun <V : Any> typedBody(response: KtorResponse, type: TypeInfo): V = body as V
    }
    return HttpResponse(ktorResponse, provider)
}

class OfflineFlagentManagerTest {

    @Test
    fun `evaluate returns result from local snapshot when bootstrapped`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()
        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export unavailable")
        val segment = Segment(
            id = 10L,
            flagID = 1L,
            description = "",
            rank = 1L,
            rolloutPercent = 100L,
            constraints = emptyList(),
            distributions = listOf(Distribution(100L, 10L, 20L, 100L, "variant_a"))
        )
        val variant = Variant(id = 20L, flagID = 1L, key = "variant_a", attachment = null)
        val flag = Flag(
            id = 1L,
            key = "offline_flag",
            description = "",
            enabled = true,
            dataRecordsEnabled = false,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery {
            flagApi.findFlags(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns httpResponseWithBody(ktorResponse, listOf(flag))

        val config = OfflineFlagentConfig(enablePersistence = false, autoRefresh = false)
        val manager = OfflineFlagentManager(exportApi, flagApi, config, null)

        manager.bootstrap()
        val result = manager.evaluate(flagKey = "offline_flag", entityID = "user123")

        assertTrue(result.isEnabled())
        assertEquals("variant_a", result.variantKey)
        assertEquals(1L, result.flagID)
        assertEquals("MATCH", result.reason)
    }

    @Test
    fun `evaluate throws when not bootstrapped`() = runBlocking {
        val exportApi = mockk<ExportApi>(relaxed = true)
        val flagApi = mockk<FlagApi>(relaxed = true)
        val config = OfflineFlagentConfig(enablePersistence = false, autoRefresh = false)
        val manager = OfflineFlagentManager(exportApi, flagApi, config, null)

        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                manager.evaluate(flagKey = "any", entityID = "user1")
            }
        }
    }

    @Test
    fun `evaluate returns FLAG_NOT_FOUND when flag not in snapshot`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()
        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export fail")
        val flag = Flag(
            id = 1L,
            key = "other_flag",
            description = "",
            enabled = true,
            dataRecordsEnabled = false,
            segments = null,
            variants = null
        )
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery {
            flagApi.findFlags(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns httpResponseWithBody(ktorResponse, listOf(flag))

        val config = OfflineFlagentConfig(enablePersistence = false, autoRefresh = false)
        val manager = OfflineFlagentManager(exportApi, flagApi, config, null)

        manager.bootstrap()
        val result = manager.evaluate(flagKey = "nonexistent", entityID = "user1")

        assertEquals("FLAG_NOT_FOUND", result.reason)
        assertNull(result.variantKey)
    }

    @Test
    fun `isReady returns true after bootstrap`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()
        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export fail")
        val flag = Flag(
            id = 1L,
            key = "x",
            description = "",
            enabled = true,
            dataRecordsEnabled = false,
            segments = null,
            variants = null
        )
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery {
            flagApi.findFlags(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns httpResponseWithBody(ktorResponse, listOf(flag))

        val config = OfflineFlagentConfig(enablePersistence = false, autoRefresh = false)
        val manager = OfflineFlagentManager(exportApi, flagApi, config, null)

        assertFalse(manager.isReady())
        manager.bootstrap()
        assertTrue(manager.isReady())
    }

    @Test
    fun `clearCache clears snapshot and isReady false`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()
        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export fail")
        val flag = Flag(
            id = 1L,
            key = "x",
            description = "",
            enabled = true,
            dataRecordsEnabled = false,
            segments = null,
            variants = null
        )
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery {
            flagApi.findFlags(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns httpResponseWithBody(ktorResponse, listOf(flag))

        val config = OfflineFlagentConfig(enablePersistence = false, autoRefresh = false)
        val manager = OfflineFlagentManager(exportApi, flagApi, config, null)

        manager.bootstrap()
        assertTrue(manager.isReady())
        manager.clearCache()
        assertFalse(manager.isReady())
    }
}
