package flagentenhanced

import (
	"fmt"
	"hash/crc32"
	"regexp"
	"sort"
	"strconv"
	"strings"
)

const (
	totalBucketNum    = 1000
	percentMultiplier = 10
)

// LocalEvaluator evaluates flags locally without API calls
type LocalEvaluator struct{}

// NewLocalEvaluator creates a new local evaluator
func NewLocalEvaluator() *LocalEvaluator {
	return &LocalEvaluator{}
}

// Evaluate evaluates a flag using local snapshot
func (e *LocalEvaluator) Evaluate(req *OfflineEvaluationRequest, snapshot *FlagSnapshot) *LocalEvaluationResult {
	// Find flag by key or ID
	var flag *LocalFlag
	if req.FlagKey != nil {
		flag = snapshot.GetFlagByKey(*req.FlagKey)
	} else if req.FlagID != nil {
		flag = snapshot.GetFlagByID(*req.FlagID)
	}

	if flag == nil {
		return &LocalEvaluationResult{
			FlagID:    req.FlagID,
			FlagKey:   req.FlagKey,
			Reason:    "FLAG_NOT_FOUND",
			DebugLogs: conditionalDebug(req.EnableDebug, "Flag not found in snapshot"),
			EntityID:  &req.EntityID,
		}
	}

	// Check if flag is enabled
	if !flag.Enabled {
		return &LocalEvaluationResult{
			FlagID:    &flag.ID,
			FlagKey:   &flag.Key,
			Reason:    "FLAG_DISABLED",
			DebugLogs: conditionalDebug(req.EnableDebug, "Flag is disabled"),
			EntityID:  &req.EntityID,
		}
	}

	// Check if flag has segments
	if len(flag.Segments) == 0 {
		return &LocalEvaluationResult{
			FlagID:    &flag.ID,
			FlagKey:   &flag.Key,
			Reason:    "NO_SEGMENTS",
			DebugLogs: conditionalDebug(req.EnableDebug, "Flag has no segments"),
			EntityID:  &req.EntityID,
		}
	}

	debugLogs := []string{}

	// Sort segments by rank
	sortedSegments := make([]*LocalSegment, len(flag.Segments))
	copy(sortedSegments, flag.Segments)
	sort.Slice(sortedSegments, func(i, j int) bool {
		return sortedSegments[i].Rank < sortedSegments[j].Rank
	})

	// Evaluate segments in order
	for _, segment := range sortedSegments {
		if req.EnableDebug {
			debugLogs = append(debugLogs, fmt.Sprintf("Evaluating segment %d (rank %d)", segment.ID, segment.Rank))
		}

		// Check constraints
		if !e.evaluateConstraints(segment.Constraints, req.EntityContext) {
			if req.EnableDebug {
				debugLogs = append(debugLogs, fmt.Sprintf("Segment %d: constraints did not match", segment.ID))
			}
			continue
		}

		if req.EnableDebug {
			debugLogs = append(debugLogs, fmt.Sprintf("Segment %d: constraints matched", segment.ID))
		}

		// Check rollout and select variant
		variantID, inRollout := e.selectVariant(segment, req.EntityID, flag.ID)

		if !inRollout {
			if req.EnableDebug {
				debugLogs = append(debugLogs, fmt.Sprintf("Segment %d: not in rollout percentage", segment.ID))
			}
			continue
		}

		// Find variant
		var variant *LocalVariant
		for _, v := range flag.Variants {
			if v.ID == variantID {
				variant = v
				break
			}
		}

		result := &LocalEvaluationResult{
			FlagID:     &flag.ID,
			FlagKey:    &flag.Key,
			VariantID:  &variantID,
			SegmentID:  &segment.ID,
			Reason:     "MATCH",
			DebugLogs:  debugLogs,
			EntityID:   &req.EntityID,
		}

		if variant != nil {
			result.VariantKey = &variant.Key
			result.VariantAttachment = variant.Attachment
		}

		if req.EnableDebug {
			result.DebugLogs = append(result.DebugLogs, fmt.Sprintf("Segment %d: matched, assigned variant %d", segment.ID, variantID))
		}

		return result
	}

	// No segment matched
	return &LocalEvaluationResult{
		FlagID:    &flag.ID,
		FlagKey:   &flag.Key,
		Reason:    "NO_MATCH",
		DebugLogs: append(debugLogs, "No segment matched"),
		EntityID:  &req.EntityID,
	}
}

