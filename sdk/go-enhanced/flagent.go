package flagentenhanced

import (
	"context"
	"net/http"
	"strings"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

// EvalResult is the unified evaluation result returned by Client (server or offline).
type EvalResult struct {
	Enabled    bool
	FlagKey    string
	VariantKey string
	EntityID   string
}

// Client is the unified Flagent client interface. Use NewFlagent to create server or offline implementations.
type Client interface {
	Evaluate(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (*EvalResult, error)
	IsEnabled(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (bool, error)
	EvaluateBatch(ctx context.Context, flagKeys []string, entities []flagent.EvaluationEntity) ([]*EvalResult, error)
	Close()
}

// Options configures NewFlagent (base URL, auth, cache, server vs offline mode).
type Options struct {
	// BaseURL is required (can also be passed as first argument to NewFlagent).
	BaseURL string

	// APIKey sets Bearer token for auth (optional).
	APIKey string
	// HTTPClient is the HTTP client to use (optional). If nil, default client with Timeout is used.
	HTTPClient *http.Client
	// Timeout is used when HTTPClient is nil (default: 30s).
	Timeout time.Duration

	// Offline enables client-side evaluation (OfflineManager). If false, server-side Manager is used.
	Offline bool

	// --- Server mode (when Offline == false) ---
	EnableCache             bool
	CacheTTL                 time.Duration
	SnapshotRefreshInterval  time.Duration

	// --- Offline mode (when Offline == true) ---
	EnablePersistence bool
	StorageDir       string
	AutoRefresh      bool
	RefreshInterval  time.Duration
	SnapshotTTL      time.Duration

	EnableDebugLogging bool
}

// DefaultOptions returns options with sensible defaults (server mode, cache enabled).
func DefaultOptions() Options {
	return Options{
		Timeout:                 30 * time.Second,
		EnableCache:             true,
		CacheTTL:                5 * time.Minute,
		SnapshotRefreshInterval: 0,
		EnablePersistence:       true,
		AutoRefresh:             true,
		RefreshInterval:         60 * time.Second,
		SnapshotTTL:             5 * time.Minute,
	}
}

// NewFlagent creates a unified Client. Mode (server or offline) is determined by opts.Offline.
// For offline mode, Bootstrap is called internally so the client is ready after NewFlagent returns.
func NewFlagent(ctx context.Context, baseURL string, opts Options) (Client, error) {
	if baseURL == "" {
		baseURL = opts.BaseURL
	}
	if baseURL == "" || strings.TrimSpace(baseURL) == "" {
		return nil, flagent.NewInvalidConfigError("baseURL is required", nil)
	}
	baseURL = strings.TrimSuffix(strings.TrimSpace(baseURL), "/")

	clientOpts := []flagent.ClientOption{}
	if opts.APIKey != "" {
		clientOpts = append(clientOpts, flagent.WithAPIKey(opts.APIKey))
	}
	if opts.HTTPClient != nil {
		clientOpts = append(clientOpts, flagent.WithHTTPClient(opts.HTTPClient))
	} else if opts.Timeout > 0 {
		clientOpts = append(clientOpts, flagent.WithTimeout(opts.Timeout))
	}

	baseClient, err := flagent.NewClient(baseURL, clientOpts...)
	if err != nil {
		return nil, err
	}

	if opts.Offline {
		offlineCfg := DefaultOfflineConfig().
			WithPersistence(opts.EnablePersistence).
			WithStorageDir(opts.StorageDir).
			WithAutoRefresh(opts.AutoRefresh).
			WithRefreshInterval(opts.RefreshInterval).
			WithSnapshotTTL(opts.SnapshotTTL).
			WithDebugLogging(opts.EnableDebugLogging)
		om := NewOfflineManager(baseClient, offlineCfg)
		if err := om.Bootstrap(ctx, false); err != nil {
			om.Close()
			return nil, err
		}
		return &offlineClientAdapter{om: om}, nil
	}

	cfg := DefaultConfig().
		WithCacheTTL(opts.CacheTTL).
		WithEnableCache(opts.EnableCache).
		WithSnapshotRefreshInterval(opts.SnapshotRefreshInterval).
		WithDebugLogging(opts.EnableDebugLogging)
	mgr := NewManager(baseClient, cfg)
	return &serverClientAdapter{manager: mgr}, nil
}

// serverClientAdapter adapts Manager to Client with unified EvalResult.
type serverClientAdapter struct {
	manager *Manager
}

func (a *serverClientAdapter) Evaluate(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (*EvalResult, error) {
	res, err := a.manager.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return nil, err
	}
	return serverResultToEvalResult(res, flagKey, entityID), nil
}

func (a *serverClientAdapter) IsEnabled(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (bool, error) {
	return a.manager.IsEnabled(ctx, flagKey, entityID, entityContext)
}

func (a *serverClientAdapter) EvaluateBatch(ctx context.Context, flagKeys []string, entities []flagent.EvaluationEntity) ([]*EvalResult, error) {
	results, err := a.manager.EvaluateBatch(ctx, flagKeys, entities)
	if err != nil {
		return nil, err
	}
	out := make([]*EvalResult, len(results))
	for i, r := range results {
		// Result order matches request: for each entity, for each flagKey
		eidx := i / len(flagKeys)
		fidx := i % len(flagKeys)
		eid := ""
		if eidx < len(entities) {
			eid = entities[eidx].EntityID
		}
		fk := ""
		if fidx < len(flagKeys) {
			fk = flagKeys[fidx]
		}
		out[i] = serverResultToEvalResult(r, fk, eid)
	}
	return out, nil
}

func (a *serverClientAdapter) Close() {
	a.manager.Close()
}

func serverResultToEvalResult(r *flagent.EvaluationResult, flagKey, entityID string) *EvalResult {
	if r == nil {
		return &EvalResult{FlagKey: flagKey, EntityID: entityID}
	}
	out := &EvalResult{
		Enabled:  r.IsEnabled(),
		FlagKey:  flagKey,
		EntityID: entityID,
	}
	if r.VariantKey != nil {
		out.VariantKey = *r.VariantKey
	}
	return out
}

// offlineClientAdapter adapts OfflineManager to Client with unified EvalResult.
type offlineClientAdapter struct {
	om *OfflineManager
}

func (a *offlineClientAdapter) Evaluate(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (*EvalResult, error) {
	res, err := a.om.Evaluate(ctx, flagKey, entityID, entityContext)
	if err != nil {
		return nil, err
	}
	return offlineResultToEvalResult(res, flagKey, entityID), nil
}

func (a *offlineClientAdapter) IsEnabled(ctx context.Context, flagKey, entityID string, entityContext map[string]interface{}) (bool, error) {
	return a.om.IsEnabled(ctx, flagKey, entityID, entityContext)
}

func (a *offlineClientAdapter) EvaluateBatch(ctx context.Context, flagKeys []string, entities []flagent.EvaluationEntity) ([]*EvalResult, error) {
	var requests []*OfflineEvaluationRequest
	for _, e := range entities {
		for _, fk := range flagKeys {
			k := fk
			requests = append(requests, &OfflineEvaluationRequest{
				FlagKey:       &k,
				EntityID:      e.EntityID,
				EntityContext: e.EntityContext,
				EnableDebug:   false,
			})
		}
	}
	results, err := a.om.EvaluateBatch(ctx, requests)
	if err != nil {
		return nil, err
	}
	out := make([]*EvalResult, len(results))
	for i, r := range results {
		fk := flagKeys[i%len(flagKeys)]
		eidx := i / len(flagKeys)
		eid := ""
		if eidx < len(entities) {
			eid = entities[eidx].EntityID
		}
		out[i] = offlineResultToEvalResult(r, fk, eid)
	}
	return out, nil
}

func (a *offlineClientAdapter) Close() {
	a.om.Close()
}

func offlineResultToEvalResult(r *LocalEvaluationResult, flagKey, entityID string) *EvalResult {
	if r == nil {
		return &EvalResult{FlagKey: flagKey, EntityID: entityID}
	}
	out := &EvalResult{
		Enabled:  r.IsEnabled(),
		FlagKey:  flagKey,
		EntityID: entityID,
	}
	if r.FlagKey != nil {
		out.FlagKey = *r.FlagKey
	}
	if r.VariantKey != nil {
		out.VariantKey = *r.VariantKey
	}
	return out
}
