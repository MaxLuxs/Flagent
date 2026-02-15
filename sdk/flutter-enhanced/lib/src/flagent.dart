import 'package:dio/dio.dart';

import 'config/flagent_config.dart';
import 'manager/flagent_manager.dart';

/// Options for creating a Flagent client via [Flagent.create].
///
/// Aligns with the unified SDK entry point: one place to configure
/// baseUrl, cache, and optional Dio instance.
class FlagentOptions {
  /// Base URL of the Flagent API (e.g. `https://api.example.com/api/v1`).
  final String baseUrl;

  /// Optional Dio instance. If not provided, a default Dio is used for [baseUrl].
  final Dio? dio;

  /// Configuration for cache and debug. Defaults to [defaultFlagentConfig].
  final FlagentConfig config;

  const FlagentOptions({
    required this.baseUrl,
    this.dio,
    this.config = defaultFlagentConfig,
  });
}

/// Unified entry point for the Flutter/Dart Flagent SDK.
///
/// Use [Flagent.create] (recommended) or [Flagent.managed] to obtain
/// a [FlagentManager] and then call [FlagentManager.evaluate],
/// [FlagentManager.isEnabled], and [FlagentManager.evaluateBatch].
abstract class Flagent {
  Flagent._();

  /// Creates a Flagent client with the given options.
  ///
  /// This is the recommended way to obtain a client. The returned
  /// [FlagentManager] supports [evaluate], [isEnabled], and [evaluateBatch].
  ///
  /// Example:
  /// ```dart
  /// final client = Flagent.create(
  ///   baseUrl: 'https://api.example.com/api/v1',
  ///   config: FlagentConfig(enableCache: true, cacheTtlMs: 300000),
  /// );
  /// final on = await client.isEnabled(flagKey: 'new_ui', entityID: 'user1');
  /// ```
  static FlagentManager create({
    required String baseUrl,
    Dio? dio,
    FlagentConfig config = defaultFlagentConfig,
  }) {
    return FlagentManager(baseUrl, dio: dio, config: config);
  }

  /// Creates a Flagent client from an options object.
  ///
  /// Same as [create] but accepts [FlagentOptions] for consistency
  /// with other SDKs that use an options bag.
  static FlagentManager fromOptions(FlagentOptions options) {
    return FlagentManager(
      options.baseUrl,
      dio: options.dio,
      config: options.config,
    );
  }

  /// Creates a Flagent client with explicit base URL and config.
  ///
  /// Use when you already have a [FlagentConfig] instance.
  /// For inline configuration, prefer [create].
  ///
  /// Example:
  /// ```dart
  /// final config = FlagentConfig(enableCache: false);
  /// final client = Flagent.managed('https://api.example.com/api/v1', config);
  /// ```
  static FlagentManager managed(
    String baseUrl,
    FlagentConfig config, {
    Dio? dio,
  }) {
    return FlagentManager(baseUrl, dio: dio, config: config);
  }
}
