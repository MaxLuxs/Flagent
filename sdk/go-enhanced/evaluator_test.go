package flagentenhanced

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestLocalEvaluator_Evaluate(t *testing.T) {
	evaluator := NewLocalEvaluator()

	t.Run("flag not found", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			Flags: make(map[int64]*LocalFlag),
		}

		flagKey := "nonexistent"
		req := &OfflineEvaluationRequest{
			FlagKey:  &flagKey,
			EntityID: "user123",
		}

		result := evaluator.Evaluate(req, snapshot)
		assert.Equal(t, "FLAG_NOT_FOUND", result.Reason)
		assert.False(t, result.IsEnabled())
	})

	t.Run("flag disabled", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			Flags: map[int64]*LocalFlag{
				1: {
					ID:      1,
					Key:     "test_flag",
					Enabled: false,
				},
			},
		}

		flagKey := "test_flag"
		req := &OfflineEvaluationRequest{
			FlagKey:  &flagKey,
			EntityID: "user123",
		}

		result := evaluator.Evaluate(req, snapshot)
		assert.Equal(t, "FLAG_DISABLED", result.Reason)
		assert.False(t, result.IsEnabled())
	})

	t.Run("no segments", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			Flags: map[int64]*LocalFlag{
				1: {
					ID:       1,
					Key:      "test_flag",
					Enabled:  true,
					Segments: []*LocalSegment{},
				},
			},
		}

		flagKey := "test_flag"
		req := &OfflineEvaluationRequest{
			FlagKey:  &flagKey,
			EntityID: "user123",
		}

		result := evaluator.Evaluate(req, snapshot)
		assert.Equal(t, "NO_SEGMENTS", result.Reason)
		assert.False(t, result.IsEnabled())
	})

	t.Run("successful evaluation with constraints", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			Flags: map[int64]*LocalFlag{
				1: {
					ID:      1,
					Key:     "test_flag",
					Enabled: true,
					Segments: []*LocalSegment{
						{
							ID:             1,
							FlagID:         1,
							Rank:           1,
							RolloutPercent: 100,
							Constraints: []*LocalConstraint{
								{
									ID:       1,
									Property: "tier",
									Operator: "EQ",
									Value:    "premium",
								},
							},
							Distributions: []*LocalDistribution{
								{
									ID:         1,
									VariantID:  10,
									VariantKey: "treatment",
									Percent:    100,
								},
							},
						},
					},
					Variants: []*LocalVariant{
						{
							ID:  10,
							Key: "treatment",
						},
					},
				},
			},
		}

		flagKey := "test_flag"
		req := &OfflineEvaluationRequest{
			FlagKey:  &flagKey,
			EntityID: "user123",
			EntityContext: map[string]interface{}{
				"tier": "premium",
			},
		}

		result := evaluator.Evaluate(req, snapshot)
		assert.Equal(t, "MATCH", result.Reason)
		assert.True(t, result.IsEnabled())
		assert.Equal(t, "treatment", *result.VariantKey)
	})

	t.Run("constraints do not match", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			Flags: map[int64]*LocalFlag{
				1: {
					ID:      1,
					Key:     "test_flag",
					Enabled: true,
					Segments: []*LocalSegment{
						{
							ID:             1,
							FlagID:         1,
							Rank:           1,
							RolloutPercent: 100,
							Constraints: []*LocalConstraint{
								{
									ID:       1,
									Property: "tier",
									Operator: "EQ",
									Value:    "premium",
								},
							},
						},
					},
				},
			},
		}

		flagKey := "test_flag"
		req := &OfflineEvaluationRequest{
			FlagKey:  &flagKey,
			EntityID: "user123",
			EntityContext: map[string]interface{}{
				"tier": "free",
			},
		}

		result := evaluator.Evaluate(req, snapshot)
		assert.Equal(t, "NO_MATCH", result.Reason)
		assert.False(t, result.IsEnabled())
	})
}

