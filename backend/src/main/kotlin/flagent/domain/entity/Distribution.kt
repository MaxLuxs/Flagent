package flagent.domain.entity

/**
 * Distribution entity - represents distribution under segment and links to variant
 *
 * Domain entity - no framework dependencies
 */
data class Distribution(
    val id: Int = 0,
    val segmentId: Int,
    val variantId: Int,
    val variantKey: String? = null,
    val percent: Int = 0 // 0-100
) {
    companion object {
        /**
         * TotalBucketNum represents how many buckets we can use for consistent hashing
         */
        const val TOTAL_BUCKET_NUM = 1000u
        
        /**
         * PercentMultiplier = TotalBucketNum / 100
         */
        const val PERCENT_MULTIPLIER = 10u
    }
}
