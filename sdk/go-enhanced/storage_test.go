package flagentenhanced

import (
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestInMemorySnapshotStorage(t *testing.T) {
	t.Run("Save and Load", func(t *testing.T) {
		s := NewInMemorySnapshotStorage()
		snap := &FlagSnapshot{
			Flags:    map[int64]*LocalFlag{1: {ID: 1, Key: "f1"}},
			FetchedAt: time.Now().UnixMilli(),
			TTLMs:    60000,
		}

		err := s.Save(snap)
		require.NoError(t, err)

		loaded, err := s.Load()
		require.NoError(t, err)
		require.NotNil(t, loaded)
		assert.Equal(t, int64(1), loaded.Flags[1].ID)
	})

	t.Run("Load when empty returns nil", func(t *testing.T) {
		s := NewInMemorySnapshotStorage()
		loaded, err := s.Load()
		require.NoError(t, err)
		assert.Nil(t, loaded)
	})

	t.Run("Clear", func(t *testing.T) {
		s := NewInMemorySnapshotStorage()
		s.Save(&FlagSnapshot{Flags: map[int64]*LocalFlag{1: {ID: 1}}})
		err := s.Clear()
		require.NoError(t, err)
		loaded, _ := s.Load()
		assert.Nil(t, loaded)
	})
}

func TestFileSnapshotStorage(t *testing.T) {
	dir := t.TempDir()

	t.Run("Save and Load", func(t *testing.T) {
		s := NewFileSnapshotStorage(dir)
		snap := &FlagSnapshot{
			Flags:    map[int64]*LocalFlag{1: {ID: 1, Key: "f1"}},
			FetchedAt: time.Now().UnixMilli(),
			TTLMs:    60000,
		}

		err := s.Save(snap)
		require.NoError(t, err)

		loaded, err := s.Load()
		require.NoError(t, err)
		require.NotNil(t, loaded)
		assert.Equal(t, int64(1), loaded.Flags[1].ID)
	})

	t.Run("Load when file not exists returns nil", func(t *testing.T) {
		emptyDir := t.TempDir()
		s := NewFileSnapshotStorage(emptyDir)
		loaded, err := s.Load()
		require.NoError(t, err)
		assert.Nil(t, loaded)
	})

	t.Run("Clear removes file", func(t *testing.T) {
		s := NewFileSnapshotStorage(dir)
		s.Save(&FlagSnapshot{Flags: map[int64]*LocalFlag{}})
		path := filepath.Join(dir, "snapshot.json")
		require.FileExists(t, path)

		err := s.Clear()
		require.NoError(t, err)
		_, err = os.Stat(path)
		assert.True(t, os.IsNotExist(err))
	})
}
