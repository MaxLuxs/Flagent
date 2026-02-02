package flagent.cache.impl

import flagent.domain.entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvalCacheExportTest {

    @Test
    fun flagWithFullData_mapsToEvalCacheFlagExport() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test description",
            createdBy = "user1",
            updatedBy = "user2",
            enabled = true,
            snapshotId = 10,
            notes = "notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = listOf(
                Segment(
                    id = 1,
                    flagId = 1,
                    description = "Segment 1",
                    rank = 1,
                    rolloutPercent = 100,
                    constraints = listOf(
                        Constraint(
                            id = 1,
                            segmentId = 1,
                            property = "region",
                            operator = "EQ",
                            value = "US"
                        )
                    ),
                    distributions = listOf(
                        Distribution(
                            id = 1,
                            segmentId = 1,
                            variantId = 10,
                            variantKey = "variant_a",
                            percent = 100
                        )
                    )
                )
            ),
            variants = listOf(
                Variant(
                    id = 10,
                    flagId = 1,
                    key = "variant_a",
                    attachment = mapOf("config" to "value")
                )
            ),
            tags = listOf(Tag(id = 1, value = "tag1")),
            updatedAt = "2024-01-01T00:00:00Z"
        )

        val export = flag.toEvalCacheExport()

        assertEquals(1, export.id)
        assertEquals("test_flag", export.key)
        assertEquals("Test description", export.description)
        assertEquals("user1", export.createdBy)
        assertEquals("user2", export.updatedBy)
        assertTrue(export.enabled)
        assertEquals(10, export.snapshotId)
        assertEquals("notes", export.notes)
        assertTrue(export.dataRecordsEnabled)
        assertEquals("user", export.entityType)
        assertEquals("2024-01-01T00:00:00Z", export.updatedAt)

        assertEquals(1, export.segments.size)
        val seg = export.segments[0]
        assertEquals(1, seg.id)
        assertEquals(1, seg.flagId)
        assertEquals("Segment 1", seg.description)
        assertEquals(1, seg.rank)
        assertEquals(100, seg.rolloutPercent)
        assertEquals(1, seg.constraints.size)
        assertEquals("region", seg.constraints[0].property)
        assertEquals("EQ", seg.constraints[0].operator)
        assertEquals("US", seg.constraints[0].value)
        assertEquals(1, seg.distributions.size)
        assertEquals(10, seg.distributions[0].variantId)
        assertEquals("variant_a", seg.distributions[0].variantKey)
        assertEquals(100, seg.distributions[0].percent)

        assertEquals(1, export.variants.size)
        assertEquals(10, export.variants[0].id)
        assertEquals("variant_a", export.variants[0].key)
        assertEquals(mapOf("config" to "value"), export.variants[0].attachment)

        assertEquals(1, export.tags.size)
        assertEquals(1, export.tags[0].id)
        assertEquals("tag1", export.tags[0].value)
    }

    @Test
    fun flagWithEmptyLists_mapsToEmptyExportLists() {
        val flag = Flag(
            id = 2,
            key = "empty_flag",
            description = "",
            segments = emptyList(),
            variants = emptyList(),
            tags = emptyList()
        )

        val export = flag.toEvalCacheExport()

        assertEquals(2, export.id)
        assertEquals("empty_flag", export.key)
        assertTrue(export.segments.isEmpty())
        assertTrue(export.variants.isEmpty())
        assertTrue(export.tags.isEmpty())
    }

    @Test
    fun segmentWithConstraintsAndDistributions_mapsCorrectly() {
        val segment = Segment(
            id = 5,
            flagId = 3,
            description = "Seg",
            rank = 2,
            rolloutPercent = 50,
            constraints = listOf(
                Constraint(id = 1, segmentId = 5, property = "tier", operator = "IN", value = "premium,enterprise"),
                Constraint(id = 2, segmentId = 5, property = "version", operator = "GTE", value = "1.0")
            ),
            distributions = listOf(
                Distribution(id = 1, segmentId = 5, variantId = 20, variantKey = "control", percent = 50),
                Distribution(id = 2, segmentId = 5, variantId = 21, variantKey = "treatment", percent = 50)
            )
        )

        val flag = Flag(
            id = 3,
            key = "flag_with_segment",
            description = "",
            segments = listOf(segment),
            variants = listOf(
                Variant(id = 20, flagId = 3, key = "control"),
                Variant(id = 21, flagId = 3, key = "treatment")
            )
        )

        val export = flag.toEvalCacheExport()

        assertEquals(2, export.segments[0].constraints.size)
        assertEquals("tier", export.segments[0].constraints[0].property)
        assertEquals("IN", export.segments[0].constraints[0].operator)
        assertEquals("version", export.segments[0].constraints[1].property)

        assertEquals(2, export.segments[0].distributions.size)
        assertEquals(20, export.segments[0].distributions[0].variantId)
        assertEquals(21, export.segments[0].distributions[1].variantId)
    }

    @Test
    fun roundTrip_flagToExportToFlag_preservesData() {
        val original = Flag(
            id = 4,
            key = "roundtrip_flag",
            description = "Roundtrip test",
            createdBy = "a",
            updatedBy = "b",
            enabled = true,
            segments = listOf(
                Segment(
                    id = 1,
                    flagId = 4,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = listOf(Constraint(id = 1, segmentId = 1, property = "x", operator = "EQ", value = "y")),
                    distributions = listOf(Distribution(id = 1, segmentId = 1, variantId = 1, percent = 100))
                )
            ),
            variants = listOf(Variant(id = 1, flagId = 4, key = "v1")),
            tags = listOf(Tag(id = 1, value = "t1"))
        )

        val export = original.toEvalCacheExport()
        val restored = export.toFlag()

        assertEquals(original.id, restored.id)
        assertEquals(original.key, restored.key)
        assertEquals(original.description, restored.description)
        assertEquals(original.enabled, restored.enabled)
        assertEquals(original.segments.size, restored.segments.size)
        assertEquals(original.segments[0].id, restored.segments[0].id)
        assertEquals(original.segments[0].constraints[0].property, restored.segments[0].constraints[0].property)
        assertEquals(original.variants[0].key, restored.variants[0].key)
        assertEquals(original.tags[0].value, restored.tags[0].value)
    }

    @Test
    fun flagWithNullOptionalFields_mapsWithDefaults() {
        val flag = Flag(
            id = 0,
            key = "minimal",
            description = "",
            createdBy = null,
            updatedBy = null,
            notes = null,
            entityType = null,
            updatedAt = null
        )

        val export = flag.toEvalCacheExport()

        assertEquals(null, export.createdBy)
        assertEquals(null, export.updatedBy)
        assertEquals(null, export.notes)
        assertEquals(null, export.entityType)
        assertEquals(null, export.updatedAt)
    }
}
