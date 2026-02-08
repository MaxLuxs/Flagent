import 'dart:async';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:dio/dio.dart';
import 'package:flagent_client/flagent_client.dart';

import '../cache/evaluation_cache.dart';
import '../config/flagent_config.dart';

BuiltMap<String, JsonObject?>? _toEntityContext(Map<String, dynamic>? ctx) {
  if (ctx == null || ctx.isEmpty) return null;
  final builder = MapBuilder<String, JsonObject?>();
  for (final e in ctx.entries) {
    if (e.value != null) {
      builder[e.key] = JsonObject(_toJsonValue(e.value));
    }
  }
  return builder.build();
}

Object _toJsonValue(dynamic v) {
  if (v is String || v is num || v is bool) return v;
  if (v is List) return v.map(_toJsonValue).toList();
  if (v is Map) return v.map((k, v) => MapEntry(k.toString(), _toJsonValue(v)));
  return v.toString();
}

/// Build EvaluationEntity from simple parameters.
EvaluationEntity buildEvaluationEntity({
  required String entityID,
  String? entityType,
  Map<String, dynamic>? entityContext,
}) {
  return EvaluationEntity((b) {
    b
      ..entityID = entityID
      ..entityType = entityType;
    final ctx = _toEntityContext(entityContext);
    if (ctx != null) b.entityContext.replace(ctx);
  });
}

/// Enhanced Flagent Manager with caching and convenient API.
///
/// Provides high-level API for evaluating feature flags with automatic caching.
class FlagentManager {
  final EvaluationApi _evaluationApi;
  final FlagentConfig _config;
  final EvaluationCache? _cache;
  Timer? _cleanupTimer;

  FlagentManager(
    String basePath, {
    Dio? dio,
    FlagentConfig config = defaultFlagentConfig,
  })  : _config = config,
        _evaluationApi = FlagentClient(
          basePathOverride: basePath,
          dio: dio,
        ).getEvaluationApi(),
        _cache = config.enableCache
            ? InMemoryEvaluationCache(config.cacheTtlMs)
            : null {
    if (_cache != null) {
      _cleanupTimer = Timer.periodic(
        Duration(milliseconds: config.cacheTtlMs),
        (_) => _cache!.evictExpired(),
      );
    }
  }

  /// Evaluate a flag for a given entity context.
  Future<EvalResult> evaluate({
    String? flagKey,
    int? flagID,
    String? entityID,
    String? entityType,
    Map<String, dynamic>? entityContext,
    bool enableDebug = false,
  }) async {
    final cacheKey = CacheKey(
      flagKey: flagKey,
      flagID: flagID,
      entityID: entityID,
      entityType: entityType,
    );

    if (_cache != null) {
      final cached = await _cache!.get(cacheKey);
      if (cached != null) return cached;
    }

    final evalContext = EvalContext((b) {
      b
        ..flagKey = flagKey
        ..flagID = flagID
        ..entityID = entityID
        ..entityType = entityType
        ..enableDebug = enableDebug;
      final ctx = _toEntityContext(entityContext);
      if (ctx != null) b.entityContext.replace(ctx);
    });

    final response = await _evaluationApi.postEvaluation(
      evalContext: evalContext,
    );

    if (response.data == null) {
      throw Exception('Evaluation failed: ${response.statusCode}');
    }

    final result = response.data!;
    if (_cache != null) {
      await _cache!.put(cacheKey, result);
    }
    return result;
  }

  /// Batch evaluate flags for multiple entities.
  Future<List<EvalResult>> evaluateBatch({
    List<String>? flagKeys,
    List<int>? flagIDs,
    required List<EvaluationEntity> entities,
    bool enableDebug = false,
  }) async {
    final request = EvaluationBatchRequest((b) => b
      ..entities = ListBuilder(entities)
      ..flagKeys = flagKeys != null ? ListBuilder(flagKeys) : null
      ..flagIDs = flagIDs != null ? ListBuilder(flagIDs) : null
      ..enableDebug = enableDebug);

    final response = await _evaluationApi.postEvaluationBatch(
      evaluationBatchRequest: request,
    );

    if (response.data == null) {
      throw Exception('Batch evaluation failed: ${response.statusCode}');
    }

    return response.data!.evaluationResults.toList();
  }

  /// Clear all cached evaluation results.
  Future<void> clearCache() async {
    await _cache?.clear();
  }

  /// Evict expired entries from cache.
  Future<void> evictExpired() async {
    await _cache?.evictExpired();
  }

  /// Destroy the manager and clean up resources.
  void destroy() {
    _cleanupTimer?.cancel();
    _cleanupTimer = null;
  }
}
