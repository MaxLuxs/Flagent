package flagentenhanced

import "time"

// LocalConstraint represents a constraint for local evaluation
type LocalConstraint struct {
	ID       int64  `json:"id"`
	Property string `json:"property"`
	Operator string `json:"operator"`
	Value    string `json:"value"`
}

// LocalDistribution represents variant distribution for local evaluation
type LocalDistribution struct {
	ID         int64  `json:"id"`
	VariantID  int64  `json:"variantID"`
	VariantKey string `json:"variantKey"`
	Percent    int    `json:"percent"`
}

// LocalSegment represents a segment for local evaluation
type LocalSegment struct {
	ID              int64                `json:"id"`
	FlagID          int64                `json:"flagID"`
	Rank            int                  `json:"rank"`
	RolloutPercent  int                  `json:"rolloutPercent"`
	Constraints     []*LocalConstraint   `json:"constraints"`
	Distributions   []*LocalDistribution `json:"distributions"`
	Description     string               `json:"description"`
}

// LocalVariant represents a variant for local evaluation
type LocalVariant struct {
	ID         int64                  `json:"id"`
	FlagID     int64                  `json:"flagID"`
	Key        string                 `json:"key"`
	Attachment map[string]interface{} `json:"attachment"`
}

// LocalFlag represents a flag for local evaluation
type LocalFlag struct {
	ID          int64           `json:"id"`
	Key         string          `json:"key"`
	Enabled     bool            `json:"enabled"`
	Description string          `json:"description"`
	Segments    []*LocalSegment `json:"segments"`
	Variants    []*LocalVariant `json:"variants"`
	EntityType  string          `json:"entityType"`
}

// FlagSnapshot represents a snapshot of all flags for offline evaluation
type FlagSnapshot struct {
	Flags     map[int64]*LocalFlag `json:"flags"`     // Map of flagID -> Flag
	FetchedAt int64                `json:"fetchedAt"` // Timestamp when snapshot was fetched
	TTLMs     int64                `json:"ttlMs"`     // Time-to-live in milliseconds
	Revision  string               `json:"revision"`  // Optional revision identifier
}

// IsExpired checks if the snapshot is expired
func (s *FlagSnapshot) IsExpired() bool {
	if s.TTLMs <= 0 {
		return false // No TTL means never expires
	}
	age := time.Now().UnixMilli() - s.FetchedAt
	return age > s.TTLMs
}

// GetFlagByKey finds a flag by its key
func (s *FlagSnapshot) GetFlagByKey(key string) *LocalFlag {
	for _, flag := range s.Flags {
		if flag.Key == key {
			return flag
		}
	}
	return nil
}

// GetFlagByID finds a flag by its ID
func (s *FlagSnapshot) GetFlagByID(id int64) *LocalFlag {
	return s.Flags[id]
}

// LocalEvaluationResult represents the result of local flag evaluation
type LocalEvaluationResult struct {
	FlagID            *int64                 `json:"flagID,omitempty"`
	FlagKey           *string                `json:"flagKey,omitempty"`
	VariantID         *int64                 `json:"variantID,omitempty"`
	VariantKey        *string                `json:"variantKey,omitempty"`
	VariantAttachment map[string]interface{} `json:"variantAttachment,omitempty"`
	SegmentID         *int64                 `json:"segmentID,omitempty"`
	Reason            string                 `json:"reason"`           // Evaluation reason (MATCH, NO_MATCH, FLAG_DISABLED, etc.)
	DebugLogs         []string               `json:"debugLogs"`        // Debug logs for troubleshooting
	EntityID          *string                `json:"entityID,omitempty"`
}

// IsEnabled checks if the flag is enabled (has variant assigned)
func (r *LocalEvaluationResult) IsEnabled() bool {
	return r.VariantKey != nil && *r.VariantKey != ""
}

// GetAttachmentValue retrieves a value from variant attachment
func (r *LocalEvaluationResult) GetAttachmentValue(key string, defaultValue interface{}) interface{} {
	if r.VariantAttachment == nil {
		return defaultValue
	}
	if val, ok := r.VariantAttachment[key]; ok {
		return val
	}
	return defaultValue
}

// OfflineEvaluationRequest represents a request for offline evaluation
type OfflineEvaluationRequest struct {
	FlagKey       *string                `json:"flagKey,omitempty"`
	FlagID        *int64                 `json:"flagID,omitempty"`
	EntityID      string                 `json:"entityID"`
	EntityType    *string                `json:"entityType,omitempty"`
	EntityContext map[string]interface{} `json:"entityContext,omitempty"`
	EnableDebug   bool                   `json:"enableDebug,omitempty"`
}
