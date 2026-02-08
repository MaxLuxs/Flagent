import 'package:flagent_client/flagent_client.dart';

/// Cache key for evaluation results.
///
/// Used to uniquely identify cached evaluation results based on flag and entity.
class CacheKey {
  final String? flagKey;
  final int? flagID;
  final String? entityID;
  final String? entityType;

  const CacheKey({
    this.flagKey,
    this.flagID,
    this.entityID,
    this.entityType,
  });

  String toKeyString() =>
      '${flagID ?? flagKey ?? ''}_${entityID ?? ''}_${entityType ?? ''}';
}

/// Cache interface for evaluation results.
abstract class EvaluationCache {
  Future<EvalResult?> get(CacheKey key);
  Future<void> put(CacheKey key, EvalResult result);
  Future<void> clear();
  Future<void> evictExpired();
}

/// In-memory cache implementation with TTL-based expiration.
class InMemoryEvaluationCache implements EvaluationCache {
  final Map<String, _CachedEntry> _cache = {};
  final int ttlMs;

  InMemoryEvaluationCache(this.ttlMs);

  @override
  Future<EvalResult?> get(CacheKey key) async {
    final entry = _cache[key.toKeyString()];
    if (entry == null) return null;
    if (DateTime.now().millisecondsSinceEpoch - entry.timestamp > ttlMs) {
      _cache.remove(key.toKeyString());
      return null;
    }
    return entry.result;
  }

  @override
  Future<void> put(CacheKey key, EvalResult result) async {
    _cache[key.toKeyString()] = _CachedEntry(
      result: result,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );
  }

  @override
  Future<void> clear() async {
    _cache.clear();
  }

  @override
  Future<void> evictExpired() async {
    final now = DateTime.now().millisecondsSinceEpoch;
    _cache.removeWhere((_, entry) => now - entry.timestamp > ttlMs);
  }
}

class _CachedEntry {
  final EvalResult result;
  final int timestamp;

  _CachedEntry({required this.result, required this.timestamp});
}
