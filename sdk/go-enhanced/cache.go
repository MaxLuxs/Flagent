package flagentenhanced

import (
	"sync"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

// CacheEntry represents a cached evaluation result
type CacheEntry struct {
	Result    *flagent.EvaluationResult
	ExpiresAt time.Time
}

// IsExpired checks if the cache entry is expired
func (e *CacheEntry) IsExpired() bool {
	return time.Now().After(e.ExpiresAt)
}

// EvaluationCache is an interface for caching evaluation results
type EvaluationCache interface {
	// Get retrieves a cached evaluation result
	Get(key string) (*flagent.EvaluationResult, bool)

	// Set stores an evaluation result in cache
	Set(key string, result *flagent.EvaluationResult, ttl time.Duration)

	// Delete removes an entry from cache
	Delete(key string)

	// Clear removes all entries from cache
	Clear()

	// EvictExpired removes all expired entries
	EvictExpired()
}

// InMemoryCache is a thread-safe in-memory cache implementation
type InMemoryCache struct {
	mu      sync.RWMutex
	entries map[string]*CacheEntry
}

// NewInMemoryCache creates a new in-memory cache
func NewInMemoryCache() *InMemoryCache {
	return &InMemoryCache{
		entries: make(map[string]*CacheEntry),
	}
}

// Get retrieves a cached evaluation result
func (c *InMemoryCache) Get(key string) (*flagent.EvaluationResult, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	entry, ok := c.entries[key]
	if !ok {
		return nil, false
	}

	if entry.IsExpired() {
		return nil, false
	}

	return entry.Result, true
}

// Set stores an evaluation result in cache
func (c *InMemoryCache) Set(key string, result *flagent.EvaluationResult, ttl time.Duration) {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.entries[key] = &CacheEntry{
		Result:    result,
		ExpiresAt: time.Now().Add(ttl),
	}
}

// Delete removes an entry from cache
func (c *InMemoryCache) Delete(key string) {
	c.mu.Lock()
	defer c.mu.Unlock()

	delete(c.entries, key)
}

// Clear removes all entries from cache
func (c *InMemoryCache) Clear() {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.entries = make(map[string]*CacheEntry)
}

// EvictExpired removes all expired entries
func (c *InMemoryCache) EvictExpired() {
	c.mu.Lock()
	defer c.mu.Unlock()

	now := time.Now()
	for key, entry := range c.entries {
		if now.After(entry.ExpiresAt) {
			delete(c.entries, key)
		}
	}
}

// Size returns the number of entries in cache
func (c *InMemoryCache) Size() int {
	c.mu.RLock()
	defer c.mu.RUnlock()

	return len(c.entries)
}
