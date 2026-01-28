package flagentenhanced

import "time"

// OfflineConfig represents configuration for offline/client-side evaluation
type OfflineConfig struct {
	// EnablePersistence enables persistent storage of snapshots
	EnablePersistence bool

	// StorageDir is the directory for persistent storage (default: ~/.flagent)
	StorageDir string

	// AutoRefresh enables automatic background refresh of snapshots
	AutoRefresh bool

	// RefreshInterval is the interval for automatic refresh (default: 60 seconds)
	RefreshInterval time.Duration

	// SnapshotTTL is the time-to-live for snapshots (default: 5 minutes)
	SnapshotTTL time.Duration

	// EnableDebugLogging enables debug logging
	EnableDebugLogging bool
}

// DefaultOfflineConfig returns the default offline configuration
func DefaultOfflineConfig() *OfflineConfig {
	return &OfflineConfig{
		EnablePersistence:  true,
		StorageDir:         "", // Will use default (~/.flagent)
		AutoRefresh:        true,
		RefreshInterval:    60 * time.Second,
		SnapshotTTL:        5 * time.Minute,
		EnableDebugLogging: false,
	}
}

// WithPersistence enables or disables persistent storage
func (c *OfflineConfig) WithPersistence(enable bool) *OfflineConfig {
	c.EnablePersistence = enable
	return c
}

// WithStorageDir sets the storage directory
func (c *OfflineConfig) WithStorageDir(dir string) *OfflineConfig {
	c.StorageDir = dir
	return c
}

// WithAutoRefresh enables or disables auto-refresh
func (c *OfflineConfig) WithAutoRefresh(enable bool) *OfflineConfig {
	c.AutoRefresh = enable
	return c
}

// WithRefreshInterval sets the refresh interval
func (c *OfflineConfig) WithRefreshInterval(interval time.Duration) *OfflineConfig {
	c.RefreshInterval = interval
	return c
}

// WithSnapshotTTL sets the snapshot TTL
func (c *OfflineConfig) WithSnapshotTTL(ttl time.Duration) *OfflineConfig {
	c.SnapshotTTL = ttl
	return c
}

// WithDebugLogging enables or disables debug logging
func (c *OfflineConfig) WithDebugLogging(enable bool) *OfflineConfig {
	c.EnableDebugLogging = enable
	return c
}
