package flagentenhanced

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sync"
)

// SnapshotStorage is an interface for storing and loading snapshots
type SnapshotStorage interface {
	// Save saves a snapshot to storage
	Save(snapshot *FlagSnapshot) error

	// Load loads a snapshot from storage
	Load() (*FlagSnapshot, error)

	// Clear removes all stored snapshots
	Clear() error
}

// InMemorySnapshotStorage is an in-memory snapshot storage
type InMemorySnapshotStorage struct {
	mu       sync.RWMutex
	snapshot *FlagSnapshot
}

// NewInMemorySnapshotStorage creates a new in-memory snapshot storage
func NewInMemorySnapshotStorage() *InMemorySnapshotStorage {
	return &InMemorySnapshotStorage{}
}

// Save saves a snapshot to memory
func (s *InMemorySnapshotStorage) Save(snapshot *FlagSnapshot) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.snapshot = snapshot
	return nil
}

// Load loads a snapshot from memory
func (s *InMemorySnapshotStorage) Load() (*FlagSnapshot, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if s.snapshot == nil {
		return nil, nil
	}

	return s.snapshot, nil
}

// Clear removes the stored snapshot
func (s *InMemorySnapshotStorage) Clear() error {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.snapshot = nil
	return nil
}

// FileSnapshotStorage is a file-based snapshot storage
type FileSnapshotStorage struct {
	mu      sync.RWMutex
	dirPath string
	filename string
}

// NewFileSnapshotStorage creates a new file-based snapshot storage
func NewFileSnapshotStorage(dirPath string) *FileSnapshotStorage {
	return &FileSnapshotStorage{
		dirPath:  dirPath,
		filename: "snapshot.json",
	}
}

// Save saves a snapshot to file
func (s *FileSnapshotStorage) Save(snapshot *FlagSnapshot) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	// Create directory if it doesn't exist
	if err := os.MkdirAll(s.dirPath, 0755); err != nil {
		return fmt.Errorf("failed to create storage directory: %w", err)
	}

	// Serialize snapshot to JSON
	data, err := json.Marshal(snapshot)
	if err != nil {
		return fmt.Errorf("failed to marshal snapshot: %w", err)
	}

	// Write to file
	filePath := filepath.Join(s.dirPath, s.filename)
	if err := os.WriteFile(filePath, data, 0644); err != nil {
		return fmt.Errorf("failed to write snapshot file: %w", err)
	}

	return nil
}

// Load loads a snapshot from file
func (s *FileSnapshotStorage) Load() (*FlagSnapshot, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	filePath := filepath.Join(s.dirPath, s.filename)

	// Check if file exists
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		return nil, nil // No cached snapshot
	}

	// Read file
	data, err := os.ReadFile(filePath)
	if err != nil {
		return nil, fmt.Errorf("failed to read snapshot file: %w", err)
	}

	// Deserialize snapshot
	var snapshot FlagSnapshot
	if err := json.Unmarshal(data, &snapshot); err != nil {
		return nil, fmt.Errorf("failed to unmarshal snapshot: %w", err)
	}

	return &snapshot, nil
}

// Clear removes the snapshot file
func (s *FileSnapshotStorage) Clear() error {
	s.mu.Lock()
	defer s.mu.Unlock()

	filePath := filepath.Join(s.dirPath, s.filename)

	// Check if file exists
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		return nil // Nothing to clear
	}

	// Remove file
	if err := os.Remove(filePath); err != nil {
		return fmt.Errorf("failed to remove snapshot file: %w", err)
	}

	return nil
}
