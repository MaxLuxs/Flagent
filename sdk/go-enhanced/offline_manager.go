package flagentenhanced

import (
	"context"
	"errors"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"sync"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

// OfflineManager is an offline-first feature flag manager with client-side evaluation
type OfflineManager struct {
	client    *flagent.Client
	config    *OfflineConfig
	evaluator *LocalEvaluator
	fetcher   *SnapshotFetcher
	storage   SnapshotStorage

	snapshot        *FlagSnapshot
	snapshotMutex   sync.RWMutex
	isBootstrapped  bool
	stopRefresh     chan struct{}
	refreshStopOnce sync.Once

	// Real-time updates
	sseClient       *SSEClient
	sseStopOnce     sync.Once
}

// NewOfflineManager creates a new offline manager
func NewOfflineManager(client *flagent.Client, config *OfflineConfig) *OfflineManager {
	if config == nil {
		config = DefaultOfflineConfig()
	}

	// Create storage
	var storage SnapshotStorage
	if config.EnablePersistence {
		storageDir := config.StorageDir
		if storageDir == "" {
			// Default to ~/.flagent
			home, _ := os.UserHomeDir()
			storageDir = filepath.Join(home, ".flagent")
		}
		storage = NewFileSnapshotStorage(storageDir)
	} else {
		storage = NewInMemorySnapshotStorage()
	}

	return &OfflineManager{
		client:      client,
		config:      config,
		evaluator:   NewLocalEvaluator(),
		fetcher:     NewSnapshotFetcher(client),
		storage:     storage,
		stopRefresh: make(chan struct{}),
	}
}

// Bootstrap initializes the manager by loading cached snapshot or fetching from server
func (m *OfflineManager) Bootstrap(ctx context.Context, forceRefresh bool) error {
	m.snapshotMutex.Lock()
	defer m.snapshotMutex.Unlock()

	if m.isBootstrapped && !forceRefresh {
		return nil
	}

	// Try to load cached snapshot first
	if !forceRefresh {
		cached, err := m.storage.Load()
		if err == nil && cached != nil && !cached.IsExpired() {
			m.snapshot = cached
			m.isBootstrapped = true

			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] Loaded cached snapshot with %d flags", len(cached.Flags))
			}

			// Start auto-refresh if enabled
			m.startAutoRefresh()
			return nil
		}
	}

	// Fetch fresh snapshot
	if err := m.fetchAndSave(ctx); err != nil {
		// If fetch fails, try to use expired cache
		cached, cacheErr := m.storage.Load()
		if cacheErr == nil && cached != nil {
			m.snapshot = cached
			m.isBootstrapped = true

			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] Using expired cached snapshot (fetch failed: %v)", err)
			}

			m.startAutoRefresh()
			return nil
		}

		return fmt.Errorf("failed to bootstrap: %w", err)
	}

	m.isBootstrapped = true
	m.startAutoRefresh()
	return nil
}

// Evaluate evaluates a flag locally using cached snapshot
func (m *OfflineManager) Evaluate(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (*LocalEvaluationResult, error) {
	snapshot, err := m.getSnapshot()
	if err != nil {
		return nil, err
	}

	req := &OfflineEvaluationRequest{
		FlagKey:       &flagKey,
		EntityID:      entityID,
		EntityContext: entityContext,
		EnableDebug:   m.config.EnableDebugLogging,
	}

	return m.evaluator.Evaluate(req, snapshot), nil
}

// IsEnabled checks if a flag is enabled for a given entity
func (m *OfflineManager) IsEnabled(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (bool, error) {
	result, err := m.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return false, err
	}
	return result.IsEnabled(), nil
}

// GetVariant returns the variant key for a given flag and entity
func (m *OfflineManager) GetVariant(ctx context.Context, flagKey string, entityID string, entityContext map[string]interface{}) (string, error) {
	result, err := m.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return "", err
	}
	if result.VariantKey == nil {
		return "", nil
	}
	return *result.VariantKey, nil
}

// EvaluateBatch evaluates multiple flags
func (m *OfflineManager) EvaluateBatch(ctx context.Context, requests []*OfflineEvaluationRequest) ([]*LocalEvaluationResult, error) {
	snapshot, err := m.getSnapshot()
	if err != nil {
		return nil, err
	}

	return m.evaluator.EvaluateBatch(requests, snapshot), nil
}

// Refresh manually refreshes the snapshot from server
func (m *OfflineManager) Refresh(ctx context.Context) error {
	m.snapshotMutex.Lock()
	defer m.snapshotMutex.Unlock()

	return m.fetchAndSave(ctx)
}

// IsReady returns true if the manager is ready for evaluation
func (m *OfflineManager) IsReady() bool {
	m.snapshotMutex.RLock()
	defer m.snapshotMutex.RUnlock()

	return m.isBootstrapped && m.snapshot != nil
}

// GetSnapshotAge returns the age of the current snapshot in milliseconds
func (m *OfflineManager) GetSnapshotAge() (int64, error) {
	m.snapshotMutex.RLock()
	defer m.snapshotMutex.RUnlock()

	if m.snapshot == nil {
		return 0, errors.New("no snapshot available")
	}

	return time.Now().UnixMilli() - m.snapshot.FetchedAt, nil
}

