package flagentenhanced

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
	flagent "github.com/MaxLuxs/Flagent/sdk/go"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func makeOfflineSnapshot() *FlagSnapshot {
	return &FlagSnapshot{
		FetchedAt: time.Now().UnixMilli(),
		TTLMs:     300000,
		Flags: map[int64]*LocalFlag{
			1: {
				ID:      1,
				Key:     "test_flag",
				Enabled: true,
				Segments: []*LocalSegment{
					{
						ID:             1,
						FlagID:         1,
						Rank:           1,
						RolloutPercent: 100,
						Distributions: []*LocalDistribution{
							{ID: 1, VariantID: 1, VariantKey: "control", Percent: 100},
						},
					},
				},
				Variants: []*LocalVariant{
					{ID: 1, FlagID: 1, Key: "control"},
				},
			},
		},
	}
}

func TestOfflineConfig(t *testing.T) {
	cfg := DefaultOfflineConfig()
	require.NotNil(t, cfg)
	assert.True(t, cfg.EnablePersistence)

	cfg = cfg.WithPersistence(false).WithStorageDir("/tmp").WithAutoRefresh(false).
		WithRefreshInterval(0).WithSnapshotTTL(0).WithDebugLogging(true)
	assert.False(t, cfg.EnablePersistence)
	assert.Equal(t, "/tmp", cfg.StorageDir)
}

func TestOfflineManager_BootstrapAndEvaluate(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		fs := flagent.FlagSnapshot{
			Flags: []api.Flag{
				{
					Id:      1,
					Key:     "test_flag",
					Enabled: true,
					Segments: []api.Segment{
						{
							Id:             1,
							FlagID:         1,
							Rank:           1,
							RolloutPercent: 100,
							Constraints:    []api.Constraint{},
							Distributions: []api.Distribution{
								{Id: 1, VariantID: 1, VariantKey: *api.NewNullableString(api.PtrString("control")), Percent: 100},
							},
						},
					},
					Variants: []api.Variant{{Id: 1, FlagID: 1, Key: "control"}},
				},
			},
		}
		json.NewEncoder(w).Encode(fs)
	}))
	defer server.Close()

	client, err := flagent.NewClient(server.URL)
	require.NoError(t, err)

	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)

	ctx := context.Background()
	err = manager.Bootstrap(ctx, false)
	require.NoError(t, err)

	assert.True(t, manager.IsReady())

	result, err := manager.Evaluate(ctx, "test_flag", "user1", nil)
	require.NoError(t, err)
	require.NotNil(t, result)
	assert.True(t, result.IsEnabled())

	enabled, err := manager.IsEnabled(ctx, "test_flag", "user1", nil)
	require.NoError(t, err)
	assert.True(t, enabled)

	variant, err := manager.GetVariant(ctx, "test_flag", "user1", nil)
	require.NoError(t, err)
	assert.Equal(t, "control", variant)
}

func TestOfflineManager_GetSnapshotAge(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	snap := makeOfflineSnapshot()
	snap.FetchedAt = 1000
	storage.Save(snap)

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.storage = storage
	manager.snapshot = snap
	manager.isBootstrapped = true

	age, err := manager.GetSnapshotAge()
	require.NoError(t, err)
	assert.GreaterOrEqual(t, age, int64(0))
}

func TestOfflineManager_IsSnapshotExpired(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	snap := makeOfflineSnapshot()
	snap.TTLMs = 1
	snap.FetchedAt = 0
	storage.Save(snap)

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(200) }))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.storage = storage
	manager.snapshot = snap
	manager.isBootstrapped = true

	expired := manager.IsSnapshotExpired()
	assert.True(t, expired)
}

func TestOfflineManager_ClearCache(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	storage.Save(makeOfflineSnapshot())

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(200) }))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.storage = storage
	manager.snapshot = makeOfflineSnapshot()
	manager.isBootstrapped = true

	manager.ClearCache()
	loaded, _ := storage.Load()
	assert.Nil(t, loaded)
}

func TestOfflineManager_BootstrapFromCache(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	storage.Save(makeOfflineSnapshot())

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		t.Error("should not call server when cache valid")
	}))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.storage = storage

	ctx := context.Background()
	err := manager.Bootstrap(ctx, false)
	require.NoError(t, err)
	assert.True(t, manager.IsReady())
}

func TestOfflineManager_Refresh(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		fs := flagent.FlagSnapshot{
			Flags: []api.Flag{
				{Id: 1, Key: "f1", Enabled: true,
					Segments: []api.Segment{{
						Id: 1, FlagID: 1, Rank: 1, RolloutPercent: 100,
						Distributions: []api.Distribution{{Id: 1, VariantID: 1, VariantKey: *api.NewNullableString(api.PtrString("control")), Percent: 100}},
					}},
					Variants: []api.Variant{{Id: 1, FlagID: 1, Key: "control"}},
				},
			},
		}
		json.NewEncoder(w).Encode(fs)
	}))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.snapshot = makeOfflineSnapshot()
	manager.isBootstrapped = true

	ctx := context.Background()
	err := manager.Refresh(ctx)
	require.NoError(t, err)
}

func TestOfflineManager_Close(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(200) }))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	manager := NewOfflineManager(client, DefaultOfflineConfig().WithPersistence(false))
	manager.Close()
}

func TestOfflineManager_EvaluateBatch(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	storage.Save(makeOfflineSnapshot())

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(200) }))
	defer server.Close()

	client, _ := flagent.NewClient(server.URL)
	config := DefaultOfflineConfig().WithPersistence(false)
	manager := NewOfflineManager(client, config)
	manager.storage = storage
	manager.snapshot = makeOfflineSnapshot()
	manager.isBootstrapped = true

	ctx := context.Background()
	key := "test_flag"
	reqs := []*OfflineEvaluationRequest{
		{FlagKey: &key, EntityID: "u1", EntityContext: nil},
	}
	results, err := manager.EvaluateBatch(ctx, reqs)
	require.NoError(t, err)
	assert.Len(t, results, 1)
	assert.True(t, results[0].IsEnabled())
}
