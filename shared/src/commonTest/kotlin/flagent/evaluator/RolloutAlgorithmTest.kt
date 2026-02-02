package flagent.evaluator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RolloutAlgorithmTest {

    @Test
    fun crc32_emptyByteArray_returnsZero() {
        assertEquals(0u, RolloutAlgorithm.crc32(ByteArray(0)))
    }

    @Test
    fun crc32_knownVector123456789_returnsStandardValue() {
        val bytes = "123456789".encodeToByteArray()
        assertEquals(0xCBF43926u, RolloutAlgorithm.crc32(bytes))
    }

    @Test
    fun crc32_isDeterministic_sameInputSameOutput() {
        val bytes = "test_entity".encodeToByteArray()
        val r1 = RolloutAlgorithm.crc32(bytes)
        val r2 = RolloutAlgorithm.crc32(bytes)
        assertEquals(r1, r2)
    }

    @Test
    fun bucket_returnsValueInRange0to999() {
        repeat(100) { i ->
            val entityID = "entity_$i"
            val salt = "salt_$i"
            val b = RolloutAlgorithm.bucket(entityID, salt)
            assertTrue(b in 0u..999u, "bucket $b out of range for entityID=$entityID")
        }
    }

    @Test
    fun bucket_isDeterministic_sameInputSameOutput() {
        val b1 = RolloutAlgorithm.bucket("user123", "flag_salt")
        val b2 = RolloutAlgorithm.bucket("user123", "flag_salt")
        assertEquals(b1, b2)
    }

    @Test
    fun bucket_differentEntityIDs_differentBuckets() {
        val buckets = (1..50).map { RolloutAlgorithm.bucket("entity_$it", "1") }.toSet()
        assertTrue(buckets.size > 1, "different entityIDs should produce different buckets")
    }

    @Test
    fun rollout_emptyEntityID_returnsNullAndMessage() {
        val (variantId, message) = RolloutAlgorithm.rollout(
            entityID = "",
            salt = "1",
            rolloutPercent = 100,
            variantIds = listOf(10),
            percentsAccumulated = listOf(1000)
        )
        assertNull(variantId)
        assertTrue(message.contains("empty entityID"))
    }

    @Test
    fun rollout_rolloutPercentZero_returnsNullAndMessage() {
        val (variantId, message) = RolloutAlgorithm.rollout(
            entityID = "user1",
            salt = "1",
            rolloutPercent = 0,
            variantIds = listOf(10),
            percentsAccumulated = listOf(1000)
        )
        assertNull(variantId)
        assertTrue(message.contains("invalid rolloutPercent"))
    }

    @Test
    fun rollout_rolloutPercentNegative_returnsNullAndMessage() {
        val (variantId, _) = RolloutAlgorithm.rollout(
            entityID = "user1",
            salt = "1",
            rolloutPercent = -1,
            variantIds = listOf(10),
            percentsAccumulated = listOf(1000)
        )
        assertNull(variantId)
    }

    @Test
    fun rollout_emptyVariantIds_returnsNullAndMessage() {
        val (variantId, message) = RolloutAlgorithm.rollout(
            entityID = "user1",
            salt = "1",
            rolloutPercent = 100,
            variantIds = emptyList(),
            percentsAccumulated = listOf(1000)
        )
        assertNull(variantId)
        assertTrue(message.contains("no distribution"))
    }

    @Test
    fun rollout_emptyPercentsAccumulated_returnsNullAndMessage() {
        val (variantId, message) = RolloutAlgorithm.rollout(
            entityID = "user1",
            salt = "1",
            rolloutPercent = 100,
            variantIds = listOf(10),
            percentsAccumulated = emptyList()
        )
        assertNull(variantId)
        assertTrue(message.contains("no distribution"))
    }

    @Test
    fun rollout_rolloutPercent100_alwaysReturnsVariant() {
        val variantIds = listOf(10, 11)
        val percentsAccumulated = listOf(500, 1000)
        repeat(50) { i ->
            val (variantId, message) = RolloutAlgorithm.rollout(
                entityID = "entity_$i",
                salt = "1",
                rolloutPercent = 100,
                variantIds = variantIds,
                percentsAccumulated = percentsAccumulated
            )
            assertTrue(variantId != null, "entity_$i: $message")
            assertTrue(variantId in variantIds)
        }
    }

    @Test
    fun rollout_rolloutPercent50_someEntitiesExcluded() {
        val variantIds = listOf(10)
        val percentsAccumulated = listOf(1000)
        var included = 0
        repeat(200) { i ->
            val (variantId, _) = RolloutAlgorithm.rollout(
                entityID = "entity_$i",
                salt = "1",
                rolloutPercent = 50,
                variantIds = variantIds,
                percentsAccumulated = percentsAccumulated
            )
            if (variantId != null) included++
        }
        assertTrue(included in 80..120, "50% rollout should include ~100 of 200, got $included")
    }

    @Test
    fun rollout_singleVariant100Percent_returnsThatVariant() {
        val (variantId, message) = RolloutAlgorithm.rollout(
            entityID = "test_entity_123",
            salt = "1",
            rolloutPercent = 100,
            variantIds = listOf(42),
            percentsAccumulated = listOf(1000)
        )
        assertEquals(42, variantId)
        assertTrue(message.contains("VariantID: 42"))
    }

    @Test
    fun rollout_fiftyFiftyDistribution_returnsEitherVariant() {
        val variantIds = listOf(10, 11)
        val percentsAccumulated = listOf(500, 1000)
        val (variantId, _) = RolloutAlgorithm.rollout(
            entityID = "test_entity_123",
            salt = "1",
            rolloutPercent = 100,
            variantIds = variantIds,
            percentsAccumulated = percentsAccumulated
        )
        assertTrue(variantId in variantIds)
    }

    @Test
    fun rollout_isDeterministic_sameInputSameOutput() {
        val r1 = RolloutAlgorithm.rollout(
            entityID = "user456",
            salt = "flag_1",
            rolloutPercent = 100,
            variantIds = listOf(1, 2),
            percentsAccumulated = listOf(500, 1000)
        )
        val r2 = RolloutAlgorithm.rollout(
            entityID = "user456",
            salt = "flag_1",
            rolloutPercent = 100,
            variantIds = listOf(1, 2),
            percentsAccumulated = listOf(500, 1000)
        )
        assertEquals(r1.first, r2.first)
        assertEquals(r1.second, r2.second)
    }
}