// IsSnapshotExpired returns true if the snapshot is expired
func (m *OfflineManager) IsSnapshotExpired() bool {
	m.snapshotMutex.RLock()
	defer m.snapshotMutex.RUnlock()

	if m.snapshot == nil {
		return true
	}

	return m.snapshot.IsExpired()
}

// ClearCache clears all cached data
func (m *OfflineManager) ClearCache() error {
	m.snapshotMutex.Lock()
	defer m.snapshotMutex.Unlock()

	m.snapshot = nil
	m.isBootstrapped = false

	return m.storage.Clear()
}

// EnableRealtimeUpdates enables real-time updates via SSE
func (m *OfflineManager) EnableRealtimeUpdates(baseURL string, flagKeys []string, flagIDs []int64) error {
	if m.sseClient != nil {
		return errors.New("real-time updates already enabled")
	}

	sseConfig := DefaultSSEConfig()
	sseConfig.EnableDebugLogging = m.config.EnableDebugLogging

	m.sseClient = NewSSEClient(baseURL, nil, sseConfig)
	m.sseClient.Connect(flagKeys, flagIDs)

	// Start listening for events
	go m.handleSSEEvents()

	if m.config.EnableDebugLogging {
		log.Println("[Flagent] Real-time updates enabled via SSE")
	}

	return nil
}

// DisableRealtimeUpdates disables real-time updates
func (m *OfflineManager) DisableRealtimeUpdates() {
	if m.sseClient == nil {
		return
	}

	m.sseStopOnce.Do(func() {
		m.sseClient.Disconnect()
		m.sseClient = nil
	})

	if m.config.EnableDebugLogging {
		log.Println("[Flagent] Real-time updates disabled")
	}
}

// IsRealtimeEnabled returns true if real-time updates are enabled
func (m *OfflineManager) IsRealtimeEnabled() bool {
	return m.sseClient != nil && m.sseClient.IsConnected()
}

// handleSSEEvents processes SSE events and triggers snapshot refresh
func (m *OfflineManager) handleSSEEvents() {
	if m.sseClient == nil {
		return
	}

	for {
		select {
		case event, ok := <-m.sseClient.Events():
			if !ok {
				return // Channel closed
			}

			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] Received SSE event: %s for flag %v", event.Type, event.FlagKey)
			}

			// Trigger snapshot refresh on flag update
			go func() {
				ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
				defer cancel()

				if err := m.Refresh(ctx); err != nil {
					if m.config.EnableDebugLogging {
						log.Printf("[Flagent] Failed to refresh snapshot after SSE event: %v", err)
					}
				}
			}()

		case status, ok := <-m.sseClient.Status():
			if !ok {
				return
			}

			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] SSE connection status: %d", status)
			}

		case err, ok := <-m.sseClient.Errors():
			if !ok {
				return
			}

			if m.config.EnableDebugLogging {
				log.Printf("[Flagent] SSE error: %v", err)
			}
		}
	}
}

// Close stops auto-refresh, SSE, and cleans up resources
func (m *OfflineManager) Close() {
	m.refreshStopOnce.Do(func() {
		close(m.stopRefresh)
	})

	m.DisableRealtimeUpdates()
}

// getSnapshot returns the current snapshot, checking for expiration
func (m *OfflineManager) getSnapshot() (*FlagSnapshot, error) {
	m.snapshotMutex.RLock()
	snapshot := m.snapshot
	m.snapshotMutex.RUnlock()

	if snapshot == nil {
		return nil, errors.New("manager not bootstrapped, call Bootstrap() first")
	}

	// If expired and auto-refresh enabled, trigger refresh in background
	if snapshot.IsExpired() && m.config.AutoRefresh {
		go func() {
			ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
			defer cancel()

			if err := m.Refresh(ctx); err != nil {
				if m.config.EnableDebugLogging {
					log.Printf("[Flagent] Background refresh failed: %v", err)
				}
			}
		}()
	}

	return snapshot, nil
}

// fetchAndSave fetches a fresh snapshot and saves it
func (m *OfflineManager) fetchAndSave(ctx context.Context) error {
	snapshot, err := m.fetcher.FetchSnapshot(ctx, m.config.SnapshotTTL.Milliseconds())
	if err != nil {
		return err
	}

	m.snapshot = snapshot

	if err := m.storage.Save(snapshot); err != nil {
		// Log error but don't fail - snapshot is still in memory
		if m.config.EnableDebugLogging {
			log.Printf("[Flagent] Failed to save snapshot to storage: %v", err)
		}
	}

	if m.config.EnableDebugLogging {
		log.Printf("[Flagent] Fetched and saved snapshot with %d flags", len(snapshot.Flags))
	}

	return nil
}

// startAutoRefresh starts automatic background refresh
func (m *OfflineManager) startAutoRefresh() {
	if !m.config.AutoRefresh || m.config.RefreshInterval <= 0 {
		return
	}

	go func() {
		ticker := time.NewTicker(m.config.RefreshInterval)
		defer ticker.Stop()

		for {
			select {
			case <-ticker.C:
				ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
				if err := m.Refresh(ctx); err != nil {
					if m.config.EnableDebugLogging {
						log.Printf("[Flagent] Auto-refresh failed: %v", err)
					}
				}
				cancel()

			case <-m.stopRefresh:
				return
			}
		}
	}()
}
