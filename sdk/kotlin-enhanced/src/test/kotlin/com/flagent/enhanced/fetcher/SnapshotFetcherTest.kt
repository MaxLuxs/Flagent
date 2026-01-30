package com.flagent.enhanced.fetcher

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.client.infrastructure.BodyProvider
import com.flagent.client.infrastructure.HttpResponse
import com.flagent.client.models.Constraint
import com.flagent.client.models.Distribution
import com.flagent.client.models.Flag
import com.flagent.client.models.Segment
import com.flagent.client.models.Variant
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

class SnapshotFetcherTest {

    @Test
    fun `fetchSnapshot returns snapshot from export API`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>(relaxed = true)

        @Suppress("UNCHECKED_CAST")
        val body = mapOf<String, Any>(
            "1" to mapOf(
                "key" to "test_flag",
                "enabled" to true,
                "segments" to emptyList<Any>(),
                "variants" to emptyList<Any>()
            )
        ) as Map<String, Any>
        coEvery { exportApi.getExportEvalCacheJSON() } returns HttpResponse(
            mockk(relaxed = true),
            mockk {
                coEvery { body(any()) } returns body
            }
        )

        val fetcher = SnapshotFetcher(exportApi, flagApi)
        val snapshot = fetcher.fetchSnapshot(ttlMs = 60_000)

        assertNotNull(snapshot)
        assertEquals(1, snapshot.flags.size)
        assertTrue(snapshot.flags.containsKey(1L))
        assertEquals("test_flag", snapshot.flags[1L]?.key)
        assertTrue(snapshot.flags[1L]?.enabled == true)
        assertNotNull(snapshot.revision)
        assertTrue(snapshot.revision!!.startsWith("snapshot-"))
    }

    @Test
    fun `fetchSnapshot falls back to findFlags when export API throws`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()

        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export unavailable")
        val flag = Flag(
            id = 1L,
            key = "fallback_flag",
            description = "",
            enabled = true,
            dataRecordsEnabled = false,
            segments = null,
            variants = null
        )
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery {
            flagApi.findFlags(
                limit = 1000,
                offset = null,
                enabled = null,
                description = null,
                key = null,
                descriptionLike = null,
                preload = true,
                deleted = false,
                tags = null
            )
        } returns httpResponseWithBody(ktorResponse, listOf(flag))

        val fetcher = SnapshotFetcher(exportApi, flagApi)
        val snapshot = fetcher.fetchSnapshot(ttlMs = 60_000)

        assertNotNull(snapshot)
        assertEquals(1, snapshot.flags.size)
        assertEquals("fallback_flag", snapshot.flags[1L]?.key)
    }

    @Test
    fun `fetchSnapshot fallback maps segments and variants`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>()

        coEvery { exportApi.getExportEvalCacheJSON() } throws RuntimeException("Export fail")
        val segment = Segment(
            id = 10L,
            flagID = 1L,
            description = "",
            rank = 1L,
            rolloutPercent = 100L,
            constraints = listOf(
                Constraint(1L, 10L, "tier", Constraint.Operator.EQ, "premium")
            ),
            distributions = listOf(
                Distribution(100L, 10L, 20L, 100L, "variant_a")
            )
        )
        val variant = Variant(id = 20L, flagID = 1L, key = "variant_a", attachment = null)
        val flag = Flag(
            id = 1L,
            key = "mapped_flag",
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

        val fetcher = SnapshotFetcher(exportApi, flagApi)
        val snapshot = fetcher.fetchSnapshot(ttlMs = 60_000)

        assertNotNull(snapshot)
        val localFlag = snapshot.flags[1L]
        assertNotNull(localFlag)
        assertEquals(1, localFlag!!.segments.size)
        assertEquals(1, localFlag.segments[0].constraints.size)
        assertEquals("tier", localFlag.segments[0].constraints[0].property)
        assertEquals(1, localFlag.variants.size)
        assertEquals("variant_a", localFlag.variants[0].key)
    }

    @Test
    fun `fetchDelta returns full snapshot`() = runBlocking {
        val exportApi = mockk<ExportApi>()
        val flagApi = mockk<FlagApi>(relaxed = true)
        @Suppress("UNCHECKED_CAST")
        val body = mapOf<String, Any>(
            "1" to mapOf(
                "key" to "delta_flag",
                "enabled" to true,
                "segments" to emptyList<Any>(),
                "variants" to emptyList<Any>()
            )
        ) as Map<String, Any>
        val ktorResponse = mockk<KtorResponse>(relaxed = true)
        coEvery { exportApi.getExportEvalCacheJSON() } returns httpResponseWithBody(ktorResponse, body)

        val fetcher = SnapshotFetcher(exportApi, flagApi)
        val snapshot = fetcher.fetchDelta(lastRevision = "old-rev", ttlMs = 60_000)

        assertNotNull(snapshot)
        assertEquals("delta_flag", snapshot!!.flags[1L]?.key)
    }
}
