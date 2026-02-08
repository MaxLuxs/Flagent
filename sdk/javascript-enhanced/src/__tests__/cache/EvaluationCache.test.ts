import { InMemoryEvaluationCache, CacheKey } from '../../cache/EvaluationCache';
import { EvalResult } from '@flagent/client';

describe('InMemoryEvaluationCache', () => {
  let cache: InMemoryEvaluationCache;
  const ttlMs = 100; // 100ms for fast testing

  beforeEach(() => {
    cache = new InMemoryEvaluationCache(ttlMs);
  });

  describe('get', () => {
    it('should return null for non-existent key', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const result = await cache.get(key);
      expect(result).toBeNull();
    });

    it('should return cached result when found', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const evalResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      await cache.put(key, evalResult);
      const result = await cache.get(key);

      expect(result).toEqual(evalResult);
    });

    it('should return null for expired entry', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const evalResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      await cache.put(key, evalResult);

      // Wait for expiration (ttlMs + small buffer)
      await new Promise((resolve) => setTimeout(resolve, ttlMs + 50));

      const result = await cache.get(key);
      expect(result).toBeNull();
    }, 5000);
  });

  describe('put', () => {
    it('should store evaluation result', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const evalResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      await cache.put(key, evalResult);
      const result = await cache.get(key);

      expect(result).toEqual(evalResult);
    });

    it('should overwrite existing entry', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const firstResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };
      const secondResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'variant_a',
      };

      await cache.put(key, firstResult);
      await cache.put(key, secondResult);
      const result = await cache.get(key);

      expect(result).toEqual(secondResult);
    });
  });

  describe('clear', () => {
    it('should remove all cached entries', async () => {
      const key1: CacheKey = { flagKey: 'flag1', entityID: 'user1' };
      const key2: CacheKey = { flagKey: 'flag2', entityID: 'user1' };
      const result1: EvalResult = { flagKey: 'flag1', variantKey: 'control' };
      const result2: EvalResult = { flagKey: 'flag2', variantKey: 'control' };

      await cache.put(key1, result1);
      await cache.put(key2, result2);

      await cache.clear();

      expect(await cache.get(key1)).toBeNull();
      expect(await cache.get(key2)).toBeNull();
    });
  });

  describe('evictExpired', () => {
    it('should remove expired entries', async () => {
      const key1: CacheKey = { flagKey: 'flag1', entityID: 'user1' };
      const key2: CacheKey = { flagKey: 'flag2', entityID: 'user1' };
      const result1: EvalResult = { flagKey: 'flag1', variantKey: 'control' };
      const result2: EvalResult = { flagKey: 'flag2', variantKey: 'control' };

      await cache.put(key1, result1);

      // Wait for expiration
      await new Promise((resolve) => setTimeout(resolve, ttlMs + 50));

      await cache.put(key2, result2);
      await cache.evictExpired();

      expect(await cache.get(key1)).toBeNull();
      expect(await cache.get(key2)).toEqual(result2);
    }, 5000);

    it('should keep non-expired entries', async () => {
      const key: CacheKey = { flagKey: 'test_flag', entityID: 'user1' };
      const evalResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      await cache.put(key, evalResult);
      await cache.evictExpired();

      const result = await cache.get(key);
      expect(result).toEqual(evalResult);
    });
  });
});