package flagent

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/MaxLuxs/Flagent/sdk/go/api"
)

const (
	defaultTimeout   = 30 * time.Second
	defaultUserAgent = "flagent-go-client/0.1.5"
)

// ClientOption is a function that configures a Client
type ClientOption func(*Client)

// Client is the Flagent API client (wrapper over generated api.APIClient)
type Client struct {
	apiClient *api.APIClient
}

// NewClient creates a new Flagent client
func NewClient(baseURL string, opts ...ClientOption) (*Client, error) {
	if baseURL == "" {
		return nil, NewInvalidConfigError("baseURL is required", nil)
	}
	baseURL = strings.TrimSuffix(baseURL, "/")

	cfg := api.NewConfiguration()
	cfg.Servers = api.ServerConfigurations{
		{URL: baseURL, Description: "Flagent API"},
	}
	cfg.HTTPClient = &http.Client{Timeout: defaultTimeout}
	cfg.UserAgent = defaultUserAgent

	client := &Client{apiClient: api.NewAPIClient(cfg)}

	for _, opt := range opts {
		opt(client)
	}

	return client, nil
}

// WithAPIKey sets the API key for authentication
func WithAPIKey(apiKey string) ClientOption {
	return func(c *Client) {
		if apiKey != "" {
			c.apiClient.GetConfig().DefaultHeader["Authorization"] = "Bearer " + apiKey
		}
	}
}

// WithTimeout sets the HTTP client timeout
func WithTimeout(timeout time.Duration) ClientOption {
	return func(c *Client) {
		c.apiClient.GetConfig().HTTPClient = &http.Client{Timeout: timeout}
	}
}

// WithHTTPClient sets a custom HTTP client
func WithHTTPClient(httpClient *http.Client) ClientOption {
	return func(c *Client) {
		c.apiClient.GetConfig().HTTPClient = httpClient
	}
}

// WithMaxRetries sets the maximum number of retries (no-op, kept for API compatibility)
func WithMaxRetries(maxRetries int) ClientOption {
	return func(c *Client) {}
}

// WithRetryDelay sets the delay between retries (no-op, kept for API compatibility)
func WithRetryDelay(delay time.Duration) ClientOption {
	return func(c *Client) {}
}

// toEvaluationResult converts api.EvalResult to EvaluationResult
func toEvaluationResult(e *api.EvalResult) *EvaluationResult {
	if e == nil {
		return nil
	}
	r := &EvaluationResult{EvalResult: e}
	if e.VariantKey.IsSet() && e.VariantKey.Get() != nil {
		r.VariantKey = e.VariantKey.Get()
	}
	return r
}

// evalContextToAPI converts our EvaluationContext to api.EvalContext
func evalContextToAPI(ec *EvaluationContext) api.EvalContext {
	ctx := api.NewEvalContext()
	if ec == nil {
		return *ctx
	}
	if ec.FlagKey != nil {
		ctx.SetFlagKey(*ec.FlagKey)
	}
	if ec.FlagID != nil {
		ctx.SetFlagID(*ec.FlagID)
	}
	if ec.EntityID != nil {
		ctx.SetEntityID(*ec.EntityID)
	}
	if ec.EntityType != nil {
		ctx.SetEntityType(*ec.EntityType)
	}
	if ec.EntityContext != nil {
		ctx.SetEntityContext(ec.EntityContext)
	}
	ctx.SetEnableDebug(ec.EnableDebug)
	return *ctx
}

// convertError converts api errors to our error types
func convertError(err error, context string) error {
	if err == nil {
		return nil
	}
	openAPIErr, ok := err.(*api.GenericOpenAPIError)
	if !ok {
		return NewNetworkError(context+": "+err.Error(), err)
	}
	if strings.Contains(openAPIErr.Error(), "404") {
		return NewFlagNotFoundError(context+": "+openAPIErr.Error(), err)
	}
	return NewEvaluationError(context+": "+openAPIErr.Error(), err)
}

// Evaluate evaluates a single flag
func (c *Client) Evaluate(ctx context.Context, evalCtx *EvaluationContext) (*EvaluationResult, error) {
	apiCtx := evalContextToAPI(evalCtx)
	result, resp, err := c.apiClient.EvaluationAPI.PostEvaluation(ctx).EvalContext(apiCtx).Execute()
	if err != nil {
		if resp != nil && resp.StatusCode == 404 {
			return nil, NewFlagNotFoundError("flag not found", err)
		}
		return nil, convertError(err, "evaluation failed")
	}
	return toEvaluationResult(result), nil
}

