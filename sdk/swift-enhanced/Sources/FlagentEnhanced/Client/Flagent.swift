import Foundation
import FlagentClient

/// Unified entry point: use `Flagent.builder()...build()` to create a client, then call `evaluate` / `isEnabled` / `evaluateBatch`.
@available(macOS 10.15, iOS 13.0, *)
public enum Flagent {

    /// Creates a new builder for configuring and building a `FlagentClient`.
    public static func builder() -> Builder {
        Builder()
    }

    public final class Builder {
        private var baseURL: String?
        private var customHeaders: [String: String] = [:]
        private var credential: URLCredential?
        private var enableCache: Bool = true
        private var cacheTtlMs: TimeInterval = 5 * 60
        private var mode: FlagentMode = .server

        init() {}

        /// Base URL of the Flagent API (e.g. `https://api.example.com/api/v1`). Required.
        public func baseURL(_ url: String) -> Builder {
            baseURL = url.hasSuffix("/") ? String(url.dropLast()) : url
            return self
        }

        /// Add a custom header (e.g. API key or Bearer token).
        public func header(name: String, value: String) -> Builder {
            if !value.isEmpty {
                customHeaders[name] = value
            }
            return self
        }

        /// Set Bearer token for authorization.
        public func bearerToken(_ token: String) -> Builder {
            if !token.isEmpty {
                customHeaders["Authorization"] = "Bearer \(token)"
            }
            return self
        }

        /// HTTP basic auth credential (optional).
        public func credential(_ credential: URLCredential) -> Builder {
            self.credential = credential
            return self
        }

        /// Enable in-memory cache and TTL in seconds (default: cache enabled, 5 minutes).
        public func cache(enable: Bool = true, ttlMs: TimeInterval? = nil) -> Builder {
            enableCache = enable
            if let ttl = ttlMs, ttl > 0 {
                cacheTtlMs = ttl / 1000.0
            }
            return self
        }

        /// Client mode: `.server` (default) or `.offline` (reserved).
        public func mode(_ mode: FlagentMode) -> Builder {
            self.mode = mode
            return self
        }

        /// Build the client. Configures the global FlagentClient API (basePath, headers, credential) and returns a `FlagentClient`.
        /// - Note: Only one base URL is effective at a time; building a second client overwrites the global config.
        /// - Note: For `.offline` mode, call `bootstrap()` on the returned client (or first evaluate will bootstrap lazily).
        public func build() -> FlagentClient {
            let url = baseURL ?? FlagentClientAPI.basePath
            FlagentClientAPI.basePath = url.hasSuffix("/") ? url : url
            FlagentClientAPI.customHeaders = customHeaders
            FlagentClientAPI.credential = credential

            switch mode {
            case .offline:
                let offlineConfig = OfflineFlagentConfig(snapshotTtlMs: Int64(cacheTtlMs * 1000))
                let offlineManager = OfflineFlagentManager(config: offlineConfig)
                return FlagentManagerAdapter(evaluator: offlineManager)
            case .server:
                let config = FlagentConfig(
                    cacheTtlMs: cacheTtlMs,
                    enableCache: enableCache,
                    enableDebugLogging: false
                )
                let manager = FlagentManager(config: config)
                return FlagentManagerAdapter(manager: manager)
            }
        }
    }
}
