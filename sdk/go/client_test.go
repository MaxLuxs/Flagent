package flagent

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestNewClient(t *testing.T) {
	t.Run("valid config", func(t *testing.T) {
		client, err := NewClient("http://localhost:18000/api/v1")
		require.NoError(t, err)
		require.NotNil(t, client)
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
		require.NotNil(t, client)
	})
}

func TestEvaluate(t *testing.T) {
	t.Run("successful evaluation", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/evaluation", r.URL.Path)
			assert.Equal(t, http.MethodPost, r.Method)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			result := api.EvalResult{}
			result.SetFlagKey("test_flag")
			result.VariantKey = *api.NewNullableString(api.PtrString("control"))
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
		assert.Equal(t, "test_flag", result.GetFlagKey())
		assert.Equal(t, "control", result.GetVariantKey())
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
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			res1 := api.EvalResult{}
			res1.SetFlagKey("flag_a")
			res1.VariantKey = *api.NewNullableString(api.PtrString("control"))
			res2 := api.EvalResult{}
			res2.SetFlagKey("flag_b")
			res2.VariantKey = *api.NewNullableString(api.PtrString("treatment"))
			response := api.EvaluationBatchResponse{
				EvaluationResults: []api.EvalResult{res1, res2},
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
		assert.Equal(t, "flag_a", results[0].GetFlagKey())
		assert.Equal(t, "flag_b", results[1].GetFlagKey())
	})
}

func TestGetFlag(t *testing.T) {
	t.Run("successful get flag", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/flags/1", r.URL.Path)
			assert.Equal(t, http.MethodGet, r.Method)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			flag := api.Flag{
				Id:          1,
				Key:         "test_flag",
				Description: "",
				Enabled:     true,
				DataRecordsEnabled: false,
			}
			json.NewEncoder(w).Encode(flag)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		flag, err := client.GetFlag(ctx, 1)
		require.NoError(t, err)
		assert.Equal(t, int64(1), flag.Id)
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
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			flags := []api.Flag{
				{Id: 1, Key: "flag_a", Description: "", Enabled: true, DataRecordsEnabled: false},
				{Id: 2, Key: "flag_b", Description: "", Enabled: false, DataRecordsEnabled: false},
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
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			health := api.Health{}
			health.SetStatus("ok")
			json.NewEncoder(w).Encode(health)
		}))
		defer server.Close()

		client, err := NewClient(server.URL)
		require.NoError(t, err)

		ctx := context.Background()
		health, err := client.HealthCheck(ctx)
		require.NoError(t, err)
		assert.Equal(t, "ok", health.GetStatus())
	})
}

func TestGetSnapshot(t *testing.T) {
	t.Run("successful get snapshot", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/export/eval_cache/json", r.URL.Path)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			snapshot := FlagSnapshot{
				Flags: []api.Flag{
					{Id: 1, Key: "flag_a", Description: "", Enabled: true, DataRecordsEnabled: false},
					{Id: 2, Key: "flag_b", Description: "", Enabled: false, DataRecordsEnabled: false},
				},
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
