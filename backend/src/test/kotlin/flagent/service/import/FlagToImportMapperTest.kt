package flagent.service.import

import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.domain.entity.Tag
import flagent.domain.entity.Variant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlagToImportMapperTest {

    @Test
    fun `toFlagImportItem maps all scalar fields and collections`() {
        val flag = Flag(
            id = 42,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            notes = "notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = emptyList(),
            variants = listOf(
                Variant(
                    id = 1,
                    flagId = 42,
                    key = "control",
                    attachment = mapOf("k" to "v")
                )
            ),
            tags = listOf(
                Tag(value = "tag-a"),
                Tag(value = "tag-b")
            )
        )

        val item = flag.toFlagImportItem()

        assertEquals(flag.key, item.key)
        assertEquals(flag.description, item.description)
        assertTrue(item.enabled)
        assertEquals(flag.notes, item.notes)
        assertTrue(item.dataRecordsEnabled)
        assertEquals(flag.entityType, item.entityType)

        // Variants
        assertEquals(1, item.variants.size)
        val v = item.variants.first()
        assertEquals("control", v.key)
        assertEquals(mapOf("k" to "v"), v.attachment)

        // Tags
        assertEquals(listOf("tag-a", "tag-b"), item.tags)
    }

    @Test
    fun `toFlagImportItem maps segments constraints and distributions with variant resolution`() {
        val flagId = 100
        val variantFromFlag = Variant(
            id = 10,
            flagId = flagId,
            key = "from-variant-list"
        )

        val segment = Segment(
            flagId = flagId,
            description = "seg-desc",
            rank = 5,
            rolloutPercent = 80,
            constraints = listOf(
                Constraint(
                    segmentId = 1,
                    property = "country",
                    operator = "EQ",
                    value = "US"
                )
            ),
            distributions = listOf(
                // Explicit variantKey wins
                Distribution(
                    segmentId = 1,
                    variantId = 999,
                    variantKey = "explicit-key",
                    percent = 30
                ),
                // Fallback to variant from flag.variants by id
                Distribution(
                    segmentId = 1,
                    variantId = variantFromFlag.id,
                    variantKey = null,
                    percent = 50
                ),
                // No variantKey and no matching variant -> empty string
                Distribution(
                    segmentId = 1,
                    variantId = 12345,
                    variantKey = null,
                    percent = 20
                )
            )
        )

        val flag = Flag(
            id = flagId,
            key = "complex_flag",
            description = "with segments",
            enabled = false,
            segments = listOf(segment),
            variants = listOf(variantFromFlag),
            tags = emptyList()
        )

        val item = flag.toFlagImportItem()

        assertEquals(1, item.segments.size)
        val segItem = item.segments.single()

        assertEquals(segment.rank, segItem.rank)
        assertEquals(segment.description, segItem.description)
        assertEquals(segment.rolloutPercent, segItem.rolloutPercent)

        // Constraints mapping
        assertEquals(1, segItem.constraints.size)
        val cItem = segItem.constraints.single()
        assertEquals("country", cItem.property)
        assertEquals("EQ", cItem.operator)
        assertEquals("US", cItem.value)

        // Distributions mapping and variantKey resolution
        assertEquals(3, segItem.distributions.size)
        val d0 = segItem.distributions[0]
        val d1 = segItem.distributions[1]
        val d2 = segItem.distributions[2]

        assertEquals("explicit-key", d0.variantKey)
        assertEquals(30, d0.percent)

        assertEquals("from-variant-list", d1.variantKey)
        assertEquals(50, d1.percent)

        assertEquals("", d2.variantKey)
        assertEquals(20, d2.percent)

        // Sanity: imported flag is still disabled
        assertFalse(item.enabled)
    }

    @Test
    fun `toFlagImportItem handles empty collections gracefully`() {
        val flag = Flag(
            id = 0,
            key = "empty_flag",
            description = "",
            enabled = false
        )

        val item = flag.toFlagImportItem()

        assertNotNull(item)
        assertTrue(item.segments.isEmpty())
        assertTrue(item.variants.isEmpty())
        assertTrue(item.tags.isEmpty())
    }
}

