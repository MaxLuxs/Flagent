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

func TestNewFlagent_EmptyBaseURL(t *testing.T) {
	ctx := context.Background()
	opts := DefaultOptions()

	_, err := NewFlagent(ctx, "", opts)
	require.Error(t, err)

	_, err = NewFlagent(ctx, "  ", opts)
	require.Error(t, err)
}

func TestNewFlagent_ServerMode(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		if req.URL.Path == "/evaluation/batch" {
			batchRes := api.EvaluationBatchResponse{
				EvaluationResults: []api.EvalResult{
					*func() *api.EvalResult { r := &api.EvalResult{}; r.SetFlagKey("f1"); r.VariantKey = *api.NewNullableString(api.PtrString("control")); return r }(),
					*func() *api.EvalResult { r := &api.EvalResult{}; r.SetFlagKey("f2"); r.VariantKey = *api.NewNullableString(api.PtrString("treatment")); return r }(),
				},
			}
			json.NewEncoder(w).Encode(batchRes)
			return
		}
		evalRes := &api.EvalResult{}
		evalRes.SetFlagKey("f1")
		evalRes.VariantKey = *api.NewNullableString(api.PtrString("control"))
		json.NewEncoder(w).Encode(evalRes)
	}))
	defer server.Close()

	ctx := context.Background()
	opts := DefaultOptions()
	opts.Offline = false

	client, err := NewFlagent(ctx, server.URL, opts)
	require.NoError(t, err)
	require.NotNil(t, client)
	defer client.Close()

	// Evaluate
	res, err := client.Evaluate(ctx, "f1", "user1", nil)
	require.NoError(t, err)
	require.NotNil(t, res)
	assert.True(t, res.Enabled)
	assert.Equal(t, "f1", res.FlagKey)
	assert.Equal(t, "control", res.VariantKey)
	assert.Equal(t, "user1", res.EntityID)

	// IsEnabled
	enabled, err := client.IsEnabled(ctx, "f1", "user1", nil)
	require.NoError(t, err)
	assert.True(t, enabled)

	// EvaluateBatch
	entities := []flagent.EvaluationEntity{{EntityID: "u1"}}
	results, err := client.EvaluateBatch(ctx, []string{"f1", "f2"}, entities)
	require.NoError(t, err)
	require.Len(t, results, 2)
	assert.Equal(t, "f1", results[0].FlagKey)
	assert.Equal(t, "u1", results[0].EntityID)
	assert.Equal(t, "f2", results[1].FlagKey)
}

func TestNewFlagent_ServerMode_WithOptions(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
		if auth := req.Header.Get("Authorization"); auth != "" {
			assert.Equal(t, "Bearer sk-test", auth)
		}
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(&api.EvalResult{})
	}))
	defer server.Close()

	ctx := context.Background()
	opts := DefaultOptions()
	opts.Offline = false
	opts.APIKey = "sk-test"
	opts.Timeout = 5 * time.Second
	opts.EnableCache = true
	opts.CacheTTL = 2 * time.Minute

	client, err := NewFlagent(ctx, server.URL, opts)
	require.NoError(t, err)
	require.NotNil(t, client)
	defer client.Close()

	_, err = client.Evaluate(ctx, "f", "e", nil)
	require.NoError(t, err)
}

func TestNewFlagent_OfflineMode(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		fs := flagent.FlagSnapshot{
			Flags: []api.Flag{
				{
					Id:      1,
					Key:     "offline_flag",
					Enabled: true,
					Segments: []api.Segment{{
						Id:             1,
						FlagID:         1,
						Rank:           1,
						RolloutPercent: 100,
						Constraints:    []api.Constraint{},
						Distributions: []api.Distribution{
							{Id: 1, VariantID: 1, VariantKey: *api.NewNullableString(api.PtrString("on")), Percent: 100},
						},
					}},
					Variants: []api.Variant{{Id: 1, FlagID: 1, Key: "on"}},
				},
			},
		}
		json.NewEncoder(w).Encode(fs)
	}))
	defer server.Close()

	ctx := context.Background()
	opts := DefaultOptions()
	opts.Offline = true
	opts.EnablePersistence = false

	client, err := NewFlagent(ctx, server.URL, opts)
	require.NoError(t, err)
	require.NotNil(t, client)
	defer client.Close()

	res, err := client.Evaluate(ctx, "offline_flag", "entity1", nil)
	require.NoError(t, err)
	require.NotNil(t, res)
	assert.True(t, res.Enabled)
	assert.Equal(t, "offline_flag", res.FlagKey)
	assert.Equal(t, "on", res.VariantKey)

	enabled, err := client.IsEnabled(ctx, "offline_flag", "entity1", nil)
	require.NoError(t, err)
	assert.True(t, enabled)

	// Batch
	entities := []flagent.EvaluationEntity{{EntityID: "e1"}, {EntityID: "e2"}}
	results, err := client.EvaluateBatch(ctx, []string{"offline_flag"}, entities)
	require.NoError(t, err)
	require.Len(t, results, 2)
	assert.Equal(t, "offline_flag", results[0].FlagKey)
	assert.Equal(t, "e1", results[0].EntityID)
	assert.True(t, results[0].Enabled)
	assert.Equal(t, "e2", results[1].EntityID)
}

func TestNewFlagent_OfflineMode_BootstrapFromCache(t *testing.T) {
	storage := NewInMemorySnapshotStorage()
	storage.Save(makeOfflineSnapshot())

	callCount := 0
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		callCount++
		t.Error("should not call server when bootstrap from cache")
	}))
	defer server.Close()

	ctx := context.Background()
	opts := DefaultOptions()
	opts.Offline = true
	opts.EnablePersistence = false

	baseClient, _ := flagent.NewClient(server.URL)
	om := NewOfflineManager(baseClient, DefaultOfflineConfig().WithPersistence(false))
	om.storage = storage
	err := om.Bootstrap(ctx, false)
	require.NoError(t, err)

	// Build client manually with pre-bootstrapped manager to simulate cache load
	client := &offlineClientAdapter{om: om}
	defer client.Close()

	res, err := client.Evaluate(ctx, "test_flag", "user1", nil)
	require.NoError(t, err)
	assert.True(t, res.Enabled)
	assert.Equal(t, 0, callCount)
}

func TestFlagentClient_Server_EvalResultFields(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		evalRes := &api.EvalResult{}
		evalRes.SetFlagKey("disabled_flag")
		evalRes.VariantKey = *api.NewNullableString(api.PtrString(""))
		json.NewEncoder(w).Encode(evalRes)
	}))
	defer server.Close()

	ctx := context.Background()
	client, err := NewFlagent(ctx, server.URL, DefaultOptions())
	require.NoError(t, err)
	defer client.Close()

	res, err := client.Evaluate(ctx, "disabled_flag", "u", nil)
	require.NoError(t, err)
	assert.False(t, res.Enabled)
	assert.Equal(t, "disabled_flag", res.FlagKey)
	assert.Equal(t, "u", res.EntityID)
}

func TestFlagentClient_Close_Idempotent(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(&api.EvalResult{})
	}))
	defer server.Close()

	client, _ := NewFlagent(context.Background(), server.URL, DefaultOptions())
	client.Close()
	client.Close()
}

func TestDefaultOptions(t *testing.T) {
	o := DefaultOptions()
	assert.False(t, o.Offline)
	assert.True(t, o.EnableCache)
	assert.Equal(t, 5*time.Minute, o.CacheTTL)
	assert.True(t, o.EnablePersistence)
	assert.True(t, o.AutoRefresh)
}
