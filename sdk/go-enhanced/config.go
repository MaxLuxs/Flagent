package flagentenhanced

import "time"

// Config represents configuration for FlagentManager
type Config struct {
	// CacheTTL is the time-to-live for cached evaluation results
	CacheTTL time.Duration

	// EnableCache enables or disables caching
	EnableCache bool

	// EnableDebugLogging enables debug logging
	EnableDebugLogging bool

	// SnapshotRefreshInterval is the interval for automatic snapshot refresh
	// Set to 0 to disable auto-refresh
	SnapshotRefreshInterval time.Duration
}

// DefaultConfig returns the default configuration
func DefaultConfig() *Config {
	return &Config{
		CacheTTL:                5 * time.Minute,
		EnableCache:             true,
		EnableDebugLogging:      false,
		SnapshotRefreshInterval: 0, // Disabled by default
	}
}

// WithCacheTTL sets the cache TTL
func (c *Config) WithCacheTTL(ttl time.Duration) *Config {
	c.CacheTTL = ttl
	return c
}

// WithEnableCache enables or disables caching
func (c *Config) WithEnableCache(enable bool) *Config {
	c.EnableCache = enable
	return c
}

// WithDebugLogging enables or disables debug logging
func (c *Config) WithDebugLogging(enable bool) *Config {
	c.EnableDebugLogging = enable
	return c
}

// WithSnapshotRefreshInterval sets the snapshot refresh interval
func (c *Config) WithSnapshotRefreshInterval(interval time.Duration) *Config {
	c.SnapshotRefreshInterval = interval
	return c
}
