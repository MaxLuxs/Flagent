package flagentenhanced

import (
	"context"
	"crypto/md5"
	"fmt"
	"log"
	"sync"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

// Manager is an enhanced Flagent client with caching and convenient API
type Manager struct {
	client *flagent.Client
	config *Config
	cache  EvaluationCache

	// For auto-refresh
	stopRefresh chan struct{}
	refreshOnce sync.Once
}

// NewManager creates a new enhanced Flagent manager
func NewManager(client *flagent.Client, config *Config) *Manager {
	if config == nil {
		config = DefaultConfig()
	}

	var cache EvaluationCache
	if config.EnableCache {
		cache = NewInMemoryCache()
	}

	manager := &Manager{
		client:      client,
		config:      config,
		cache:       cache,
		stopRefresh: make(chan struct{}),
	}

	// Start auto-refresh if configured
	if config.SnapshotRefreshInterval > 0 {
		go manager.startAutoRefresh()
	}

	return manager
}

// Evaluate evaluates a single flag with caching
func (m *Manager) Evaluate(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (*flagent.EvaluationResult, error) {
	// Generate cache key
	cacheKey := m.generateCacheKey(flagKey, entityID, entityContext)

	// Check cache if enabled
	if m.config.EnableCache && m.cache != nil {
		if result, ok := m.cache.Get(cacheKey); ok {
			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] Cache hit for flag=%s, entity=%s", flagKey, entityID)
			}
			return result, nil
		}
	}

	// Evaluate from server
	result, err := m.client.Evaluate(ctx, &flagent.EvaluationContext{
		FlagKey:       flagent.StringPtr(flagKey),
		EntityID:      flagent.StringPtr(entityID),
		EntityContext: entityContext,
	})
	if err != nil {
		return nil, err
	}

	// Store in cache if enabled
	if m.config.EnableCache && m.cache != nil {
		m.cache.Set(cacheKey, result, m.config.CacheTTL)
		if m.config.EnableDebugLogging {
			log.Printf("[Flagent] Cached result for flag=%s, entity=%s", flagKey, entityID)
		}
	}

	return result, nil
}

// EvaluateBatch evaluates multiple flags for multiple entities
func (m *Manager) EvaluateBatch(ctx context.Context, flagKeys []string, entities []flagent.EvaluationEntity) ([]*flagent.EvaluationResult, error) {
	return m.client.EvaluateBatch(ctx, &flagent.BatchEvaluationRequest{
		FlagKeys: flagKeys,
		Entities: entities,
	})
}

// IsEnabled checks if a flag is enabled for a given entity
func (m *Manager) IsEnabled(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (bool, error) {
	result, err := m.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return false, err
	}
	return result.IsEnabled(), nil
}

// GetVariant returns the variant key for a given flag and entity
func (m *Manager) GetVariant(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (string, error) {
	result, err := m.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return "", err
	}
	if result.VariantKey == nil {
		return "", nil
	}
	return *result.VariantKey, nil
}

// ClearCache clears all cached entries
func (m *Manager) ClearCache() {
	if m.cache != nil {
		m.cache.Clear()
		if m.config.EnableDebugLogging {
			log.Println("[Flagent] Cache cleared")
		}
	}
}

// EvictExpired removes expired entries from cache
func (m *Manager) EvictExpired() {
	if m.cache != nil {
		m.cache.EvictExpired()
		if m.config.EnableDebugLogging {
			log.Println("[Flagent] Expired entries evicted")
		}
	}
}

// generateCacheKey generates a cache key for evaluation
func (m *Manager) generateCacheKey(flagKey string, entityID string, entityContext map[string]interface{}) string {
	// Simple implementation - can be improved with better hashing
	contextStr := fmt.Sprintf("%v", entityContext)
	hash := md5.Sum([]byte(flagKey + entityID + contextStr))
	return fmt.Sprintf("%x", hash)
}

// startAutoRefresh starts automatic snapshot refresh
func (m *Manager) startAutoRefresh() {
	ticker := time.NewTicker(m.config.SnapshotRefreshInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			// Clear cache to force refresh
			m.ClearCache()
			if m.config.EnableDebugLogging {
				log.Println("[Flagent] Auto-refresh triggered")
			}
		case <-m.stopRefresh:
			return
		}
	}
}

// StopAutoRefresh stops automatic snapshot refresh
func (m *Manager) StopAutoRefresh() {
	m.refreshOnce.Do(func() {
		close(m.stopRefresh)
	})
}

// Close stops auto-refresh and cleans up resources
func (m *Manager) Close() {
	m.StopAutoRefresh()
}
