import Foundation
import FlagentClient
import AnyCodable

/// Adapts `FlagentManager` to the unified `FlagentClient` protocol and adds `isEnabled`.
@available(macOS 10.15, iOS 13.0, *)
public final class FlagentManagerAdapter: FlagentClient {
    private let evaluator: FlagentEvaluating

    public init(manager: FlagentManager) {
        self.evaluator = manager
    }

    init(evaluator: FlagentEvaluating) {
        self.evaluator = evaluator
    }

    public func evaluate(
        flagKey: String? = nil,
        flagID: Int64? = nil,
        entityID: String? = nil,
        entityType: String? = nil,
        entityContext: [String: AnyCodable]? = nil,
        enableDebug: Bool = false
    ) async throws -> EvalResult {
        try await evaluator.evaluate(
            flagKey: flagKey,
            flagID: flagID,
            entityID: entityID,
            entityType: entityType,
            entityContext: entityContext,
            enableDebug: enableDebug
        )
    }

    public func isEnabled(
        flagKey: String,
        entityID: String? = nil,
        entityType: String? = nil,
        entityContext: [String: AnyCodable]? = nil
    ) async throws -> Bool {
        let result = try await evaluator.evaluate(
            flagKey: flagKey,
            flagID: nil,
            entityID: entityID,
            entityType: entityType,
            entityContext: entityContext,
            enableDebug: false
        )
        return result.variantKey != nil
    }

    public func evaluateBatch(
        flagKeys: [String]? = nil,
        flagIDs: [Int64]? = nil,
        entities: [EvaluationEntity],
        enableDebug: Bool = false
    ) async throws -> [EvalResult] {
        try await evaluator.evaluateBatch(
            flagKeys: flagKeys,
            flagIDs: flagIDs,
            entities: entities,
            enableDebug: enableDebug
        )
    }
}
