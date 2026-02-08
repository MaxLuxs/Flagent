import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:test/test.dart';

void main() {
  group('InMemoryEvaluationCache', () {
    late InMemoryEvaluationCache cache;
    const ttlMs = 100;

    setUp(() {
      cache = InMemoryEvaluationCache(ttlMs);
    });

    group('get', () {
      test('returns null for non-existent key', () async {
        const key = CacheKey(flagKey: 'test_flag', entityID: 'user1');
        final result = await cache.get(key);
        expect(result, isNull);
      });

      test('returns cached result when found', () async {
        const key = CacheKey(flagKey: 'test_flag', entityID: 'user1');
        final evalResult = EvalResult((b) => b
          ..flagKey = 'test_flag'
          ..variantKey = 'control');

        await cache.put(key, evalResult);
        final result = await cache.get(key);

        expect(result?.flagKey, equals('test_flag'));
        expect(result?.variantKey, equals('control'));
      });

      test('returns null for expired entry', () async {
        const key = CacheKey(flagKey: 'test_flag', entityID: 'user1');
        final evalResult = EvalResult((b) => b
          ..flagKey = 'test_flag'
          ..variantKey = 'control');

        await cache.put(key, evalResult);
        await Future<void>.delayed(Duration(milliseconds: ttlMs + 50));

        final result = await cache.get(key);
        expect(result, isNull);
      });
    });

    group('put', () {
      test('stores evaluation result', () async {
        const key = CacheKey(flagKey: 'test_flag', entityID: 'user1');
        final evalResult = EvalResult((b) => b
          ..flagKey = 'test_flag'
          ..variantKey = 'control');

        await cache.put(key, evalResult);
        final result = await cache.get(key);

        expect(result?.variantKey, equals('control'));
      });

      test('overwrites existing entry', () async {
        const key = CacheKey(flagKey: 'test_flag', entityID: 'user1');
        final first = EvalResult((b) => b..variantKey = 'control');
        final second = EvalResult((b) => b..variantKey = 'variant_a');

        await cache.put(key, first);
        await cache.put(key, second);
        final result = await cache.get(key);

        expect(result?.variantKey, equals('variant_a'));
      });
    });

    group('clear', () {
      test('removes all cached entries', () async {
        final key1 = const CacheKey(flagKey: 'flag1', entityID: 'user1');
        final key2 = const CacheKey(flagKey: 'flag2', entityID: 'user1');
        final result1 = EvalResult((b) => b..flagKey = 'flag1');
        final result2 = EvalResult((b) => b..flagKey = 'flag2');

        await cache.put(key1, result1);
        await cache.put(key2, result2);
        await cache.clear();

        expect(await cache.get(key1), isNull);
        expect(await cache.get(key2), isNull);
      });
    });

    group('evictExpired', () {
      test('removes expired entries', () async {
        final key1 = const CacheKey(flagKey: 'flag1', entityID: 'user1');
        final key2 = const CacheKey(flagKey: 'flag2', entityID: 'user1');
        final result1 = EvalResult((b) => b..flagKey = 'flag1');
        final result2 = EvalResult((b) => b..flagKey = 'flag2');

        await cache.put(key1, result1);
        await Future<void>.delayed(Duration(milliseconds: ttlMs + 50));
        await cache.put(key2, result2);
        await cache.evictExpired();

        expect(await cache.get(key1), isNull);
        expect((await cache.get(key2))?.flagKey, equals('flag2'));
      });
    });
  });
}
