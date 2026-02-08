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

func makeEvalResult(flagKey, variantKey string) *api.EvalResult {
	r := &api.EvalResult{}
	r.SetFlagKey(flagKey)
	r.VariantKey = *api.NewNullableString(&variantKey)
	return r
}

func TestNewManager(t *testing.T) {
	client, _ := flagent.NewClient("http://localhost:18000/api/v1")

	t.Run("with default config", func(t *testing.T) {
		manager := NewManager(client, nil)
		require.NotNil(t, manager)
		assert.NotNil(t, manager.config)
		assert.NotNil(t, manager.cache)
	})

	t.Run("with custom config", func(t *testing.T) {
		config := DefaultConfig().
			WithCacheTTL(10 * time.Minute).
			WithDebugLogging(true)

		manager := NewManager(client, config)
		require.NotNil(t, manager)
		assert.Equal(t, 10*time.Minute, manager.config.CacheTTL)
		assert.True(t, manager.config.EnableDebugLogging)
	})
}

func TestManagerEvaluate(t *testing.T) {
	t.Run("successful evaluation with caching", func(t *testing.T) {
		callCount := 0
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			callCount++
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			json.NewEncoder(w).Encode(makeEvalResult("test_flag", "control"))
		}))
		defer server.Close()

		client, err := flagent.NewClient(server.URL)
		require.NoError(t, err)

		config := DefaultConfig().WithCacheTTL(1 * time.Minute)
		manager := NewManager(client, config)

		ctx := context.Background()

		// First call - should hit server
		result1, err := manager.Evaluate(ctx, "test_flag", "user123", nil)
		require.NoError(t, err)
		assert.Equal(t, "test_flag", *result1.FlagKey)
		assert.Equal(t, 1, callCount)

		// Second call - should use cache
		result2, err := manager.Evaluate(ctx, "test_flag", "user123", nil)
		require.NoError(t, err)
		assert.Equal(t, "test_flag", *result2.FlagKey)
		assert.Equal(t, 1, callCount) // Should not increment
	})

	t.Run("cache expiration", func(t *testing.T) {
		callCount := 0
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			callCount++
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			json.NewEncoder(w).Encode(makeEvalResult("test_flag", "control"))
		}))
		defer server.Close()

		client, err := flagent.NewClient(server.URL)
		require.NoError(t, err)

		config := DefaultConfig().WithCacheTTL(100 * time.Millisecond)
		manager := NewManager(client, config)

		ctx := context.Background()

		// First call
		_, err = manager.Evaluate(ctx, "test_flag", "user123", nil)
		require.NoError(t, err)
		assert.Equal(t, 1, callCount)

		// Wait for cache to expire
		time.Sleep(150 * time.Millisecond)

		// Second call - cache expired, should hit server again
		_, err = manager.Evaluate(ctx, "test_flag", "user123", nil)
		require.NoError(t, err)
		assert.Equal(t, 2, callCount)
	})
}

func TestManagerIsEnabled(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(makeEvalResult("test_flag", "control"))
	}))
	defer server.Close()

	client, err := flagent.NewClient(server.URL)
	require.NoError(t, err)

	manager := NewManager(client, DefaultConfig())
	ctx := context.Background()

	enabled, err := manager.IsEnabled(ctx, "test_flag", "user123", nil)
	require.NoError(t, err)
	assert.True(t, enabled)
}

func TestManagerGetVariant(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(makeEvalResult("test_flag", "treatment"))
	}))
	defer server.Close()

	client, err := flagent.NewClient(server.URL)
	require.NoError(t, err)

	manager := NewManager(client, DefaultConfig())
	ctx := context.Background()

	variant, err := manager.GetVariant(ctx, "test_flag", "user123", nil)
	require.NoError(t, err)
	assert.Equal(t, "treatment", variant)
}

func TestManagerClearCache(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(makeEvalResult("test_flag", "control"))
	}))
	defer server.Close()

	client, err := flagent.NewClient(server.URL)
	require.NoError(t, err)

	manager := NewManager(client, DefaultConfig())
	ctx := context.Background()

	// Populate cache
	_, err = manager.Evaluate(ctx, "test_flag", "user123", nil)
	require.NoError(t, err)

	// Clear cache
	manager.ClearCache()

	// Cache should be empty
	cache := manager.cache.(*InMemoryCache)
	assert.Equal(t, 0, cache.Size())
}

func TestManagerEvictExpired(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(makeEvalResult("test_flag", "control"))
	}))
	defer server.Close()

	client, err := flagent.NewClient(server.URL)
	require.NoError(t, err)

	config := DefaultConfig().WithCacheTTL(100 * time.Millisecond)
	manager := NewManager(client, config)
	ctx := context.Background()

	// Populate cache
	_, err = manager.Evaluate(ctx, "test_flag", "user123", nil)
	require.NoError(t, err)

	cache := manager.cache.(*InMemoryCache)
	assert.Equal(t, 1, cache.Size())

	// Wait for expiration
	time.Sleep(150 * time.Millisecond)

	// Evict expired
	manager.EvictExpired()

	// Cache should be empty
	assert.Equal(t, 0, cache.Size())
}
