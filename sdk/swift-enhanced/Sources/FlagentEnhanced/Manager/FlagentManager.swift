import Foundation
import Combine
import FlagentClient
import AnyCodable

// Helper extension to convert Combine publishers to async/await
extension AnyPublisher {
    func async() async throws -> Output {
        return try await withCheckedThrowingContinuation { continuation in
            var cancellable: AnyCancellable?
            cancellable = self
                .sink(
                    receiveCompletion: { completion in
                        switch completion {
                        case .finished:
                            break
                        case .failure(let error):
                            continuation.resume(throwing: error)
                        }
                        cancellable?.cancel()
                    },
                    receiveValue: { value in
                        continuation.resume(returning: value)
                        cancellable?.cancel()
                    }
                )
        }
    }
}

@available(macOS 10.15, iOS 13.0, *)
public class FlagentManager {
    private let config: FlagentConfig
    private var cache: InMemoryEvaluationCache?
    private var cancellables = Set<AnyCancellable>()
    
    public init(config: FlagentConfig = FlagentConfig()) {
        self.config = config
        if config.enableCache {
            self.cache = InMemoryEvaluationCache(ttl: config.cacheTtlMs)
        }
        
        // Periodic cache cleanup
        if cache != nil {
            Timer.publish(every: config.cacheTtlMs, on: .main, in: .common)
                .autoconnect()
                .sink { [weak self] _ in
                    self?.cache?.evictExpired()
                }
                .store(in: &cancellables)
        }
    }
    
    public func evaluate(
        flagKey: String? = nil,
        flagID: Int64? = nil,
        entityID: String? = nil,
        entityType: String? = nil,
        entityContext: [String: AnyCodable]? = nil,
        enableDebug: Bool = false
    ) async throws -> EvalResult {
        let cacheKey = CacheKey(flagKey: flagKey, flagID: flagID, entityID: entityID, entityType: entityType)
        
        // Try cache first
        if let cached = cache?.get(key: cacheKey) {
            return cached
        }
        
        // Evaluate via API (argument order matches FlagentClient EvalContext init)
        let evalContext = EvalContext(
            entityID: entityID,
            entityType: entityType,
            entityContext: entityContext,
            enableDebug: enableDebug,
            flagID: flagID,
            flagKey: flagKey
        )
        
        let publisher = EvaluationAPI.postEvaluation(evalContext: evalContext)
        let result = try await publisher.async()
        
        // Cache result
        cache?.put(key: cacheKey, result: result)
        
        return result
    }
    
    public func evaluateBatch(
        flagKeys: [String]? = nil,
        flagIDs: [Int64]? = nil,
        entities: [EvaluationEntity],
        enableDebug: Bool = false
    ) async throws -> [EvalResult] {
        let request = EvaluationBatchRequest(
            entities: entities,
            enableDebug: enableDebug,
            flagIDs: flagIDs.map { $0.map { Int($0) } },
            flagKeys: flagKeys
        )
        
        let publisher = EvaluationAPI.postEvaluationBatch(evaluationBatchRequest: request)
        let response = try await publisher.async()
        
        return response.evaluationResults
    }
    
    public func clearCache() {
        cache?.clear()
    }
    
    public func evictExpired() {
        cache?.evictExpired()
    }
}