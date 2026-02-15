import Foundation
import FlagentClient
import AnyCodable

/// Unified client interface for flag evaluation. Use `Flagent.builder()...build()` to obtain an implementation.
@available(macOS 10.15, iOS 13.0, *)
public protocol FlagentClient: AnyObject {
    /// Evaluate a single flag for an entity.
    func evaluate(
        flagKey: String?,
        flagID: Int64?,
        entityID: String?,
        entityType: String?,
        entityContext: [String: AnyCodable]?,
        enableDebug: Bool
    ) async throws -> EvalResult

    /// Convenience: returns `true` when evaluation returns a variant (flag is on), `false` otherwise.
    func isEnabled(
        flagKey: String,
        entityID: String?,
        entityType: String?,
        entityContext: [String: AnyCodable]?
    ) async throws -> Bool

    /// Evaluate multiple flags for multiple entities in one request.
    func evaluateBatch(
        flagKeys: [String]?,
        flagIDs: [Int64]?,
        entities: [EvaluationEntity],
        enableDebug: Bool
    ) async throws -> [EvalResult]
}
