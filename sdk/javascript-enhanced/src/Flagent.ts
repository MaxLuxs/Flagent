import { Configuration, EvalResult, EvaluationEntity } from '@flagent/client';
import { FlagentManager } from './manager/FlagentManager';
import { defaultFlagentConfig, FlagentConfig } from './config/FlagentConfig';

/**
 * Options for creating a Flagent client via Flagent.create().
 * Single entry point for server-side and web/React Native usage.
 */
export interface FlagentOptions {
  /** Base URL/path for the Flagent API (e.g. "https://api.example.com/api/v1"). Required. */
  basePath: string;
  /** Cache TTL in milliseconds. Default: 300000 (5 minutes). */
  cacheTtlMs?: number;
  /** Enable evaluation result cache. Default: true. */
  enableCache?: boolean;
  /** Enable debug logging. Default: false. */
  enableDebugLogging?: boolean;
  /** Optional auth: Bearer token. */
  accessToken?: string | (() => string) | (() => Promise<string>);
  /** Optional auth: API key (header name is typically "X-Api-Key" or from server). */
  apiKey?: string | (() => string) | (() => Promise<string>);
  /** Optional auth: Basic username. */
  username?: string;
  /** Optional auth: Basic password. */
  password?: string;
  /** Optional: custom base options passed to axios (e.g. headers, timeout). */
  baseOptions?: Record<string, unknown>;
}

/**
 * Client interface returned by Flagent.create().
 * Use only evaluate, isEnabled, evaluateBatch — no need to know EvaluationApi or FlagentManager.
 */
export interface FlagentClient {
  /**
   * Evaluate a flag for the given entity/context.
   */
  evaluate(options: {
    flagKey?: string;
    flagID?: number;
    entityID?: string;
    entityType?: string;
    entityContext?: Record<string, unknown>;
    enableDebug?: boolean;
  }): Promise<EvalResult>;

  /**
   * Returns true if the flag is "on" for the entity: variant is present and not "control".
   * Default (e.g. on error or missing) is false.
   */
  isEnabled(options: {
    flagKey?: string;
    flagID?: number;
    entityID?: string;
    entityType?: string;
    entityContext?: Record<string, unknown>;
    enableDebug?: boolean;
  }): Promise<boolean>;

  /**
   * Batch evaluate flags for multiple entities. Results are not cached.
   */
  evaluateBatch(options: {
    flagKeys?: string[];
    flagIDs?: number[];
    entities: EvaluationEntity[];
    enableDebug?: boolean;
  }): Promise<EvalResult[]>;

  /**
   * Clear cached evaluation results (no-op if cache disabled).
   */
  clearCache(): Promise<void>;

  /**
   * Stop cleanup timers and release resources. Call when client is no longer needed.
   */
  destroy(): void;
}

function buildConfiguration(options: FlagentOptions): Configuration {
  const params: Record<string, unknown> = {
    basePath: options.basePath,
    ...(options.accessToken != null && { accessToken: options.accessToken }),
    ...(options.apiKey != null && { apiKey: options.apiKey }),
    ...(options.username != null && { username: options.username }),
    ...(options.password != null && { password: options.password }),
    ...(options.baseOptions != null && { baseOptions: options.baseOptions }),
  };
  return new Configuration(params as any);
}

function buildFlagentConfig(options: FlagentOptions): FlagentConfig {
  return {
    ...defaultFlagentConfig,
    cacheTtlMs: options.cacheTtlMs ?? defaultFlagentConfig.cacheTtlMs,
    enableCache: options.enableCache ?? defaultFlagentConfig.enableCache,
    enableDebugLogging: options.enableDebugLogging ?? defaultFlagentConfig.enableDebugLogging,
  };
}

/**
 * Default variant key that is considered "disabled" for isEnabled().
 * Any other non-empty variant is considered "enabled".
 */
export const DISABLED_VARIANT_KEY = 'control';

/**
 * Single entry point for the Flagent Enhanced SDK.
 *
 * Create a client with Flagent.create(options), then use evaluate / isEnabled / evaluateBatch.
 * Internal APIs (EvaluationApi, FlagentManager) stay hidden.
 *
 * @example
 * ```ts
 * const client = Flagent.create({
 *   basePath: 'https://api.example.com/api/v1',
 *   enableCache: true,
 *   cacheTtlMs: 60_000,
 * });
 * const result = await client.evaluate({ flagKey: 'new_feature', entityID: 'user123' });
 * const on = await client.isEnabled({ flagKey: 'new_feature', entityID: 'user123' });
 * ```
 */
export const Flagent = {
  /**
   * Create a Flagent client. Uses server-side evaluation (EvaluationApi + FlagentManager).
   * For offline/client-side evaluation, use FlagentManager/offline APIs directly (advanced).
   */
  create(options: FlagentOptions): FlagentClient {
    if (!options?.basePath) {
      throw new Error('Flagent.create: basePath is required');
    }
    const configuration = buildConfiguration(options);
    const config = buildFlagentConfig(options);
    const manager = new FlagentManager(configuration, config);

    const client: FlagentClient = {
      async evaluate(evalOptions) {
        return manager.evaluate(evalOptions);
      },

      async isEnabled(evalOptions) {
        try {
          const result = await manager.evaluate(evalOptions);
          const v = result?.variantKey;
          return v != null && v !== '' && v !== DISABLED_VARIANT_KEY;
        } catch {
          return false;
        }
      },

      async evaluateBatch(batchOptions) {
        return manager.evaluateBatch(batchOptions);
      },

      async clearCache() {
        return manager.clearCache();
      },

      destroy() {
        manager.destroy();
      },
    };

    return client;
  },
};
