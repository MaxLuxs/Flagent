# Changelog

All notable changes to the Flagent Go Enhanced SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-27

### Added
- Initial release of Flagent Go Enhanced SDK
- FlagentManager with high-level API
- In-memory cache implementation with TTL
- Cache interface for custom implementations
- Configuration system with builder pattern
- Auto-refresh support for background cache updates
- Convenient evaluation methods (Evaluate, IsEnabled, GetVariant)
- Cache management (Clear, EvictExpired)
- **Client-Side Evaluation** - Offline-first local evaluation ⭐ **NEW**
  - LocalEvaluator for local flag evaluation
  - OfflineManager for offline-first operation
  - Snapshot storage (in-memory + file persistence)
  - Snapshot fetcher with auto-refresh
  - Bootstrap mechanism for initialization
- Comprehensive test coverage (11 test suites)
- Examples and documentation

### Features
- **Client-Side Evaluation** - Sub-millisecond local evaluation without API calls
- **Offline Support** - Works without network connection
- **Real-Time Updates (SSE)** - Instant flag updates via Server-Sent Events ⭐ **NEW**
  - SSEClient for SSE connection management
  - Auto-reconnection with exponential backoff
  - Event filtering by flag keys/IDs
  - Integration with OfflineManager for auto-refresh
- **Caching** - In-memory cache with configurable TTL
- **Convenient API** - High-level API for flag evaluation
- **Auto-Refresh** - Optional background snapshot updates
- **Batch Evaluation** - Support for batch evaluation
- **Persistent Storage** - File-based caching for offline use
- **Cache Management** - Clear cache, evict expired entries
- **Thread-Safe** - All operations are thread-safe
- **Performance** - 50-200x faster with caching/client-side eval (< 1ms vs 50-200ms)

[1.0.0]: https://github.com/MaxLuxs/Flagent/releases/tag/go-enhanced-sdk-v1.0.0
