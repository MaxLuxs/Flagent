package flagent

// Constraint represents a flag constraint
type Constraint struct {
	ID       *int64  `json:"id,omitempty"`
	Property string  `json:"property"`
	Operator string  `json:"operator"`
	Value    *string `json:"value,omitempty"`
}

// Distribution represents variant distribution
type Distribution struct {
	ID         *int64 `json:"id,omitempty"`
	VariantID  int64  `json:"variantID"`
	VariantKey string `json:"variantKey"`
	Percent    int    `json:"percent"`
}

// Segment represents a flag segment
type Segment struct {
	ID              *int64          `json:"id,omitempty"`
	FlagID          *int64          `json:"flagID,omitempty"`
	Rank            int             `json:"rank"`
	RolloutPercent  int             `json:"rolloutPercent"`
	Constraints     []*Constraint   `json:"constraints,omitempty"`
	Distributions   []*Distribution `json:"distributions,omitempty"`
	Description     *string         `json:"description,omitempty"`
}

// Variant represents a flag variant
type Variant struct {
	ID         *int64                 `json:"id,omitempty"`
	FlagID     *int64                 `json:"flagID,omitempty"`
	Key        string                 `json:"key"`
	Attachment map[string]interface{} `json:"attachment,omitempty"`
}

// Flag represents a feature flag
type Flag struct {
	ID          *int64     `json:"id,omitempty"`
	Key         string     `json:"key"`
	Enabled     bool       `json:"enabled"`
	Description *string    `json:"description,omitempty"`
	Segments    []*Segment `json:"segments,omitempty"`
	Variants    []*Variant `json:"variants,omitempty"`
	EntityType  *string    `json:"entityType,omitempty"`
	CreatedBy   *string    `json:"createdBy,omitempty"`
	UpdatedBy   *string    `json:"updatedBy,omitempty"`
	UpdatedAt   *int64     `json:"updatedAt,omitempty"`
}

// EvaluationContext represents the context for flag evaluation
type EvaluationContext struct {
	FlagKey       *string                `json:"flagKey,omitempty"`
	FlagID        *int64                 `json:"flagID,omitempty"`
	EntityID      *string                `json:"entityID,omitempty"`
	EntityType    *string                `json:"entityType,omitempty"`
	EntityContext map[string]interface{} `json:"entityContext,omitempty"`
	EnableDebug   bool                   `json:"enableDebug,omitempty"`
}

// EvaluationResult represents the result of flag evaluation
type EvaluationResult struct {
	FlagID              *int64                 `json:"flagID,omitempty"`
	FlagKey             *string                `json:"flagKey,omitempty"`
	VariantID           *int64                 `json:"variantID,omitempty"`
	VariantKey          *string                `json:"variantKey,omitempty"`
	VariantAttachment   map[string]interface{} `json:"variantAttachment,omitempty"`
	SegmentID           *int64                 `json:"segmentID,omitempty"`
	EvaluationTimestamp *int64                 `json:"evaluationTimestamp,omitempty"`
	DebugLogs           []string               `json:"debugLogs,omitempty"`
	EntityID            *string                `json:"entityID,omitempty"`
}

// IsEnabled checks if the flag is enabled (has variant assigned)
func (r *EvaluationResult) IsEnabled() bool {
	return r.VariantKey != nil
}

// GetAttachmentValue retrieves a value from variant attachment
func (r *EvaluationResult) GetAttachmentValue(key string, defaultValue interface{}) interface{} {
	if r.VariantAttachment == nil {
		return defaultValue
	}
	if val, ok := r.VariantAttachment[key]; ok {
		return val
	}
	return defaultValue
}

// EvaluationEntity represents an entity for batch evaluation
type EvaluationEntity struct {
	EntityID      string                 `json:"entityID"`
	EntityType    *string                `json:"entityType,omitempty"`
	EntityContext map[string]interface{} `json:"entityContext,omitempty"`
}

// BatchEvaluationRequest represents a batch evaluation request
type BatchEvaluationRequest struct {
	Entities    []EvaluationEntity `json:"entities"`
	FlagKeys    []string           `json:"flagKeys,omitempty"`
	FlagIDs     []int64            `json:"flagIDs,omitempty"`
	EnableDebug bool               `json:"enableDebug,omitempty"`
}

// BatchEvaluationResponse represents a batch evaluation response
type BatchEvaluationResponse struct {
	EvaluationResults []*EvaluationResult `json:"evaluationResults"`
}

// Health represents server health status
type Health struct {
	Status string `json:"status"`
}

// FlagSnapshot represents a snapshot for client-side evaluation
type FlagSnapshot struct {
	Flags     []*Flag `json:"flags"`
	ExportAt  int64   `json:"exportAt"`
	Revision  *string `json:"revision,omitempty"`
}
