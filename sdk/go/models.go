package flagent

import "github.com/MaxLuxs/Flagent/sdk/go/api"

// EvaluationResult wraps api.EvalResult with convenience methods for go-enhanced compatibility.
type EvaluationResult struct {
	*api.EvalResult
	// VariantKey exposed as *string for go-enhanced compatibility (populated from EvalResult)
	VariantKey *string
}

// IsEnabled checks if the flag is enabled (has variant assigned)
func (r *EvaluationResult) IsEnabled() bool {
	return r != nil && r.VariantKey != nil && *r.VariantKey != ""
}

// GetAttachmentValue retrieves a value from variant attachment
func (r *EvaluationResult) GetAttachmentValue(key string, defaultValue interface{}) interface{} {
	if r == nil || r.EvalResult == nil || r.VariantAttachment == nil {
		return defaultValue
	}
	if val, ok := r.VariantAttachment[key]; ok {
		return val
	}
	return defaultValue
}

// EvaluationContext is the input for Evaluate (maps to api.EvalContext)
type EvaluationContext struct {
	FlagKey       *string
	FlagID        *int64
	EntityID      *string
	EntityType    *string
	EntityContext map[string]interface{}
	EnableDebug   bool
}

// BatchEvaluationRequest maps to api.EvaluationBatchRequest
type BatchEvaluationRequest struct {
	Entities    []EvaluationEntity
	FlagKeys    []string
	FlagIDs     []int64
	EnableDebug bool
}

// EvaluationEntity maps to api.EvaluationEntity
type EvaluationEntity struct {
	EntityID      string
	EntityType    *string
	EntityContext map[string]interface{}
}

// FlagSnapshot is the export format from /export/eval_cache/json (for go-enhanced compatibility)
type FlagSnapshot struct {
	Flags    []api.Flag `json:"flags"`
	Revision *string    `json:"revision,omitempty"`
}

// Re-export api types for compatibility
type (
	Flag   = api.Flag
	Health = api.Health
)
