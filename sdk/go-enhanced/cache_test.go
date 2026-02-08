package flagentenhanced

import (
	"testing"
	"time"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
	flagent "github.com/MaxLuxs/Flagent/sdk/go"
	"github.com/stretchr/testify/assert"
)

func TestInMemoryCache_Delete(t *testing.T) {
	c := NewInMemoryCache()
	v := "control"
	r := &flagent.EvaluationResult{
		EvalResult: &api.EvalResult{},
		VariantKey: &v,
	}

	c.Set("key1", r, time.Minute)
	assert.Equal(t, 1, c.Size())

	c.Delete("key1")
	assert.Equal(t, 0, c.Size())

	_, ok := c.Get("key1")
	assert.False(t, ok)
}
