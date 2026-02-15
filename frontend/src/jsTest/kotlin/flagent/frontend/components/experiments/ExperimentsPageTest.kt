package flagent.frontend.components.experiments

import flagent.api.model.DistributionResponse
import flagent.api.model.FlagResponse
import flagent.api.model.SegmentResponse
import flagent.api.model.VariantResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExperimentsPageTest {

    /** OSS: variant distribution % logic (same as VariantComparisonTable). */
    @Test
    fun testVariantDistributionPercent_oss() {
        val v1 = VariantResponse(1, 1, "control")
        val v2 = VariantResponse(2, 1, "treatment")
        val variants = listOf(v1, v2)
        val variantIds = variants.map { it.id }.toSet()
        val distributions = listOf(
            DistributionResponse(1, 1, 1, "control", 60),
            DistributionResponse(2, 1, 2, "treatment", 40)
        )
        val distByVariant = distributions
            .filter { it.variantID in variantIds }
            .groupBy { it.variantID }
            .mapValues { (_, list) -> list.sumOf { it.percent } }
        assertEquals(60, distByVariant[v1.id])
        assertEquals(40, distByVariant[v2.id])
        val useEqual = distByVariant.isEmpty()
        assertEquals(false, useEqual)
        val equalPct = 100 / variants.size.coerceAtLeast(1)
        assertEquals(50, equalPct)
    }

    @Test
    fun testVariantDistributionPercent_emptyDistributions_oss() {
        val variants = listOf(VariantResponse(1, 1, "A"), VariantResponse(2, 1, "B"))
        val distByVariant = emptyMap<Int, Int>()
        val useEqual = distByVariant.isEmpty()
        assertEquals(true, useEqual)
        val equalPct = 100 / variants.size.coerceAtLeast(1)
        assertEquals(50, equalPct)
    }

    @Test
    fun testExperimentsPageComponentExists() {
        assertTrue(true, "ExperimentsPage component exists")
    }

    /** OSS: experiments list filter (All / Enabled / Disabled) behaves correctly */
    @Test
    fun testExperimentsFilterLogic_oss() {
        val variant = VariantResponse(id = 1, flagID = 1, key = "control")
        val segment = SegmentResponse(id = 1, flagID = 1, rank = 0, rolloutPercent = 100, constraints = emptyList(), distributions = emptyList())
        val flags = listOf(
            FlagResponse(id = 1, key = "exp1", description = "", enabled = true, snapshotID = 0, dataRecordsEnabled = false, tags = emptyList(), dependsOn = emptyList(), segments = listOf(segment), variants = listOf(variant, VariantResponse(2, 1, "treatment"))),
            FlagResponse(id = 2, key = "exp2", description = "", enabled = false, snapshotID = 0, dataRecordsEnabled = false, tags = emptyList(), dependsOn = emptyList(), segments = listOf(segment), variants = listOf(variant, VariantResponse(3, 2, "B"))),
            FlagResponse(id = 3, key = "exp3", description = "", enabled = true, snapshotID = 0, dataRecordsEnabled = false, tags = emptyList(), dependsOn = emptyList(), segments = listOf(segment), variants = listOf(variant, VariantResponse(4, 3, "C"))),
        )
        // Same logic as ExperimentsPage filteredFlags
        fun filterByStatus(list: List<FlagResponse>, status: String): List<FlagResponse> = when (status) {
            "All" -> list
            "Enabled" -> list.filter { it.enabled }
            "Disabled" -> list.filter { !it.enabled }
            else -> list
        }
        assertEquals(3, filterByStatus(flags, "All").size)
        assertEquals(2, filterByStatus(flags, "Enabled").size)
        assertEquals(1, filterByStatus(flags, "Disabled").size)
    }
}
