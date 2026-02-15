# Flagent Evaluation Specification

> [English](evaluation-spec.md) | [Русский](evaluation-spec.ru.md)

Canonical specification for flag evaluation, rollout, and constraint matching. All implementations (backend, shared evaluator, Kotlin-enhanced SDK, Go-enhanced SDK) MUST follow this spec for consistent results.

## 1. Hash input for rollout

- **Order**: `salt + entityID` (no separator)
- **Encoding**: UTF-8 bytes
- **Salt**: Typically the flag ID as string (e.g. `flag.id.toString()`)

Example: for `salt = "1"`, `entityID = "user_123"` → input = `"1user_123"`.

## 2. CRC32

- **Algorithm**: Standard CRC-32 (polynomial 0xEDB88320)
- **Input**: Byte array of the hash input string (UTF-8)
- **Output**: 32-bit unsigned integer
- **Usage**: `bucket = crc32(salt + entityID) % 1000` (bucket in 0..999)

Pure Kotlin implementation (no JVM-specific `java.util.zip.CRC32`):

```kotlin
fun crc32(bytes: ByteArray): UInt {
    var crc = 0xFFFFFFFFu
    for (byte in bytes) {
        crc = crc xor byte.toUByte().toUInt()
        for (k in 0 until 8) {
            crc = if ((crc and 1u) != 0u) (crc shr 1) xor 0xEDB88320u else crc shr 1
        }
    }
    return crc xor 0xFFFFFFFFu
}
```

## 3. Bucket and rollout

- **Total buckets**: 1000 (0..999)
- **Bucket**: `crc32((salt + entityID).toByteArray()) % 1000`
- **Rollout percent**: 0–100
- **Rollout threshold**: Entity is in rollout iff `bucket < rolloutThreshold`, where `rolloutThreshold = (rolloutPercent * 10)` (e.g. 50% → 500)

So for rollout we compare `bucket` (0..999) with `rolloutPercent * 10`. If `bucket >= rolloutThreshold`, the entity is not in the segment rollout (evaluate next segment).

## 4. Distribution (variant selection)

- **Accumulated percents**: For N variants with percents p1, p2, …, pN (sum 100), accumulated = [p1*10, p1*10+p2*10, …, 1000].
- **Variant index**: Binary search on accumulated percents using `bucket + 1` (1..1000) so that bucket 0 maps to first slice.
- **Formula**: `bucketInt = (bucket % 1000) + 1` (value in 1..1000). Find index i such that `accumulated[i-1] < bucketInt <= accumulated[i]` (with accumulated[-1]=0). Return `variantIds[i]`.

Backend-compatible bucket-to-variant (Segment.DistributionArray legacy):

- `bucketNum = crc32(salt + entityID) % 1000` (0..999)
- `bucketInt = bucketNum + 1` (1..1000)
- Binary search: find smallest i with `percentsAccumulated[i] >= bucketInt`. Variant = `variantIds[i]`.

Rollout check (same as above): if `rolloutPercent == 0` → not in rollout. If `rolloutPercent == 100` → in rollout. Otherwise entity is in rollout iff the bucket falls within the segment’s rollout window (e.g. `bucket < rolloutPercent * 10`).

## 5. Constraint operators

Supported operators for segment constraints:

| Operator   | Description        |
|-----------|---------------------|
| EQ        | Equals              |
| NEQ       | Not equals          |
| LT        | Less than           |
| LTE       | Less than or equal  |
| GT        | Greater than        |
| GTE       | Greater than or equal |
| EREG      | Regex match         |
| NEREG     | Regex not match     |
| IN        | Value in comma-separated list |
| NOTIN     | Value not in list   |
| CONTAINS  | String contains    |
| NOTCONTAINS | String does not contain |

Property value from `entityContext` is compared to constraint `value` as strings (numeric comparison for LT/LTE/GT/GTE).

## 6. Evaluation flow

1. Resolve flag (by ID or key).
2. If flag disabled or no segments → return no variant.
3. Sort segments by `rank` ascending.
4. For each segment in order:
   - Evaluate constraints (all must match). If any fails → continue to next segment.
   - Build distribution (accumulated percents from variant percents).
   - Compute bucket = `crc32(salt + entityID) % 1000`, salt = flag ID.
   - Check rollout: if `bucket >= rolloutPercent * 10` → continue to next segment.
   - Select variant by bucket (see Distribution above). Return variant + segment.
5. If no segment matched → return no variant (segmentID = last evaluated segment if needed for debug).

## 7. Consistency requirements

- All clients (backend, shared, Kotlin-enhanced, Go-enhanced) MUST use:
  - Hash input: `salt + entityID`
  - Same CRC32 polynomial and bucket formula
  - Same rollout threshold: `bucket < rolloutPercent * 10`
  - Same distribution accumulation (percent * 10, sum) and binary search

This ensures the same entityID always gets the same variant for the same flag/segment configuration.
