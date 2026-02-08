/**
 * Tests for EvaluationApi (generated OpenAPI client).
 */

import type { AxiosInstance } from 'axios';
import { Configuration } from '../configuration';
import { EvaluationApi } from '../api/evaluation-api';
import type { EvalContext, EvalResult } from '../models';

describe('EvaluationApi', () => {
  let mockAxios: jest.Mocked<AxiosInstance>;
  let api: EvaluationApi;

  beforeEach(() => {
    mockAxios = {
      request: jest.fn(),
      defaults: { baseURL: '' },
    } as unknown as jest.Mocked<AxiosInstance>;

    const config = new Configuration({
      basePath: 'https://api.example.com/api/v1',
    });

    api = new EvaluationApi(config, 'https://api.example.com/api/v1', mockAxios);
  });

  describe('postEvaluation', () => {
    it('should evaluate flag and return result', async () => {
      const context: EvalContext = {
        flagKey: 'test_flag',
        entityID: 'user1',
        entityType: 'user',
      };

      const expectedResult: EvalResult = {
        flagKey: 'test_flag',
        variantKey: 'control',
      };

      (mockAxios.request as jest.Mock).mockResolvedValue({
        data: expectedResult,
        status: 200,
      });

      const result = await api.postEvaluation(context);

      expect(result.data).toEqual(expectedResult);
      expect(mockAxios.request).toHaveBeenCalledTimes(1);
    });

    it('should pass correct request options', async () => {
      const context: EvalContext = { flagKey: 'f1' };
      (mockAxios.request as jest.Mock).mockResolvedValue({ data: {}, status: 200 });

      await api.postEvaluation(context);

      const call = (mockAxios.request as jest.Mock).mock.calls[0][0];
      expect(call.method?.toLowerCase()).toBe('post');
      expect(call.url).toContain('/evaluation');
      expect(call.data).toBeDefined();
    });
  });

  describe('postEvaluationBatch', () => {
    it('should evaluate batch and return results', async () => {
      const request = {
        entities: [{ entityID: 'user1' }],
        flagKeys: ['f1', 'f2'],
      };

      const expectedResponse = {
        evaluationResults: [
          { flagKey: 'f1', variantKey: 'control' },
          { flagKey: 'f2', variantKey: 'variant_a' },
        ],
      };

      (mockAxios.request as jest.Mock).mockResolvedValue({
        data: expectedResponse,
        status: 200,
      });

      const result = await api.postEvaluationBatch(request);

      expect(result.data.evaluationResults).toHaveLength(2);
      expect(result.data.evaluationResults[0].flagKey).toBe('f1');
    });
  });
});
