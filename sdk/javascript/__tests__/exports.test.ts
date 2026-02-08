/**
 * Tests for module exports.
 */

import {
  Configuration,
  EvaluationApi,
  FlagApi,
  HealthApi,
} from '../index';

describe('Module exports', () => {
  it('should export Configuration', () => {
    expect(Configuration).toBeDefined();
    const config = new Configuration({ basePath: 'http://test/api/v1' });
    expect(config.basePath).toBe('http://test/api/v1');
  });

  it('should export EvaluationApi', () => {
    expect(EvaluationApi).toBeDefined();
    const api = new EvaluationApi(new Configuration());
    expect(api.postEvaluation).toBeDefined();
    expect(api.postEvaluationBatch).toBeDefined();
  });

  it('should export FlagApi', () => {
    expect(FlagApi).toBeDefined();
  });

  it('should export HealthApi', () => {
    expect(HealthApi).toBeDefined();
  });
});