// EvaluateBatch evaluates multiple flags for multiple entities
func (c *Client) EvaluateBatch(ctx context.Context, req *BatchEvaluationRequest) ([]*EvaluationResult, error) {
	if req == nil {
		return nil, NewInvalidConfigError("request is required", nil)
	}
	entities := make([]api.EvaluationEntity, len(req.Entities))
	for i, e := range req.Entities {
		entityID := e.EntityID
		entities[i] = api.EvaluationEntity{
			EntityID:      &entityID,
			EntityType:    e.EntityType,
			EntityContext: e.EntityContext,
		}
	}
	flagIDs := make([]int32, len(req.FlagIDs))
	for i, id := range req.FlagIDs {
		flagIDs[i] = int32(id)
	}
	apiReq := api.EvaluationBatchRequest{
		Entities:    entities,
		FlagKeys:    req.FlagKeys,
		FlagIDs:     flagIDs,
		EnableDebug: api.PtrBool(req.EnableDebug),
	}
	apiReqPtr := &apiReq
	result, _, err := c.apiClient.EvaluationAPI.PostEvaluationBatch(ctx).EvaluationBatchRequest(*apiReqPtr).Execute()
	if err != nil {
		return nil, convertError(err, "batch evaluation failed")
	}
	results := make([]*EvaluationResult, len(result.EvaluationResults))
	for i := range result.EvaluationResults {
		results[i] = toEvaluationResult(&result.EvaluationResults[i])
	}
	return results, nil
}

// GetFlag retrieves a flag by ID
func (c *Client) GetFlag(ctx context.Context, flagID int64) (*Flag, error) {
	flag, resp, err := c.apiClient.FlagAPI.GetFlag(ctx, flagID).Execute()
	if err != nil {
		if resp != nil && resp.StatusCode == 404 {
			return nil, NewFlagNotFoundError(fmt.Sprintf("flag %d not found", flagID), err)
		}
		return nil, convertError(err, "failed to get flag")
	}
	return flag, nil
}

// ListFlagsOptions represents options for listing flags
type ListFlagsOptions struct {
	Limit   int
	Offset  int
	Enabled *bool
	Preload bool
}

// ListFlags retrieves a list of flags
func (c *Client) ListFlags(ctx context.Context, opts *ListFlagsOptions) ([]Flag, error) {
	if opts == nil {
		opts = &ListFlagsOptions{Limit: 100, Offset: 0, Preload: true}
	}
	req := c.apiClient.FlagAPI.FindFlags(ctx)
	req = req.Limit(int64(opts.Limit))
	req = req.Offset(int64(opts.Offset))
	req = req.Preload(opts.Preload)
	if opts.Enabled != nil {
		req = req.Enabled(*opts.Enabled)
	}
	flags, _, err := req.Execute()
	if err != nil {
		return nil, convertError(err, "failed to list flags")
	}
	return flags, nil
}

// GetSnapshot retrieves a snapshot for client-side evaluation
func (c *Client) GetSnapshot(ctx context.Context) (*FlagSnapshot, error) {
	result, _, err := c.apiClient.ExportAPI.GetExportEvalCacheJSON(ctx).Execute()
	if err != nil {
		return nil, convertError(err, "failed to get snapshot")
	}
	// API returns map[string]interface{}, unmarshal to FlagSnapshot
	jsonBytes, err := json.Marshal(result)
	if err != nil {
		return nil, NewNetworkError("failed to marshal snapshot: "+err.Error(), err)
	}
	var snapshot FlagSnapshot
	if err := json.Unmarshal(jsonBytes, &snapshot); err != nil {
		return nil, NewNetworkError("failed to unmarshal snapshot: "+err.Error(), err)
	}
	return &snapshot, nil
}

// HealthCheck checks server health
func (c *Client) HealthCheck(ctx context.Context) (*Health, error) {
	health, _, err := c.apiClient.HealthAPI.GetHealth(ctx).Execute()
	if err != nil {
		return nil, convertError(err, "health check failed")
	}
	return health, nil
}
