import Foundation
import FlagentClient

public struct CacheKey: Hashable {
    public let flagKey: String?
    public let flagID: Int64?
    public let entityID: String?
    public let entityType: String?
    
    public init(flagKey: String? = nil, flagID: Int64? = nil, entityID: String? = nil, entityType: String? = nil) {
        self.flagKey = flagKey
        self.flagID = flagID
        self.entityID = entityID
        self.entityType = entityType
    }
    
    func toKeyString() -> String {
        return "\(flagID?.description ?? flagKey ?? "")_\(entityID ?? "")_\(entityType ?? "")"
    }
}

private struct CachedEntry {
    let result: EvalResult
    let timestamp: Date
    
    func isExpired(ttl: TimeInterval, currentTime: Date) -> Bool {
        return currentTime.timeIntervalSince(timestamp) > ttl
    }
}

public protocol EvaluationCache {
    func get(key: CacheKey) -> EvalResult?
    func put(key: CacheKey, result: EvalResult)
    func clear()
    func evictExpired()
}

public class InMemoryEvaluationCache: EvaluationCache {
    private var cache: [String: CachedEntry] = [:]
    private let ttl: TimeInterval
    private let queue = DispatchQueue(label: "com.flagent.cache", attributes: .concurrent)
    
    public init(ttl: TimeInterval) {
        self.ttl = ttl
    }
    
    public func get(key: CacheKey) -> EvalResult? {
        return queue.sync {
            guard let entry = cache[key.toKeyString()],
                  !entry.isExpired(ttl: ttl, currentTime: Date()) else {
                cache.removeValue(forKey: key.toKeyString())
                return nil
            }
            return entry.result
        }
    }
    
    public func put(key: CacheKey, result: EvalResult) {
        queue.async(flags: .barrier) {
            self.cache[key.toKeyString()] = CachedEntry(result: result, timestamp: Date())
        }
    }
    
    public func clear() {
        queue.async(flags: .barrier) {
            self.cache.removeAll()
        }
    }
    
    public func evictExpired() {
        queue.async(flags: .barrier) {
            let currentTime = Date()
            let keysToRemove = self.cache.filter { $0.value.isExpired(ttl: self.ttl, currentTime: currentTime) }.map { $0.key }
            keysToRemove.forEach { self.cache.removeValue(forKey: $0) }
        }
    }
}