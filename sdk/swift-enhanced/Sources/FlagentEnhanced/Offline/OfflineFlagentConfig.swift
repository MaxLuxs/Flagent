import Foundation

/// Configuration for offline (client-side) evaluation.
public struct OfflineFlagentConfig {
    /// Snapshot TTL in milliseconds; snapshot is considered stale after this period.
    public let snapshotTtlMs: Int64
    /// Whether to auto-refresh snapshot in background (reserved for future use).
    public let autoRefresh: Bool
    /// Refresh interval in ms when autoRefresh is true (reserved).
    public let refreshIntervalMs: Int64

    public init(
        snapshotTtlMs: Int64 = 300_000,
        autoRefresh: Bool = false,
        refreshIntervalMs: Int64 = 60_000
    ) {
        self.snapshotTtlMs = snapshotTtlMs
        self.autoRefresh = autoRefresh
        self.refreshIntervalMs = refreshIntervalMs
    }
}
