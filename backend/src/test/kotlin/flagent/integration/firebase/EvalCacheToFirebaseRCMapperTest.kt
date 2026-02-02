package flagent.integration.firebase

import flagent.cache.impl.EvalCacheDistributionExport
import flagent.cache.impl.EvalCacheFlagExport
import flagent.cache.impl.EvalCacheJSON
import flagent.cache.impl.EvalCacheSegmentExport
import flagent.cache.impl.EvalCacheVariantExport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvalCacheToFirebaseRCMapperTest {

    @Test
    fun `maps disabled flag to false`() {
        val cache = EvalCacheJSON(
            flags = listOf(
                EvalCacheFlagExport(key = "my_flag", description = "", enabled = false, variants = emptyList())
            )
        )
        val result = EvalCacheToFirebaseRCMapper.map(cache)
        assertTrue(result.contains("\"my_flag\""))
        assertTrue(result.contains("\"value\":\"false\""))
    }

    @Test
    fun `maps enabled flag without variants to true`() {
        val cache = EvalCacheJSON(
            flags = listOf(
                EvalCacheFlagExport(key = "simple_flag", description = "", enabled = true, variants = emptyList())
            )
        )
        val result = EvalCacheToFirebaseRCMapper.map(cache)
        assertTrue(result.contains("\"simple_flag\""))
        assertTrue(result.contains("\"value\":\"true\""))
    }

    @Test
    fun `maps flag with variants to JSON`() {
        val cache = EvalCacheJSON(
            flags = listOf(
                EvalCacheFlagExport(
                    key = "exp_flag",
                    description = "",
                    enabled = true,
                    segments = listOf(
                        EvalCacheSegmentExport(
                            id = 1,
                            flagId = 1,
                            rank = 0,
                            distributions = listOf(
                                EvalCacheDistributionExport(
                                    segmentId = 1,
                                    variantId = 10,
                                    variantKey = "treatment",
                                    percent = 50
                                )
                            )
                        )
                    ),
                    variants = listOf(
                        EvalCacheVariantExport(id = 10, flagId = 1, key = "treatment", attachment = mapOf("color" to "blue"))
                    )
                )
            )
        )
        val result = EvalCacheToFirebaseRCMapper.map(cache)
        assertTrue(result.contains("exp_flag"))
        assertTrue(result.contains("variant"))
        assertTrue(result.contains("treatment"))
        assertTrue(result.contains("attachment"))
        assertTrue(result.contains("color"))
        assertTrue(result.contains("blue"))
    }

    @Test
    fun `applies parameter prefix`() {
        val cache = EvalCacheJSON(
            flags = listOf(
                EvalCacheFlagExport(key = "foo", description = "", enabled = true, variants = emptyList())
            )
        )
        val result = EvalCacheToFirebaseRCMapper.map(cache, "flagent_")
        assertTrue(result.contains("\"flagent_foo\""))
    }

    @Test
    fun `output has valid parameters structure`() {
        val cache = EvalCacheJSON(
            flags = listOf(
                EvalCacheFlagExport(key = "a", description = "", enabled = true, variants = emptyList()),
                EvalCacheFlagExport(key = "b", description = "", enabled = false, variants = emptyList())
            )
        )
        val result = EvalCacheToFirebaseRCMapper.map(cache)
        assertTrue(result.startsWith("{"))
        assertTrue(result.contains("\"parameters\""))
        assertTrue(result.contains("\"defaultValue\""))
    }
}
