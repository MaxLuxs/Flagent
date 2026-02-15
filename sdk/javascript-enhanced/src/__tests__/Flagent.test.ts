import { Flagent, FlagentClient, FlagentOptions, DISABLED_VARIANT_KEY } from '../Flagent';
import { EvalResult, EvaluationBatchResponse } from '@flagent/client';
import { EvaluationApi } from '@flagent/client';

jest.mock('@flagent/client', () => {
  const actual = jest.requireActual('@flagent/client');
  return {
    ...actual,
    EvaluationApi: jest.fn(),
  };
});

describe('Flagent', () => {
  let client: FlagentClient;
  let mockApi: jest.Mocked<Pick<EvaluationApi, 'postEvaluation' | 'postEvaluationBatch'>>;

  beforeEach(() => {
    jest.clearAllMocks();
    mockApi = {
      postEvaluation: jest.fn(),
      postEvaluationBatch: jest.fn(),
    } as any;
    (EvaluationApi as jest.MockedClass<typeof EvaluationApi>).mockImplementation(() => mockApi as any);

    client = Flagent.create({
      basePath: 'https://api.example.com/api/v1',
      enableCache: true,
      cacheTtlMs: 60_000,
    });
  });

  afterEach(() => {
    client.destroy();
  });

  describe('Flagent.create', () => {
    it('should create client with minimal options (basePath only)', () => {
      const c = Flagent.create({ basePath: 'https://api.test/v1' });
      expect(c.evaluate).toBeDefined();
      expect(c.isEnabled).toBeDefined();
      expect(c.evaluateBatch).toBeDefined();
      expect(c.clearCache).toBeDefined();
      expect(c.destroy).toBeDefined();
      c.destroy();
    });

    it('should throw when basePath is missing', () => {
      expect(() => Flagent.create({} as FlagentOptions)).toThrow('basePath is required');
      expect(() => Flagent.create({ basePath: '' })).toThrow('basePath is required');
    });

    it('should accept auth and cache options', () => {
      const c = Flagent.create({
        basePath: 'https://api.test/v1',
        accessToken: 'token',
        enableCache: false,
        cacheTtlMs: 10_000,
      });
      expect(c).toBeDefined();
      c.destroy();
    });
  });

  describe('evaluate', () => {
    it('should delegate to manager and return EvalResult', async () => {
      const expected: EvalResult = { flagKey: 'f1', variantKey: 'variant_a' };
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({ data: expected });

      const result = await client.evaluate({
        flagKey: 'f1',
        entityID: 'user1',
        entityType: 'user',
      });

      expect(result).toEqual(expected);
      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(1);
    });
  });

  describe('isEnabled', () => {
    it('should return true when variantKey is not control and not empty', async () => {
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: { flagKey: 'f1', variantKey: 'enabled' },
      });
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(true);
    });

    it('should return true for any non-control variant', async () => {
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: { flagKey: 'f1', variantKey: 'variant_b' },
      });
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(true);
    });

    it('should return false when variantKey is control', async () => {
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: { flagKey: 'f1', variantKey: DISABLED_VARIANT_KEY },
      });
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(false);
    });

    it('should return false when variantKey is null/undefined', async () => {
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: { flagKey: 'f1', variantKey: null },
      });
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(false);
    });

    it('should return false when variantKey is empty string', async () => {
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({
        data: { flagKey: 'f1', variantKey: '' },
      });
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(false);
    });

    it('should return false on evaluate error', async () => {
      (mockApi.postEvaluation as jest.Mock).mockRejectedValue(new Error('Network error'));
      const on = await client.isEnabled({ flagKey: 'f1', entityID: 'user1' });
      expect(on).toBe(false);
    });
  });

  describe('evaluateBatch', () => {
    it('should delegate to manager and return EvalResult[]', async () => {
      const expectedResponse: EvaluationBatchResponse = {
        evaluationResults: [
          { flagKey: 'f1', variantKey: 'control' },
          { flagKey: 'f2', variantKey: 'on' },
        ],
      };
      (mockApi.postEvaluationBatch as jest.Mock).mockResolvedValue({
        data: expectedResponse,
      });

      const results = await client.evaluateBatch({
        flagKeys: ['f1', 'f2'],
        entities: [{ entityID: 'user1', entityType: 'user' }],
      });

      expect(results).toEqual(expectedResponse.evaluationResults);
      expect(mockApi.postEvaluationBatch).toHaveBeenCalledTimes(1);
    });
  });

  describe('clearCache', () => {
    it('should clear cache so next evaluate hits API again', async () => {
      const result: EvalResult = { flagKey: 'f1', variantKey: 'control' };
      (mockApi.postEvaluation as jest.Mock).mockResolvedValue({ data: result });

      await client.evaluate({ flagKey: 'f1', entityID: 'user1' });
      await client.clearCache();
      await client.evaluate({ flagKey: 'f1', entityID: 'user1' });

      expect(mockApi.postEvaluation).toHaveBeenCalledTimes(2);
    });
  });

  describe('destroy', () => {
    it('should not throw and should stop cleanup interval', () => {
      expect(() => client.destroy()).not.toThrow();
    });
  });

  describe('DISABLED_VARIANT_KEY', () => {
    it('should be "control"', () => {
      expect(DISABLED_VARIANT_KEY).toBe('control');
    });
  });
});
