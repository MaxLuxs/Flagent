package flagent

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestNewClient(t *testing.T) {
	t.Run("valid config", func(t *testing.T) {
		client, err := NewClient("http://localhost:18000/api/v1")
		require.NoError(t, err)
		require.NotNil(t, client)
		assert.Equal(t, "http://localhost:18000/api/v1", client.baseURL)
	})

	t.Run("empty baseURL", func(t *testing.T) {
		_, err := NewClient("")
		require.Error(t, err)
		assert.IsType(t, &InvalidConfigError{}, err)
	})

	t.Run("with options", func(t *testing.T) {
		client, err := NewClient(
			"http://localhost:18000/api/v1",
			WithAPIKey("test-key"),
			WithTimeout(10*time.Second),
			WithMaxRetries(5),
		)
		require.NoError(t, err)
		assert.Equal(t, "test-key", client.apiKey)
		assert.Equal(t, 10*time.Second, client.httpClient.Timeout)
		assert.Equal(t, 5, client.maxRetries)
	})
}

func TestEvaluate(t *testing.T) {
	t.Run("successful evaluation", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/evaluation", r.URL.Path)
			assert.Equal(t, http.MethodPost, r.Method)

			result := &EvaluationResult{
				FlagKey:    stringPtr("test_flag"),
				VariantKey: stringPtr("control"),
			}
			json.NewEncoder(w).Encode(result)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		evalCtx := &EvaluationContext{
			FlagKey:  stringPtr("test_flag"),
			EntityID: stringPtr("user123"),
		}

		result, err := client.Evaluate(ctx, evalCtx)
		require.NoError(t, err)
		assert.Equal(t, "test_flag", *result.FlagKey)
		assert.Equal(t, "control", *result.VariantKey)
		assert.True(t, result.IsEnabled())
	})

	t.Run("flag not found", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNotFound)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		evalCtx := &EvaluationContext{
			FlagKey:  stringPtr("nonexistent"),
			EntityID: stringPtr("user123"),
		}

		_, err = client.Evaluate(ctx, evalCtx)
		require.Error(t, err)
		assert.IsType(t, &FlagNotFoundError{}, err)
	})
}

func TestEvaluateBatch(t *testing.T) {
	t.Run("successful batch evaluation", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/evaluation/batch", r.URL.Path)
			assert.Equal(t, http.MethodPost, r.Method)

			response := &BatchEvaluationResponse{
				EvaluationResults: []*EvaluationResult{
					{
						FlagKey:    stringPtr("flag_a"),
						VariantKey: stringPtr("control"),
						EntityID:   stringPtr("user1"),
					},
					{
						FlagKey:    stringPtr("flag_b"),
						VariantKey: stringPtr("treatment"),
						EntityID:   stringPtr("user2"),
					},
				},
			}
			json.NewEncoder(w).Encode(response)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		req := &BatchEvaluationRequest{
			Entities: []EvaluationEntity{
				{EntityID: "user1"},
				{EntityID: "user2"},
			},
			FlagKeys: []string{"flag_a", "flag_b"},
		}

		results, err := client.EvaluateBatch(ctx, req)
		require.NoError(t, err)
		assert.Len(t, results, 2)
		assert.Equal(t, "flag_a", *results[0].FlagKey)
		assert.Equal(t, "flag_b", *results[1].FlagKey)
	})
}

func TestGetFlag(t *testing.T) {
	t.Run("successful get flag", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/flags/1", r.URL.Path)
			assert.Equal(t, http.MethodGet, r.Method)

			flag := &Flag{
				ID:      int64Ptr(1),
				Key:     "test_flag",
				Enabled: true,
			}
			json.NewEncoder(w).Encode(flag)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		flag, err := client.GetFlag(ctx, 1)
		require.NoError(t, err)
		assert.Equal(t, int64(1), *flag.ID)
		assert.Equal(t, "test_flag", flag.Key)
		assert.True(t, flag.Enabled)
	})

	t.Run("flag not found", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNotFound)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		_, err = client.GetFlag(ctx, 999)
		require.Error(t, err)
		assert.IsType(t, &FlagNotFoundError{}, err)
	})
}

func TestListFlags(t *testing.T) {
	t.Run("successful list flags", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/flags", r.URL.Path)
			assert.Equal(t, http.MethodGet, r.Method)

			flags := []*Flag{
				{ID: int64Ptr(1), Key: "flag_a", Enabled: true},
				{ID: int64Ptr(2), Key: "flag_b", Enabled: false},
			}
			json.NewEncoder(w).Encode(flags)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		flags, err := client.ListFlags(ctx, nil)
		require.NoError(t, err)
		assert.Len(t, flags, 2)
		assert.Equal(t, "flag_a", flags[0].Key)
		assert.Equal(t, "flag_b", flags[1].Key)
	})
}

func TestHealthCheck(t *testing.T) {
	t.Run("successful health check", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/health", r.URL.Path)
			json.NewEncoder(w).Encode(&Health{Status: "ok"})
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		health, err := client.HealthCheck(ctx)
		require.NoError(t, err)
		assert.Equal(t, "ok", health.Status)
	})
}

func TestGetSnapshot(t *testing.T) {
	t.Run("successful get snapshot", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/export/eval_cache/json", r.URL.Path)
			
			snapshot := &FlagSnapshot{
				Flags: []*Flag{
					{Key: "flag_a", Enabled: true},
					{Key: "flag_b", Enabled: false},
				},
				ExportAt: time.Now().Unix(),
			}
			json.NewEncoder(w).Encode(snapshot)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		snapshot, err := client.GetSnapshot(ctx)
		require.NoError(t, err)
		assert.Len(t, snapshot.Flags, 2)
		assert.Equal(t, "flag_a", snapshot.Flags[0].Key)
	})
}

// Helper functions
func stringPtr(s string) *string {
	return &s
}

func int64Ptr(i int64) *int64 {
	return &i
}