// EvaluateBatch evaluates multiple flags
func (e *LocalEvaluator) EvaluateBatch(requests []*OfflineEvaluationRequest, snapshot *FlagSnapshot) []*LocalEvaluationResult {
	results := make([]*LocalEvaluationResult, len(requests))
	for i, req := range requests {
		results[i] = e.Evaluate(req, snapshot)
	}
	return results
}

// evaluateConstraints checks if all constraints match (AND logic)
func (e *LocalEvaluator) evaluateConstraints(constraints []*LocalConstraint, context map[string]interface{}) bool {
	if len(constraints) == 0 {
		return true
	}

	for _, constraint := range constraints {
		if !e.evaluateConstraint(constraint, context) {
			return false
		}
	}
	return true
}

// evaluateConstraint evaluates a single constraint
func (e *LocalEvaluator) evaluateConstraint(constraint *LocalConstraint, context map[string]interface{}) bool {
	contextValue := ""
	if val, ok := context[constraint.Property]; ok {
		contextValue = fmt.Sprintf("%v", val)
	}
	constraintValue := constraint.Value

	switch constraint.Operator {
	case "EQ":
		return contextValue == constraintValue
	case "NEQ":
		return contextValue != constraintValue
	case "LT":
		cv, _ := strconv.ParseFloat(contextValue, 64)
		cv2, _ := strconv.ParseFloat(constraintValue, 64)
		return cv < cv2
	case "LTE":
		cv, _ := strconv.ParseFloat(contextValue, 64)
		cv2, _ := strconv.ParseFloat(constraintValue, 64)
		return cv <= cv2
	case "GT":
		cv, _ := strconv.ParseFloat(contextValue, 64)
		cv2, _ := strconv.ParseFloat(constraintValue, 64)
		return cv > cv2
	case "GTE":
		cv, _ := strconv.ParseFloat(contextValue, 64)
		cv2, _ := strconv.ParseFloat(constraintValue, 64)
		return cv >= cv2
	case "IN":
		values := strings.Split(constraintValue, ",")
		for _, v := range values {
			if strings.TrimSpace(v) == contextValue {
				return true
			}
		}
		return false
	case "NOTIN":
		values := strings.Split(constraintValue, ",")
		for _, v := range values {
			if strings.TrimSpace(v) == contextValue {
				return false
			}
		}
		return true
	case "CONTAINS":
		return strings.Contains(contextValue, constraintValue)
	case "NOTCONTAINS":
		return !strings.Contains(contextValue, constraintValue)
	case "EREG":
		matched, err := regexp.MatchString(constraintValue, contextValue)
		return err == nil && matched
	case "NEREG":
		matched, err := regexp.MatchString(constraintValue, contextValue)
		return err != nil || !matched
	default:
		return false
	}
}

// selectVariant selects a variant based on rollout and distribution.
// EVALUATION_SPEC: hashInput = salt + entityID (no separator)
func (e *LocalEvaluator) selectVariant(segment *LocalSegment, entityID string, flagID int64) (int64, bool) {
	salt := fmt.Sprintf("%d", flagID)
	hashInput := salt + entityID
	bucket := int(crc32.ChecksumIEEE([]byte(hashInput)) % uint32(totalBucketNum))

	// Check rollout percentage
	rolloutBucket := segment.RolloutPercent * percentMultiplier
	if bucket >= rolloutBucket {
		return 0, false
	}

	// Select variant based on distribution (EVALUATION_SPEC: sort by percent, bucketInt = bucket+1)
	if len(segment.Distributions) == 0 {
		return 0, true
	}

	sortedDist := make([]*LocalDistribution, len(segment.Distributions))
	copy(sortedDist, segment.Distributions)
	sort.Slice(sortedDist, func(i, j int) bool {
		return sortedDist[i].Percent < sortedDist[j].Percent
	})

	bucketInt := bucket + 1 // 1..1000 scale per EVALUATION_SPEC
	cumulative := 0
	for _, dist := range sortedDist {
		cumulative += dist.Percent * percentMultiplier
		if bucketInt <= cumulative {
			return dist.VariantID, true
		}
	}
	return sortedDist[len(sortedDist)-1].VariantID, true
}

// conditionalDebug returns debug logs only if debug is enabled
func conditionalDebug(enableDebug bool, message string) []string {
	if enableDebug {
		return []string{message}
	}
	return []string{}
}
