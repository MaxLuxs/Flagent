import {
  EvaluationApi,
  Configuration,
  EvalResult,
  EvaluationBatchRequest,
  EvaluationEntity,
  EvalContext,
} from '@flagent/client';
import { FlagentConfig, defaultFlagentConfig } from '../config/FlagentConfig';
import { EvaluationCache, InMemoryEvaluationCache, CacheKey } from '../cache/EvaluationCache';

/**
 * Enhanced Flagent Manager with caching and convenient API.
 * 
 * Provides high-level API for evaluating feature flags with automatic caching.
 * Results are cached with configurable TTL to reduce API calls and improve performance.
 * 
 * @example
 * ```typescript
 * const manager = new FlagentManager(configuration, {
 *   cacheTtlMs: 60000,
 *   enableCache: true
 * });
 * const result = await manager.evaluate({
 *   flagKey: 'new_feature',
 *   entityID: 'user123'
 * });
 * ```
 */
export class FlagentManager {
  private readonly evaluationApi: EvaluationApi;
  private readonly config: FlagentConfig;
  private readonly cache: EvaluationCache | null;
  private cleanupInterval?: NodeJS.Timeout;

  /**
   * Creates a new FlagentManager instance.
   * 
   * @param configuration The base API configuration
   * @param config Configuration for caching and behavior
   */
  constructor(
    configuration: Configuration,
    config: FlagentConfig = defaultFlagentConfig
  ) {
    this.evaluationApi = new EvaluationApi(configuration);
    this.config = config;
    this.cache = config.enableCache ? new InMemoryEvaluationCache(config.cacheTtlMs) : null;

    // Periodic cache cleanup
    if (this.cache) {
      this.cleanupInterval = setInterval(() => {
        this.cache?.evictExpired();
      }, this.config.cacheTtlMs);
    }
  }

  /**
   * Evaluate a flag for a given entity context.
   * 
   * Results are cached with TTL configured in FlagentConfig. Cached results are returned
   * if available and not expired.
   * 
   * @param options Evaluation options
   * @param options.flagKey The flag key (optional if flagID is provided)
   * @param options.flagID The flag ID (optional if flagKey is provided)
   * @param options.entityID The entity ID to evaluate for
   * @param options.entityType The entity type (e.g., "user", "session")
   * @param options.entityContext Additional context for evaluation (e.g., region, tier)
   * @param options.enableDebug Enable debug mode for detailed evaluation logs
   * @returns Evaluation result with assigned variant
   * @throws Error if API call fails or flag is not found
   */
  async evaluate(
    options: {
      flagKey?: string;
      flagID?: number;
      entityID?: string;
      entityType?: string;
      entityContext?: Record<string, any>;
      enableDebug?: boolean;
    }
  ): Promise<EvalResult> {
    const cacheKey: CacheKey = {
      flagKey: options.flagKey,
      flagID: options.flagID,
      entityID: options.entityID,
      entityType: options.entityType,
    };

    // Try cache first
    if (this.cache) {
      const cached = await this.cache.get(cacheKey);
      if (cached) {
        return cached;
      }
    }

    // Evaluate via API
    const evalContext: EvalContext = {
      flagKey: options.flagKey,
      flagID: options.flagID,
      entityID: options.entityID,
      entityType: options.entityType,
      entityContext: options.entityContext,
      enableDebug: options.enableDebug ?? false,
    };

    const response = await this.evaluationApi.postEvaluation(evalContext);
    const result = response.data;

    // Cache result
    if (this.cache) {
      await this.cache.put(cacheKey, result);
    }

    return result;
  }

  /**
   * Batch evaluate flags for multiple entities.
   * 
   * Evaluates multiple flags for multiple entities in a single API call.
   * Batch results are not cached.
   * 
   * @param options Batch evaluation options
   * @param options.flagKeys List of flag keys to evaluate (optional if flagIDs is provided)
   * @param options.flagIDs List of flag IDs to evaluate (optional if flagKeys is provided)
   * @param options.entities List of entities to evaluate for
   * @param options.enableDebug Enable debug mode for detailed evaluation logs
   * @returns List of evaluation results
   * @throws Error if API call fails
   */
  async evaluateBatch(
    options: {
      flagKeys?: string[];
      flagIDs?: number[];
      entities: EvaluationEntity[];
      enableDebug?: boolean;
    }
  ): Promise<EvalResult[]> {
    const request: EvaluationBatchRequest = {
      entities: options.entities,
      flagKeys: options.flagKeys,
      flagIDs: options.flagIDs,
      enableDebug: options.enableDebug ?? false,
    };

    const response = await this.evaluationApi.postEvaluationBatch(request);
    return response.data.evaluationResults;
  }

  /**
   * Clear all cached evaluation results.
   * 
   * Removes all entries from the cache. This does not affect ongoing evaluations.
   */
  async clearCache(): Promise<void> {
    if (this.cache) {
      await this.cache.clear();
    }
  }

  /**
   * Evict expired entries from cache.
   * 
   * Removes entries that have exceeded their TTL. This is called automatically
   * on a periodic basis, but can be called manually to force cleanup.
   */
  async evictExpired(): Promise<void> {
    if (this.cache) {
      await this.cache.evictExpired();
    }
  }

  /**
   * Destroy the manager and clean up resources.
   * 
   * Stops the periodic cache cleanup interval. Call this when the manager
   * is no longer needed to prevent memory leaks.
   */
  destroy(): void {
    if (this.cleanupInterval) {
      clearInterval(this.cleanupInterval);
    }
  }
}