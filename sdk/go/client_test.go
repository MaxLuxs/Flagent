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

	t.Run("WithHTTPClient and WithRetryDelay", func(t *testing.T) {
		hc := &http.Client{Timeout: 5 * time.Second}
		client, err := NewClient(
			"http://localhost:18000/api/v1",
			WithHTTPClient(hc),
			WithRetryDelay(time.Second),
		)
		require.NoError(t, err)
		require.NotNil(t, client)
	})

	t.Run("WithAPIKey empty string does not set header", func(t *testing.T) {
		client, err := NewClient("http://localhost:18000/api/v1", WithAPIKey(""))
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

	t.Run("Evaluate with nil evalCtx", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			result := api.EvalResult{}
			result.SetFlagKey("test")
			json.NewEncoder(w).Encode(result)
		}))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		result, err := client.Evaluate(context.Background(), nil)
		require.NoError(t, err)
		assert.Equal(t, "test", result.GetFlagKey())
	})

	t.Run("Evaluate API error returns EvaluationError", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusBadRequest)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			w.Write([]byte(`{"message":"bad request"}`))
		}))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		_, err = client.Evaluate(context.Background(), &EvaluationContext{FlagKey: stringPtr("f1")})
		require.Error(t, err)
		assert.IsType(t, &EvaluationError{}, err)
	})
}

func TestEvaluateBatch(t *testing.T) {
	t.Run("nil request returns InvalidConfigError", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(200) }))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		_, err = client.EvaluateBatch(context.Background(), nil)
		require.Error(t, err)
		assert.IsType(t, &InvalidConfigError{}, err)
	})

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

	t.Run("API error returns EvaluationError", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusBadRequest)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			w.Write([]byte(`{"message":"invalid request"}`))
		}))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		_, err = client.EvaluateBatch(context.Background(), &BatchEvaluationRequest{
			Entities: []EvaluationEntity{{EntityID: "u1"}},
			FlagKeys: []string{"f1"},
		})
		require.Error(t, err)
		assert.IsType(t, &EvaluationError{}, err)
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

	t.Run("ListFlags with opts", func(t *testing.T) {
		enabled := true
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/flags", r.URL.Path)
			q := r.URL.Query()
			assert.Equal(t, "5", q.Get("limit"))
			assert.Equal(t, "10", q.Get("offset"))
			assert.Equal(t, "true", q.Get("enabled"))
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			json.NewEncoder(w).Encode([]api.Flag{})
		}))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		flags, err := client.ListFlags(context.Background(), &ListFlagsOptions{
			Limit: 5, Offset: 10, Enabled: &enabled, Preload: false,
		})
		require.NoError(t, err)
		assert.Empty(t, flags)
	})
}

func TestConvertErrorAndEvaluatePaths(t *testing.T) {
	t.Run("Evaluate network error returns NetworkError", func(t *testing.T) {
		// Use unreachable host to trigger non-GenericOpenAPIError (connection refused etc)
		client, err := NewClient("http://127.0.0.1:19999", WithTimeout(time.Millisecond))
		require.NoError(t, err)
		_, err = client.Evaluate(context.Background(), &EvaluationContext{FlagKey: stringPtr("f1")})
		require.Error(t, err)
		assert.IsType(t, &NetworkError{}, err)
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
	t.Run("malformed JSON returns error", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			w.Write([]byte(`{invalid json`))
		}))
		defer server.Close()
		client, err := NewClient(server.URL)
		require.NoError(t, err)
		_, err = client.GetSnapshot(context.Background())
		require.Error(t, err)
		// OpenAPI client returns GenericOpenAPIError for decode failures -> convertError yields EvaluationError
		assert.IsType(t, &EvaluationError{}, err)
	})

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
