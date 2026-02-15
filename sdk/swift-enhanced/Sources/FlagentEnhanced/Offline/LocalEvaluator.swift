import Foundation
import FlagentClient
import AnyCodable

/// Result of local evaluation; can be converted to EvalResult.
struct OfflineEvalResult {
    let flagID: Int64?
    let flagKey: String?
    let variantID: Int64?
    let variantKey: String?
    let variantAttachment: [String: AnyCodable]?
    let segmentID: Int64?
    let evalDebugLog: EvalDebugLog?

    func toEvalResult() -> EvalResult {
        EvalResult(
            flagID: flagID,
            flagKey: flagKey,
            variantID: variantID,
            variantKey: variantKey,
            variantAttachment: variantAttachment,
            evalContext: nil,
            timestamp: Date(),
            evalDebugLog: evalDebugLog
        )
    }
}

/// Evaluates a flag against a snapshot locally.
struct LocalEvaluator {
    private let constraintEvaluator = ConstraintEvaluator()

    func evaluate(
        flagKey: String?,
        flagID: Int64?,
        entityID: String,
        entityType: String?,
        entityContext: [String: AnyCodable]?,
        snapshot: OfflineFlagSnapshot,
        enableDebug: Bool
    ) -> OfflineEvalResult {
        let contextStrings = (entityContext ?? [:]).mapValues { v in
            (v.value as? String) ?? String(describing: v.value)
        }
        let flag: OfflineLocalFlag? = {
            if let key = flagKey {
                return snapshot.flags.values.first { $0.key == key }
            }
            if let id = flagID {
                return snapshot.flags[id]
            }
            return nil
        }()
        guard let f = flag else {
            return OfflineEvalResult(
                flagID: flagID,
                flagKey: flagKey,
                variantID: nil,
                variantKey: nil,
                variantAttachment: nil,
                segmentID: nil,
                evalDebugLog: enableDebug ? EvalDebugLog(msg: "Flag not found in snapshot", segmentDebugLogs: nil) : nil
            )
        }
        if !f.enabled {
            return OfflineEvalResult(
                flagID: f.id,
                flagKey: f.key,
                variantID: nil,
                variantKey: nil,
                variantAttachment: nil,
                segmentID: nil,
                evalDebugLog: enableDebug ? EvalDebugLog(msg: "flagID \(f.id) is not enabled", segmentDebugLogs: nil) : nil
            )
        }
        if f.segments.isEmpty {
            return OfflineEvalResult(
                flagID: f.id,
                flagKey: f.key,
                variantID: nil,
                variantKey: nil,
                variantAttachment: nil,
                segmentID: nil,
                evalDebugLog: enableDebug ? EvalDebugLog(msg: "flagID \(f.id) has no segments", segmentDebugLogs: nil) : nil
            )
        }
        let sortedSegments = f.segments.sorted { $0.rank < $1.rank }
        var debugLogs: [SegmentDebugLog] = []
        var variantID: Int64?
        var variantKey: String?
        var variantAttachment: [String: AnyCodable]?
        var segmentID: Int64?
        for seg in sortedSegments {
            segmentID = seg.id
            let matches = constraintEvaluator.evaluate(constraints: seg.constraints, context: contextStrings)
            if !matches {
                if enableDebug {
                    debugLogs.append(SegmentDebugLog(segmentID: seg.id, msg: "segment_id \(seg.id) did not match constraints"))
                }
                continue
            }
            let (variantIds, percents) = prepareDistribution(seg.distributions)
            let (selectedVariantId, _) = RolloutAlgorithm.rollout(
                entityID: entityID,
                salt: String(f.id),
                rolloutPercent: seg.rolloutPercent,
                variantIds: variantIds,
                percentsAccumulated: percents
            )
            if let vid = selectedVariantId {
                let variant = f.variants.first { Int64(vid) == $0.id }
                variantID = Int64(vid)
                variantKey = variant?.key
                variantAttachment = variant?.attachment
                if enableDebug {
                    debugLogs.append(SegmentDebugLog(segmentID: seg.id, msg: "matched all constraints. rollout yes. variantID: \(vid)"))
                }
                break
            } else {
                if enableDebug {
                    debugLogs.append(SegmentDebugLog(segmentID: seg.id, msg: "matched all constraints. rollout no."))
                }
            }
        }
        return OfflineEvalResult(
            flagID: f.id,
            flagKey: f.key,
            variantID: variantID,
            variantKey: variantKey,
            variantAttachment: variantAttachment,
            segmentID: segmentID,
            evalDebugLog: enableDebug && !debugLogs.isEmpty ? EvalDebugLog(msg: nil, segmentDebugLogs: debugLogs) : nil
        )
    }

    /// Sorted by percent, accumulated 0-1000 scale.
    private func prepareDistribution(_ distributions: [OfflineLocalDistribution]) -> (variantIds: [Int], percentsAccumulated: [Int]) {
        if distributions.isEmpty { return ([], []) }
        let sorted = distributions.sorted { $0.percent < $1.percent }
        var variantIds: [Int] = []
        var accumulated: [Int] = []
        var acc = 0
        for d in sorted {
            acc += d.percent * 10
            variantIds.append(Int(d.variantID))
            accumulated.append(acc)
        }
        return (variantIds, accumulated)
    }
}
