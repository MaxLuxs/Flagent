package flagent.evaluator

/**
 * Canonical rollout algorithm for consistent hashing and variant selection.
 * All clients (backend, SDKs) MUST use this for identical results.
 *
 * Spec: docs/architecture/evaluation-spec.md
 * - Hash input: salt + entityID (no separator)
 * - CRC32 polynomial 0xEDB88320, bucket = crc32 % 1000
 * - Rollout: entity in rollout iff bucket < rolloutPercent * 10
 * - Distribution: accumulated percents (0-1000), binary search by bucket+1
 */
object RolloutAlgorithm {

    const val TOTAL_BUCKET_NUM = 1000u

    /**
     * CRC32 (polynomial 0xEDB88320). Pure Kotlin, no JVM-specific APIs.
     */
    fun crc32(bytes: ByteArray): UInt {
        var crc = 0xFFFFFFFFu
        for (byte in bytes) {
            crc = crc xor byte.toUByte().toUInt()
            for (k in 0 until 8) {
                crc = if ((crc and 1u) != 0u) {
                    (crc shr 1) xor 0xEDB88320u
                } else {
                    crc shr 1
                }
            }
        }
        return crc xor 0xFFFFFFFFu
    }

    /**
     * Compute bucket (0..999) for entity. Hash input = salt + entityID.
     */
    fun bucket(entityID: String, salt: String): UInt {
        val input = salt + entityID
        return crc32(input.encodeToByteArray()) % TOTAL_BUCKET_NUM
    }

    /**
     * Rollout and variant selection. Returns (variantID, debugMessage).
     * - variantIds: list of variant IDs in distribution order
     * - percentsAccumulated: accumulated percents in 0-1000 scale (e.g. [500, 1000] for 50/50)
     */
    fun rollout(
        entityID: String,
        salt: String,
        rolloutPercent: Int,
        variantIds: List<Int>,
        percentsAccumulated: List<Int>
    ): Pair<Int?, String> {
        if (entityID.isEmpty()) {
            return null to "rollout no. empty entityID"
        }
        if (rolloutPercent <= 0) {
            return null to "rollout no. invalid rolloutPercent: $rolloutPercent"
        }
        if (variantIds.isEmpty() || percentsAccumulated.isEmpty()) {
            return null to "rollout no. there's no distribution set"
        }

        val bucketNum = bucket(entityID, salt)
        val bucketInt = (bucketNum + 1u).toInt() // 1..1000

        val rolloutThreshold = rolloutPercent * 10 // 0-1000
        if (rolloutPercent < 100 && bucketInt > rolloutThreshold) {
            return null to "rollout no. entityID bucket: $bucketNum rolloutPercent: $rolloutPercent"
        }

        val index = bucketByNum(bucketInt, percentsAccumulated)
        val variantID = variantIds.getOrElse(index) { variantIds.last() }
        return variantID to "rollout yes. BucketNum: $bucketNum, VariantID: $variantID, RolloutPercent: $rolloutPercent"
    }

    /**
     * Binary search: find smallest i such that percentsAccumulated[i] >= bucketInt.
     * percentsAccumulated is in 0-1000 scale (e.g. [500, 1000]).
     */
    private fun bucketByNum(bucketInt: Int, percentsAccumulated: List<Int>): Int {
        val idx = percentsAccumulated.binarySearch(bucketInt)
        return if (idx >= 0) idx else -idx - 1
    }
}
