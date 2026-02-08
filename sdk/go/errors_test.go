package flagent

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestErrorTypes(t *testing.T) {
	t.Run("FlagNotFoundError", func(t *testing.T) {
		err := NewFlagNotFoundError("flag not found", nil)
		assert.Error(t, err)
		assert.IsType(t, &FlagNotFoundError{}, err)
		var fnf *FlagNotFoundError
		assert.True(t, errors.As(err, &fnf))
	})

	t.Run("EvaluationError", func(t *testing.T) {
		err := NewEvaluationError("eval failed", nil)
		assert.Error(t, err)
		assert.IsType(t, &EvaluationError{}, err)
	})

	t.Run("NetworkError", func(t *testing.T) {
		err := NewNetworkError("connection refused", nil)
		assert.Error(t, err)
		assert.IsType(t, &NetworkError{}, err)
	})

	t.Run("InvalidConfigError", func(t *testing.T) {
		err := NewInvalidConfigError("invalid config", nil)
		assert.Error(t, err)
		assert.IsType(t, &InvalidConfigError{}, err)
	})

	t.Run("FlagentError unwrap", func(t *testing.T) {
		inner := errors.New("inner error")
		err := NewEvaluationError("wrapper", inner)
		assert.True(t, errors.Is(err, inner))
	})
}
