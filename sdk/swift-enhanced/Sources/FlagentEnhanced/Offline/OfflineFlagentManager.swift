import Foundation
import FlagentClient
import AnyCodable
import Combine

/// Offline-first manager: fetches snapshot (Export API or FlagAPI fallback) and evaluates locally.
@available(macOS 10.15, iOS 13.0, *)
public final class OfflineFlagentManager {
    private let config: OfflineFlagentConfig
    private let fetcher: SnapshotFetcher
    private let evaluator = LocalEvaluator()
    private let queue = DispatchQueue(label: "com.flagent.offline")
    private var currentSnapshot: OfflineFlagSnapshot?
    private var isBootstrapped = false

    public init(config: OfflineFlagentConfig = OfflineFlagentConfig()) {
        self.config = config
        self.fetcher = SnapshotFetcher(ttlMs: config.snapshotTtlMs)
    }

    /// Load snapshot from network (or cache in future). Call once before evaluate; evaluate will bootstrap lazily if needed.
    public func bootstrap(forceRefresh: Bool = false) async throws {
        let shouldSkip = queue.sync { isBootstrapped && !forceRefresh }
        if shouldSkip { return }
        let snapshot = try await fetcher.fetchSnapshot()
        queue.sync {
            currentSnapshot = snapshot
            isBootstrapped = true
        }
    }

    private func getSnapshot() throws -> OfflineFlagSnapshot {
        let s = queue.sync { currentSnapshot }
        guard let s = s else { throw OfflineFlagentError.notBootstrapped }
        return s
    }

    public func evaluate(
        flagKey: String? = nil,
        flagID: Int64? = nil,
        entityID: String? = nil,
        entityType: String? = nil,
        entityContext: [String: AnyCodable]? = nil,
        enableDebug: Bool = false
    ) async throws -> EvalResult {
        if !isBootstrapped {
            try await bootstrap()
        }
        let snapshot = try getSnapshot()
        let eid = entityID ?? ""
        let result = evaluator.evaluate(
            flagKey: flagKey,
            flagID: flagID,
            entityID: eid,
            entityType: entityType,
            entityContext: entityContext,
            snapshot: snapshot,
            enableDebug: enableDebug
        )
        return result.toEvalResult()
    }

    public func evaluateBatch(
        flagKeys: [String]? = nil,
        flagIDs: [Int64]? = nil,
        entities: [EvaluationEntity],
        enableDebug: Bool = false
    ) async throws -> [EvalResult] {
        if !isBootstrapped {
            try await bootstrap()
        }
        let snapshot = try getSnapshot()
        var results: [EvalResult] = []
        if let keys = flagKeys, !keys.isEmpty {
            for entity in entities {
                for key in keys {
                    let result = evaluator.evaluate(
                        flagKey: key,
                        flagID: nil,
                        entityID: entity.entityID ?? "",
                        entityType: entity.entityType,
                        entityContext: entity.entityContext,
                        snapshot: snapshot,
                        enableDebug: enableDebug
                    )
                    results.append(result.toEvalResult())
                }
            }
        } else if let ids = flagIDs, !ids.isEmpty {
            for entity in entities {
                for id in ids {
                    let result = evaluator.evaluate(
                        flagKey: nil,
                        flagID: id,
                        entityID: entity.entityID ?? "",
                        entityType: entity.entityType,
                        entityContext: entity.entityContext,
                        snapshot: snapshot,
                        enableDebug: enableDebug
                    )
                    results.append(result.toEvalResult())
                }
            }
        } else {
            for entity in entities {
                let result = evaluator.evaluate(
                    flagKey: nil,
                    flagID: nil,
                    entityID: entity.entityID ?? "",
                    entityType: entity.entityType,
                    entityContext: entity.entityContext,
                    snapshot: snapshot,
                    enableDebug: enableDebug
                )
                results.append(result.toEvalResult())
            }
        }
        return results
    }
}

public enum OfflineFlagentError: Error, LocalizedError {
    case notBootstrapped

    public var errorDescription: String? {
        switch self {
        case .notBootstrapped: return "OfflineFlagentManager not bootstrapped. Call bootstrap() first."
        }
    }
}
