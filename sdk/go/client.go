package flagent

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

const (
	defaultTimeout     = 30 * time.Second
	defaultUserAgent   = "flagent-go-client/0.1.5"
	defaultMaxRetries  = 3
	defaultRetryDelay  = 1 * time.Second
)

// ClientOption is a function that configures a Client
type ClientOption func(*Client)

// Client is the Flagent API client
type Client struct {
	baseURL    string
	apiKey     string
	httpClient *http.Client
	userAgent  string
	maxRetries int
	retryDelay time.Duration
}

// NewClient creates a new Flagent client
func NewClient(baseURL string, opts ...ClientOption) (*Client, error) {
	if baseURL == "" {
		return nil, NewInvalidConfigError("baseURL is required", nil)
	}

	// Ensure baseURL doesn't end with /
	baseURL = strings.TrimSuffix(baseURL, "/")

	client := &Client{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: defaultTimeout,
		},
		userAgent:  defaultUserAgent,
		maxRetries: defaultMaxRetries,
		retryDelay: defaultRetryDelay,
	}

	// Apply options
	for _, opt := range opts {
		opt(client)
	}

	return client, nil
}

// WithAPIKey sets the API key for authentication
func WithAPIKey(apiKey string) ClientOption {
	return func(c *Client) {
		c.apiKey = apiKey
	}
}

// WithTimeout sets the HTTP client timeout
func WithTimeout(timeout time.Duration) ClientOption {
	return func(c *Client) {
		c.httpClient.Timeout = timeout
	}
}

// WithHTTPClient sets a custom HTTP client
func WithHTTPClient(httpClient *http.Client) ClientOption {
	return func(c *Client) {
		c.httpClient = httpClient
	}
}

// WithMaxRetries sets the maximum number of retries
func WithMaxRetries(maxRetries int) ClientOption {
	return func(c *Client) {
		c.maxRetries = maxRetries
	}
}

// WithRetryDelay sets the delay between retries
func WithRetryDelay(delay time.Duration) ClientOption {
	return func(c *Client) {
		c.retryDelay = delay
	}
}

// doRequest performs an HTTP request with retry logic
func (c *Client) doRequest(ctx context.Context, method, path string, body interface{}) (*http.Response, error) {
	var bodyReader io.Reader
	if body != nil {
		jsonBody, err := json.Marshal(body)
		if err != nil {
			return nil, NewInvalidConfigError("failed to marshal request body", err)
		}
		bodyReader = bytes.NewReader(jsonBody)
	}

	url := c.baseURL + path
	
	var lastErr error
	for attempt := 0; attempt <= c.maxRetries; attempt++ {
		if attempt > 0 {
			time.Sleep(c.retryDelay * time.Duration(attempt))
		}

		// Reset body reader for retries
		if body != nil {
			jsonBody, _ := json.Marshal(body)
			bodyReader = bytes.NewReader(jsonBody)
		}

		req, err := http.NewRequestWithContext(ctx, method, url, bodyReader)
		if err != nil {
			lastErr = err
			continue
		}

		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("User-Agent", c.userAgent)
		if c.apiKey != "" {
			req.Header.Set("Authorization", "Bearer "+c.apiKey)
		}

		resp, err := c.httpClient.Do(req)
		if err != nil {
			lastErr = err
			continue
		}

		// Success or non-retryable error
		if resp.StatusCode < 500 {
			return resp, nil
		}

		// Server error - retry
		resp.Body.Close()
		lastErr = fmt.Errorf("server error: %d", resp.StatusCode)
	}

	return nil, NewNetworkError("request failed after retries", lastErr)
}

// parseErrorResponse parses error response from server
func parseErrorResponse(resp *http.Response) error {
	body, _ := io.ReadAll(resp.Body)
	return fmt.Errorf("API error (status %d): %s", resp.StatusCode, string(body))
}

