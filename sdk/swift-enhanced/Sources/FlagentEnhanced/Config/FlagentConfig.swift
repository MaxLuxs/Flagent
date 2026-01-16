import Foundation

public struct FlagentConfig {
    public let cacheTtlMs: TimeInterval
    public let enableCache: Bool
    public let enableDebugLogging: Bool
    
    public init(
        cacheTtlMs: TimeInterval = 5 * 60, // 5 minutes
        enableCache: Bool = true,
        enableDebugLogging: Bool = false
    ) {
        self.cacheTtlMs = cacheTtlMs
        self.enableCache = enableCache
        self.enableDebugLogging = enableDebugLogging
    }
}