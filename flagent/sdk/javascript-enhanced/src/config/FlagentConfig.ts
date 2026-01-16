/**
 * Configuration for Flagent Enhanced SDK.
 * 
 * Controls caching behavior and debug settings for the enhanced SDK.
 */
export interface FlagentConfig {
  /** Cache time-to-live in milliseconds. Default: 5 minutes (300000 ms) */
  cacheTtlMs: number;
  /** Enable caching of evaluation results. When disabled, all evaluations go directly to API without caching. Default: true */
  enableCache: boolean;
  /** Enable debug logging for development and troubleshooting. Default: false */
  enableDebugLogging: boolean;
}

export const defaultFlagentConfig: FlagentConfig = {
  cacheTtlMs: 5 * 60 * 1000, // 5 minutes
  enableCache: true,
  enableDebugLogging: false,
};