/// Configuration for Flagent Enhanced SDK.
///
/// Controls caching behavior and debug settings for the enhanced SDK.
class FlagentConfig {
  /// Cache time-to-live in milliseconds. Default: 5 minutes (300000 ms)
  final int cacheTtlMs;

  /// Enable caching of evaluation results. When disabled, all evaluations go
  /// directly to API without caching. Default: true
  final bool enableCache;

  /// Enable debug logging for development and troubleshooting. Default: false
  final bool enableDebugLogging;

  const FlagentConfig({
    this.cacheTtlMs = 5 * 60 * 1000,
    this.enableCache = true,
    this.enableDebugLogging = false,
  });
}

/// Default Flagent configuration.
const defaultFlagentConfig = FlagentConfig();
