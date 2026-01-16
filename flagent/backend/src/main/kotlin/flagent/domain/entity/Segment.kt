package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Segment entity - the unit of segmentation
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class Segment(
    val id: Int = 0,
    val flagId: Int,
    val description: String? = null,
    val rank: Int = 999, // SegmentDefaultRank
    val rolloutPercent: Int = 0,
    val constraints: List<Constraint> = emptyList(),
    val distributions: List<Distribution> = emptyList()
) {
    /**
     * SegmentEvaluation holds necessary info for evaluation
     * Not stored in DB, computed at runtime
     */
    data class SegmentEvaluation(
        val conditionsExpr: String? = null, // Serialized expression
        val distributionArray: DistributionArray
    )
    
    /**
     * DistributionArray for faster evaluation
     */
    data class DistributionArray(
        val variantIds: List<Int>,
        val percentsAccumulated: List<Int> // Accumulated percentages for binary search
    ) {
        companion object {
            const val TOTAL_BUCKET_NUM = 1000u
        }
        
        /**
         * Rollout rolls out the entity based on the rolloutPercent
         * Uses consistent hashing (CRC32) for uniform distribution
         * Returns variant ID if rollout matches, null otherwise
         */
        fun rollout(entityID: String, salt: String, rolloutPercent: Int): Pair<Int?, String> {
            if (entityID.isEmpty()) {
                return null to "rollout no. empty entityID"
            }
            
            if (rolloutPercent <= 0) {
                return null to "rollout no. invalid rolloutPercent: $rolloutPercent"
            }
            
            if (variantIds.isEmpty() || percentsAccumulated.isEmpty()) {
                return null to "rollout no. there's no distribution set"
            }
            
            val bucketNum = crc32Num(entityID, salt)
            val (variantID, index) = bucketByNum(bucketNum)
            
            val log = "BucketNum: $bucketNum, VariantID: $variantID, RolloutPercent: $rolloutPercent"
            
            return if (rollout(bucketNum, rolloutPercent.toUInt(), index)) {
                variantID to "rollout yes. $log"
            } else {
                null to "rollout no. $log"
            }
        }
        
        private fun bucketByNum(bucketNum: UInt): Pair<Int, Int> {
            val bucketInt = (bucketNum + 1u).toInt()
            val index = percentsAccumulated.binarySearch(bucketInt)
            val actualIndex = if (index < 0) -index - 1 else index
            val variantID = variantIds.getOrElse(actualIndex) { variantIds.last() }
            return variantID to actualIndex
        }
        
        private fun rollout(bucketNum: UInt, rolloutPercent: UInt, index: Int): Boolean {
            if (rolloutPercent == 0u) return false
            if (rolloutPercent == 100u) return true
            
            val min = if (index > 0) percentsAccumulated[index - 1] else 0
            val max = percentsAccumulated[index]
            val r = if (max - min - 1 > 0) max - min - 1 else 0
            
            return 100u * (bucketNum - min.toUInt()) <= r.toUInt() * rolloutPercent
        }
        
        private fun crc32Num(entityID: String, salt: String): UInt {
            val bytes = (salt + entityID).toByteArray()
            val checksum = java.util.zip.CRC32()
            checksum.update(bytes)
            return (checksum.value.toUInt() % TOTAL_BUCKET_NUM.toUInt()).toUInt()
        }
    }
    
    /**
     * Prepare evaluation data structure
     */
    fun prepareEvaluation(): SegmentEvaluation {
        val distributionArray = DistributionArray(
            variantIds = distributions.map { it.variantId },
            percentsAccumulated = calculateAccumulatedPercents()
        )
        
        return SegmentEvaluation(
            distributionArray = distributionArray
        )
    }
    
    private fun calculateAccumulatedPercents(): List<Int> {
        val multiplier = 10 // PercentMultiplier = 1000 / 100 = 10
        val accumulated = mutableListOf<Int>()
        
        distributions.forEachIndexed { index, distribution ->
            val percent = distribution.percent * multiplier
            if (index == 0) {
                accumulated.add(percent)
            } else {
                accumulated.add(accumulated[index - 1] + percent)
            }
        }
        
        return accumulated
    }
}
