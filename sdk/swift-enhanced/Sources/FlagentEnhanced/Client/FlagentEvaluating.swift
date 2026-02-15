import Foundation
import FlagentClient
import AnyCodable

/// Internal protocol for evaluation; allows injecting a mock in tests. `FlagentManager` conforms.
@available(macOS 10.15, iOS 13.0, *)
protocol FlagentEvaluating: AnyObject {
    func evaluate(
        flagKey: String?,
        flagID: Int64?,
        entityID: String?,
        entityType: String?,
        entityContext: [String: AnyCodable]?,
        enableDebug: Bool
    ) async throws -> EvalResult

    func evaluateBatch(
        flagKeys: [String]?,
        flagIDs: [Int64]?,
        entities: [EvaluationEntity],
        enableDebug: Bool
    ) async throws -> [EvalResult]
}

@available(macOS 10.15, iOS 13.0, *)
extension FlagentManager: FlagentEvaluating {}

@available(macOS 10.15, iOS 13.0, *)
extension OfflineFlagentManager: FlagentEvaluating {}