// Evaluate evaluates a single flag
func (c *Client) Evaluate(ctx context.Context, evalCtx *EvaluationContext) (*EvaluationResult, error) {
	resp, err := c.doRequest(ctx, http.MethodPost, "/evaluation", evalCtx)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, NewFlagNotFoundError("flag not found", nil)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, NewEvaluationError("evaluation failed", parseErrorResponse(resp))
	}

	var result EvaluationResult
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, NewEvaluationError("failed to decode response", err)
	}

	return &result, nil
}

// EvaluateBatch evaluates multiple flags for multiple entities
func (c *Client) EvaluateBatch(ctx context.Context, req *BatchEvaluationRequest) ([]*EvaluationResult, error) {
	resp, err := c.doRequest(ctx, http.MethodPost, "/evaluation/batch", req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, NewEvaluationError("batch evaluation failed", parseErrorResponse(resp))
	}

	var batchResp BatchEvaluationResponse
	if err := json.NewDecoder(resp.Body).Decode(&batchResp); err != nil {
		return nil, NewEvaluationError("failed to decode response", err)
	}

	return batchResp.EvaluationResults, nil
}

// GetFlag retrieves a flag by ID
func (c *Client) GetFlag(ctx context.Context, flagID int64) (*Flag, error) {
	path := fmt.Sprintf("/flags/%d", flagID)
	resp, err := c.doRequest(ctx, http.MethodGet, path, nil)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, NewFlagNotFoundError(fmt.Sprintf("flag %d not found", flagID), nil)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, NewFlagentError("failed to get flag", parseErrorResponse(resp))
	}

	var flag Flag
	if err := json.NewDecoder(resp.Body).Decode(&flag); err != nil {
		return nil, NewFlagentError("failed to decode response", err)
	}

	return &flag, nil
}

// ListFlagsOptions represents options for listing flags
type ListFlagsOptions struct {
	Limit   int
	Offset  int
	Enabled *bool
	Preload bool
}

// ListFlags retrieves a list of flags
func (c *Client) ListFlags(ctx context.Context, opts *ListFlagsOptions) ([]*Flag, error) {
	if opts == nil {
		opts = &ListFlagsOptions{
			Limit:   100,
			Offset:  0,
			Preload: true,
		}
	}

	params := url.Values{}
	params.Set("limit", fmt.Sprintf("%d", opts.Limit))
	params.Set("offset", fmt.Sprintf("%d", opts.Offset))
	params.Set("preload", fmt.Sprintf("%t", opts.Preload))
	if opts.Enabled != nil {
		params.Set("enabled", fmt.Sprintf("%t", *opts.Enabled))
	}

	path := "/flags?" + params.Encode()
	resp, err := c.doRequest(ctx, http.MethodGet, path, nil)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, NewFlagentError("failed to list flags", parseErrorResponse(resp))
	}

	var flags []*Flag
	if err := json.NewDecoder(resp.Body).Decode(&flags); err != nil {
		return nil, NewFlagentError("failed to decode response", err)
	}

	return flags, nil
}

// GetSnapshot retrieves a snapshot for client-side evaluation
func (c *Client) GetSnapshot(ctx context.Context) (*FlagSnapshot, error) {
	resp, err := c.doRequest(ctx, http.MethodGet, "/export/eval_cache/json", nil)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, NewFlagentError("failed to get snapshot", parseErrorResponse(resp))
	}

	var snapshot FlagSnapshot
	if err := json.NewDecoder(resp.Body).Decode(&snapshot); err != nil {
		return nil, NewFlagentError("failed to decode response", err)
	}

	return &snapshot, nil
}

// HealthCheck checks server health
func (c *Client) HealthCheck(ctx context.Context) (*Health, error) {
	resp, err := c.doRequest(ctx, http.MethodGet, "/health", nil)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, NewNetworkError("health check failed", parseErrorResponse(resp))
	}

	var health Health
	if err := json.NewDecoder(resp.Body).Decode(&health); err != nil {
		return nil, NewNetworkError("failed to decode response", err)
	}

	return &health, nil
}

// NewFlagentError creates a generic Flagent error
func NewFlagentError(message string, err error) error {
	return &FlagentError{Message: message, Err: err}
}
