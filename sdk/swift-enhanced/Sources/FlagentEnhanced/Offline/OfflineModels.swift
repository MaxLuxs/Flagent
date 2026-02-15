import Foundation
import FlagentClient
import AnyCodable

/// Snapshot of flags for local evaluation.
public struct OfflineFlagSnapshot {
    public let flags: [Int64: OfflineLocalFlag]
    public let revision: String?
    public let fetchedAtMs: Int64
    public let ttlMs: Int64

    public init(flags: [Int64: OfflineLocalFlag], revision: String? = nil, fetchedAtMs: Int64, ttlMs: Int64) {
        self.flags = flags
        self.revision = revision
        self.fetchedAtMs = fetchedAtMs
        self.ttlMs = ttlMs
    }

    public func isExpired(nowMs: Int64? = nil) -> Bool {
        let now = nowMs ?? Int64(Date().timeIntervalSince1970 * 1000)
        return now - fetchedAtMs > ttlMs
    }
}

public struct OfflineLocalFlag {
    public let id: Int64
    public let key: String
    public let enabled: Bool
    public let segments: [OfflineLocalSegment]
    public let variants: [OfflineLocalVariant]

    public init(id: Int64, key: String, enabled: Bool, segments: [OfflineLocalSegment], variants: [OfflineLocalVariant]) {
        self.id = id
        self.key = key
        self.enabled = enabled
        self.segments = segments
        self.variants = variants
    }
}

public struct OfflineLocalSegment {
    public let id: Int64
    public let rank: Int
    public let rolloutPercent: Int
    public let constraints: [OfflineLocalConstraint]
    public let distributions: [OfflineLocalDistribution]

    public init(id: Int64, rank: Int, rolloutPercent: Int, constraints: [OfflineLocalConstraint], distributions: [OfflineLocalDistribution]) {
        self.id = id
        self.rank = rank
        self.rolloutPercent = rolloutPercent
        self.constraints = constraints
        self.distributions = distributions
    }
}

public struct OfflineLocalVariant {
    public let id: Int64
    public let key: String
    public let attachment: [String: AnyCodable]?

    public init(id: Int64, key: String, attachment: [String: AnyCodable]? = nil) {
        self.id = id
        self.key = key
        self.attachment = attachment
    }
}

public struct OfflineLocalConstraint {
    public let id: Int64
    public let property: String
    public let operator_: String
    public let value: String

    public init(id: Int64, property: String, operator_: String, value: String) {
        self.id = id
        self.property = property
        self.operator_ = operator_
        self.value = value
    }
}

public struct OfflineLocalDistribution {
    public let id: Int64
    public let variantID: Int64
    public let percent: Int

    public init(id: Int64, variantID: Int64, percent: Int) {
        self.id = id
        self.variantID = variantID
        self.percent = percent
    }
}
