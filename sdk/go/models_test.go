package flagent

import (
	"testing"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
	"github.com/stretchr/testify/assert"
)

func TestEvaluationResult_IsEnabled(t *testing.T) {
	t.Run("enabled when VariantKey set", func(t *testing.T) {
		v := "control"
		r := &EvaluationResult{
			EvalResult: &api.EvalResult{},
			VariantKey: &v,
		}
		assert.True(t, r.IsEnabled())
	})

	t.Run("disabled when VariantKey nil", func(t *testing.T) {
		r := &EvaluationResult{EvalResult: &api.EvalResult{}, VariantKey: nil}
		assert.False(t, r.IsEnabled())
	})

	t.Run("disabled when VariantKey empty", func(t *testing.T) {
		v := ""
		r := &EvaluationResult{EvalResult: &api.EvalResult{}, VariantKey: &v}
		assert.False(t, r.IsEnabled())
	})
}

func TestEvaluationResult_GetAttachmentValue(t *testing.T) {
	t.Run("returns value when key exists", func(t *testing.T) {
		r := &EvaluationResult{
			EvalResult: &api.EvalResult{
				VariantAttachment: map[string]interface{}{"color": "red"},
			},
		}
		assert.Equal(t, "red", r.GetAttachmentValue("color", "default"))
	})

	t.Run("returns default when key missing", func(t *testing.T) {
		r := &EvaluationResult{
			EvalResult: &api.EvalResult{
				VariantAttachment: map[string]interface{}{"color": "red"},
			},
		}
		assert.Equal(t, "default", r.GetAttachmentValue("missing", "default"))
	})

	t.Run("returns default when nil", func(t *testing.T) {
		var r *EvaluationResult
		assert.Equal(t, "default", r.GetAttachmentValue("key", "default"))
	})

	t.Run("returns default when EvalResult nil", func(t *testing.T) {
		r := &EvaluationResult{EvalResult: nil}
		assert.Equal(t, "default", r.GetAttachmentValue("key", "default"))
	})
}
