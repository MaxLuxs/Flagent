import { EvalResult } from '@flagent/client';

/**
 * Cache key for evaluation results.
 * 
 * Used to uniquely identify cached evaluation results based on flag and entity.
 */
export interface CacheKey {
  /** The flag key (optional if flagID is provided) */
  flagKey?: string;
  /** The flag ID (optional if flagKey is provided) */
  flagID?: number;
  /** The entity ID */
  entityID?: string;
  /** The entity type */
  entityType?: string;
}

interface CachedEntry {
  result: EvalResult;
  timestamp: number;
}

/**
 * Cache interface for evaluation results.
 * 
 * Provides caching of evaluation results with TTL-based expiration.
 */
export interface EvaluationCache {
  /**
   * Get cached evaluation result.
   * 
   * @param key The cache key
   * @returns Cached result if available and not expired, null otherwise
   */
  get(key: CacheKey): Promise<EvalResult | null>;
  
  /**
   * Put evaluation result in cache.
   * 
   * @param key The cache key
   * @param result The evaluation result to cache
   */
  put(key: CacheKey, result: EvalResult): Promise<void>;
  
  /**
   * Clear all cached entries.
   */
  clear(): Promise<void>;
  
  /**
   * Remove expired entries from cache.
   */
  evictExpired(): Promise<void>;
}

/**
 * Thread-safe in-memory cache implementation.
 * 
 * Uses Map for storage. Entries are automatically expired based on TTL.
 * 
 * @param ttlMs Time-to-live for cached entries in milliseconds
 */
export class InMemoryEvaluationCache implements EvaluationCache {
  private cache: Map<string, CachedEntry> = new Map();
  private readonly ttlMs: number;

  /**
   * Creates a new InMemoryEvaluationCache instance.
   * 
   * @param ttlMs Time-to-live for cached entries in milliseconds
   */
  constructor(ttlMs: number) {
    this.ttlMs = ttlMs;
  }

  private toKeyString(key: CacheKey): string {
    return `${key.flagID ?? key.flagKey ?? ''}_${key.entityID ?? ''}_${key.entityType ?? ''}`;
  }

  async get(key: CacheKey): Promise<EvalResult | null> {
    const keyString = this.toKeyString(key);
    const entry = this.cache.get(keyString);
    
    if (!entry) {
      return null;
    }

    const now = Date.now();
    if (now - entry.timestamp > this.ttlMs) {
      this.cache.delete(keyString);
      return null;
    }

    return entry.result;
  }

  async put(key: CacheKey, result: EvalResult): Promise<void> {
    const keyString = this.toKeyString(key);
    this.cache.set(keyString, {
      result,
      timestamp: Date.now(),
    });
  }

  async clear(): Promise<void> {
    this.cache.clear();
  }

  async evictExpired(): Promise<void> {
    const now = Date.now();
    const keysToDelete: string[] = [];
    
    this.cache.forEach((entry, key) => {
      if (now - entry.timestamp > this.ttlMs) {
        keysToDelete.push(key);
      }
    });

    keysToDelete.forEach(key => this.cache.delete(key));
  }
}