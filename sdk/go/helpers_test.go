package flagent

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestHelpers(t *testing.T) {
	t.Run("StringPtr", func(t *testing.T) {
		p := StringPtr("hello")
		require.NotNil(t, p)
		assert.Equal(t, "hello", *p)
	})

	t.Run("Int64Ptr", func(t *testing.T) {
		p := Int64Ptr(42)
		require.NotNil(t, p)
		assert.Equal(t, int64(42), *p)
	})

	t.Run("BoolPtr", func(t *testing.T) {
		p := BoolPtr(true)
		require.NotNil(t, p)
		assert.True(t, *p)
	})

	t.Run("IntPtr", func(t *testing.T) {
		p := IntPtr(100)
		require.NotNil(t, p)
		assert.Equal(t, 100, *p)
	})
}

func TestFlagentError_Error(t *testing.T) {
	t.Run("without inner error", func(t *testing.T) {
		err := NewEvaluationError("failed", nil)
		assert.Equal(t, "failed", err.Error())
	})

	t.Run("with inner error", func(t *testing.T) {
		inner := errors.New("connection refused")
		err := NewEvaluationError("wrapper", inner)
		assert.Contains(t, err.Error(), "wrapper")
		assert.Contains(t, err.Error(), "connection refused")
	})
}
