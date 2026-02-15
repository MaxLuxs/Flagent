import Foundation

/// Canonical rollout: salt+entityID, CRC32, bucket 0..999. Matches shared/evaluator/RolloutAlgorithm.kt.
enum RolloutAlgorithm {
    static let totalBucketNum: UInt32 = 1000

    /// CRC32 (polynomial 0xEDB88320).
    static func crc32(_ bytes: [UInt8]) -> UInt32 {
        var crc: UInt32 = 0xFFFF_FFFF
        for byte in bytes {
            crc ^= UInt32(byte)
            for _ in 0..<8 {
                crc = (crc & 1) != 0 ? (crc >> 1) ^ 0xEDB8_8320 : crc >> 1
            }
        }
        return crc ^ 0xFFFF_FFFF
    }

    /// Bucket 0..999. Input = salt + entityID.
    static func bucket(entityID: String, salt: String) -> UInt32 {
        let input = salt + entityID
        let data = Array(input.utf8)
        return crc32(data) % totalBucketNum
    }

    /// Returns (variantID, message). variantIds and percentsAccumulated in 0-1000 scale.
    static func rollout(
        entityID: String,
        salt: String,
        rolloutPercent: Int,
        variantIds: [Int],
        percentsAccumulated: [Int]
    ) -> (Int?, String) {
        if entityID.isEmpty {
            return (nil, "rollout no. empty entityID")
        }
        if rolloutPercent <= 0 {
            return (nil, "rollout no. invalid rolloutPercent: \(rolloutPercent)")
        }
        if variantIds.isEmpty || percentsAccumulated.isEmpty {
            return (nil, "rollout no. there's no distribution set")
        }
        let bucketNum = bucket(entityID: entityID, salt: salt)
        let bucketInt = Int(bucketNum) + 1
        let rolloutThreshold = rolloutPercent * 10
        if rolloutPercent < 100 && bucketInt > rolloutThreshold {
            return (nil, "rollout no. entityID bucket: \(bucketNum) rolloutPercent: \(rolloutPercent)")
        }
        let index = bucketByNum(bucketInt: bucketInt, percentsAccumulated: percentsAccumulated)
        let variantID = index < variantIds.count ? variantIds[index] : variantIds.last!
        return (variantID, "rollout yes. BucketNum: \(bucketNum), VariantID: \(variantID), RolloutPercent: \(rolloutPercent)")
    }

    /// Smallest i such that percentsAccumulated[i] >= bucketInt.
    private static func bucketByNum(bucketInt: Int, percentsAccumulated: [Int]) -> Int {
        var low = 0
        var high = percentsAccumulated.count
        while low < high {
            let mid = low + (high - low) / 2
            if percentsAccumulated[mid] < bucketInt {
                low = mid + 1
            } else {
                high = mid
            }
        }
        return low
    }
}
