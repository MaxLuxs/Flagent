import { FlagentManager } from '../../manager/FlagentManager';
import { Configuration, EvalResult, EvaluationBatchResponse } from '@flagent/client';
import { EvaluationApi } from '@flagent/client';

// Mock EvaluationApi
jest.mock('@flagent/client', () => {
  const actualModule = jest.requireActual('@flagent/client');
  return {
    ...actualModule,
    EvaluationApi: jest.fn(),
  };
});

describe('FlagentManager', () => {
  let manager: FlagentManager;
  let mockConfig: Configuration;
  let mockApi: jest.Mocked<EvaluationApi>;

  beforeEach(() => {
    jest.clearAllMocks();
    mockConfig = new Configuration({
      basePath: 'https://api.example.com/api/v1',
    });
    
    // Create mock instance
    mockApi = {
      postEvaluation: jest.fn(),
      postEvaluationBatch: jest.fn(),
    } as any;
    
    (EvaluationApi as jest.MockedClass<typeof EvaluationApi>).mockImplementation(() => mockApi);
    
    manager = new FlagentManager(mockConfig);
  });

  describe('evaluate', () => {
    it('should evaluate flag and return result', async () => {
      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: expectedResult,
      });

      const result = await manager.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
        entityType: 'user',
      });

      expect(result).toEqual(expectedResult);
      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(1);
    });

    it('should use cache when enabled', async () => {
      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: expectedResult,
      });

      // First call
      const result1 = await manager.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      // Second call should use cache
      const result2 = await manager.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      expect(result1).toEqual(expectedResult);
      expect(result2).toEqual(expectedResult);
      // Should only call API once due to caching
      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(1);
    });

    it('should not use cache when disabled', async () => {
      const managerWithoutCache = new FlagentManager(mockConfig, {
        enableCache: false,
        cacheTtlMs: 5000,
        enableDebugLogging: false,
      });

      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: expectedResult,
      });

      await managerWithoutCache.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      await managerWithoutCache.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      // Should call API twice when cache is disabled
      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(2);
    });
  });

  describe('evaluateBatch', () => {
    it('should evaluate batch and return results', async () => {
      const expectedResponse: EvaluationBatchResponse = {
        evaluationResults: [
          { flagKey: 'flag1', variantKey: 'control' },
          { flagKey: 'flag2', variantKey: 'variant_a' },
        ],
      };

      (mockApi.postEvaluationBatch as jest.Mock).mockResolvedValue({
        data: expectedResponse,
      });

      const results = await manager.evaluateBatch({
        flagKeys: ['flag1', 'flag2'],
        entities: [
          { entityID: 'user1', entityType: 'user' },
        ],
      });

      expect(results).toEqual(expectedResponse.evaluationResults);
      expect(mockApi.postEvaluationBatch).toHaveBeenCalledTimes(1);
    });
  });

  describe('clearCache', () => {
    it('should clear cache', async () => {
      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: expectedResult,
      });

      await manager.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      await manager.clearCache();

      // After clearing, should call API again
      await manager.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(2);
    });
  });

  describe('evictExpired', () => {
    it('should remove expired entries from cache', async () => {
      const managerWithShortTTL = new FlagentManager(mockConfig, {
        enableCache: true,
        cacheTtlMs: 100, // Very short TTL for testing
        enableDebugLogging: false,
      });

      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: expectedResult,
      });

      await managerWithShortTTL.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      // Wait for expiration
      await new Promise((resolve) => setTimeout(resolve, 150));

      await managerWithShortTTL.evictExpired();

      // Should call API again after eviction
      await managerWithShortTTL.evaluate({
        flagKey: 'test_flag',
        entityID: 'user1',
      });

      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(2);
    });
  });

  describe('destroy', () => {
    it('should clear cleanup interval', () => {
      const manager = new FlagentManager(mockConfig);
      manager.destroy();
      // Just verify it doesn't throw
      expect(manager).toBeDefined();
    });
  });
});