func TestLocalEvaluator_EvaluateConstraint(t *testing.T) {
	evaluator := NewLocalEvaluator()

	tests := []struct {
		name       string
		constraint *LocalConstraint
		context    map[string]interface{}
		expected   bool
	}{
		{
			name: "EQ operator - match",
			constraint: &LocalConstraint{
				Property: "tier",
				Operator: "EQ",
				Value:    "premium",
			},
			context:  map[string]interface{}{"tier": "premium"},
			expected: true,
		},
		{
			name: "EQ operator - no match",
			constraint: &LocalConstraint{
				Property: "tier",
				Operator: "EQ",
				Value:    "premium",
			},
			context:  map[string]interface{}{"tier": "free"},
			expected: false,
		},
		{
			name: "IN operator - match",
			constraint: &LocalConstraint{
				Property: "region",
				Operator: "IN",
				Value:    "US,CA,UK",
			},
			context:  map[string]interface{}{"region": "US"},
			expected: true,
		},
		{
			name: "IN operator - no match",
			constraint: &LocalConstraint{
				Property: "region",
				Operator: "IN",
				Value:    "US,CA,UK",
			},
			context:  map[string]interface{}{"region": "RU"},
			expected: false,
		},
		{
			name: "GT operator - match",
			constraint: &LocalConstraint{
				Property: "age",
				Operator: "GT",
				Value:    "18",
			},
			context:  map[string]interface{}{"age": 25},
			expected: true,
		},
		{
			name: "GT operator - no match",
			constraint: &LocalConstraint{
				Property: "age",
				Operator: "GT",
				Value:    "18",
			},
			context:  map[string]interface{}{"age": 15},
			expected: false,
		},
		{
			name: "CONTAINS operator - match",
			constraint: &LocalConstraint{
				Property: "email",
				Operator: "CONTAINS",
				Value:    "@example.com",
			},
			context:  map[string]interface{}{"email": "user@example.com"},
			expected: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := evaluator.evaluateConstraint(tt.constraint, tt.context)
			assert.Equal(t, tt.expected, result)
		})
	}
}

func TestLocalEvaluationResult_GetAttachmentValue(t *testing.T) {
	result := &LocalEvaluationResult{
		VariantAttachment: map[string]interface{}{
			"color": "#FF0000",
			"size":  "large",
		},
	}

	t.Run("existing key", func(t *testing.T) {
		value := result.GetAttachmentValue("color", "#000000")
		assert.Equal(t, "#FF0000", value)
	})

	t.Run("missing key", func(t *testing.T) {
		value := result.GetAttachmentValue("missing", "default")
		assert.Equal(t, "default", value)
	})

	t.Run("nil attachment", func(t *testing.T) {
		result := &LocalEvaluationResult{}
		value := result.GetAttachmentValue("color", "default")
		assert.Equal(t, "default", value)
	})
}

func TestFlagSnapshot_IsExpired(t *testing.T) {
	t.Run("not expired", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			FetchedAt: time.Now().UnixMilli(),
			TTLMs:     60000, // 60 seconds
		}
		assert.False(t, snapshot.IsExpired())
	})

	t.Run("expired", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			FetchedAt: time.Now().UnixMilli() - 120000, // 2 minutes ago
			TTLMs:     60000,                            // 60 seconds TTL
		}
		assert.True(t, snapshot.IsExpired())
	})

	t.Run("no TTL never expires", func(t *testing.T) {
		snapshot := &FlagSnapshot{
			FetchedAt: time.Now().UnixMilli() - 999999999,
			TTLMs:     0, // No TTL
		}
		assert.False(t, snapshot.IsExpired())
	})
}

func TestFlagSnapshot_GetFlagByKey(t *testing.T) {
	snapshot := &FlagSnapshot{
		Flags: map[int64]*LocalFlag{
			1: {ID: 1, Key: "flag_a"},
			2: {ID: 2, Key: "flag_b"},
		},
	}

	t.Run("found", func(t *testing.T) {
		flag := snapshot.GetFlagByKey("flag_a")
		require.NotNil(t, flag)
		assert.Equal(t, int64(1), flag.ID)
	})

	t.Run("not found", func(t *testing.T) {
		flag := snapshot.GetFlagByKey("nonexistent")
		assert.Nil(t, flag)
	})
}

func TestFlagSnapshot_GetFlagByID(t *testing.T) {
	snapshot := &FlagSnapshot{
		Flags: map[int64]*LocalFlag{
			1: {ID: 1, Key: "flag_a"},
			2: {ID: 2, Key: "flag_b"},
		},
	}
	assert.NotNil(t, snapshot.GetFlagByID(1))
	assert.NotNil(t, snapshot.GetFlagByID(2))
	assert.Nil(t, snapshot.GetFlagByID(999))
}

func TestLocalEvaluator_EvaluateBatch(t *testing.T) {
	evaluator := NewLocalEvaluator()
	snapshot := &FlagSnapshot{
		Flags: map[int64]*LocalFlag{
			1: {
				ID:      1,
				Key:     "f1",
				Enabled: true,
				Segments: []*LocalSegment{{
					ID: 1, FlagID: 1, Rank: 1, RolloutPercent: 100,
					Constraints: nil,
					Distributions: []*LocalDistribution{{ID: 1, VariantID: 1, VariantKey: "control", Percent: 100}},
				}},
				Variants: []*LocalVariant{{ID: 1, FlagID: 1, Key: "control"}},
			},
		},
	}
	key := "f1"
	reqs := []*OfflineEvaluationRequest{
		{FlagKey: &key, EntityID: "u1"},
	}
	results := evaluator.EvaluateBatch(reqs, snapshot)
	require.Len(t, results, 1)
	assert.True(t, results[0].IsEnabled())
}
