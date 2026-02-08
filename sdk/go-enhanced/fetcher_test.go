package flagentenhanced

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
	flagent "github.com/MaxLuxs/Flagent/sdk/go"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestSnapshotFetcher(t *testing.T) {
	t.Run("NewSnapshotFetcher", func(t *testing.T) {
		client, _ := flagent.NewClient("http://localhost:18000/api/v1")
		f := NewSnapshotFetcher(client)
		require.NotNil(t, f)
	})

	t.Run("FetchSnapshot success", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			assert.Equal(t, "/export/eval_cache/json", r.URL.Path)
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			snap := flagent.FlagSnapshot{
				Flags: []api.Flag{
					{
						Id:       1,
						Key:      "test_flag",
						Enabled:  true,
						Segments: []api.Segment{},
						Variants: []api.Variant{{Id: 1, Key: "control", FlagID: 1}},
					},
				},
			}
			json.NewEncoder(w).Encode(snap)
		}))
		defer server.Close()

		client, err := flagent.NewClient(server.URL)
		require.NoError(t, err)

		f := NewSnapshotFetcher(client)
		ctx := context.Background()

		snap, err := f.FetchSnapshot(ctx, 60000)
		require.NoError(t, err)
		require.NotNil(t, snap)
		assert.Len(t, snap.Flags, 1)
		assert.Equal(t, int64(1), snap.Flags[1].ID)
		assert.Equal(t, "test_flag", snap.Flags[1].Key)
	})

	t.Run("FetchSnapshot error", func(t *testing.T) {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusInternalServerError)
		}))
		defer server.Close()

		client, err := flagent.NewClient(server.URL)
		require.NoError(t, err)

		f := NewSnapshotFetcher(client)
		ctx := context.Background()

		_, err = f.FetchSnapshot(ctx, 60000)
		require.Error(t, err)
	})
}
