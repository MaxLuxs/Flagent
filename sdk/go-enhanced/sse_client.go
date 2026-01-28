package flagentenhanced

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strings"
	"sync"
	"time"
)

// SSEEvent represents a Server-Sent Event
type SSEEvent struct {
	Event string
	Data  string
	ID    string
}

// FlagUpdateEvent represents a flag update event from SSE
type FlagUpdateEvent struct {
	Type      string            `json:"type"`
	FlagID    *int64            `json:"flagID,omitempty"`
	FlagKey   *string           `json:"flagKey,omitempty"`
	Message   string            `json:"message"`
	Data      map[string]string `json:"data,omitempty"`
	Timestamp int64             `json:"timestamp"`
}

// ConnectionStatus represents the SSE connection status
type ConnectionStatus int

const (
	Disconnected ConnectionStatus = iota
	Connecting
	Connected
	Error
)

// SSEClient connects to Flagent SSE endpoint for real-time flag updates
type SSEClient struct {
	baseURL   string
	httpClient *http.Client
	config     *SSEConfig

	// Event channels
	events chan *FlagUpdateEvent
	status chan ConnectionStatus
	errors chan error

	// Connection management
	ctx           context.Context
	cancel        context.CancelFunc
	wg            sync.WaitGroup
	mu            sync.RWMutex
	isConnected   bool
	reconnectAttempt int
}

// SSEConfig represents configuration for SSE client
type SSEConfig struct {
	// AutoReconnect enables automatic reconnection on disconnect
	AutoReconnect bool

	// ReconnectDelay is the initial delay before reconnection
	ReconnectDelay time.Duration

	// MaxReconnectAttempts is the maximum number of reconnection attempts (0 = unlimited)
	MaxReconnectAttempts int

	// EventBufferSize is the size of the event channel buffer
	EventBufferSize int

	// EnableDebugLogging enables debug logging
	EnableDebugLogging bool
}

// DefaultSSEConfig returns the default SSE configuration
func DefaultSSEConfig() *SSEConfig {
	return &SSEConfig{
		AutoReconnect:        true,
		ReconnectDelay:       1 * time.Second,
		MaxReconnectAttempts: 0, // Unlimited
		EventBufferSize:      100,
		EnableDebugLogging:   false,
	}
}

// NewSSEClient creates a new SSE client
func NewSSEClient(baseURL string, httpClient *http.Client, config *SSEConfig) *SSEClient {
	if config == nil {
		config = DefaultSSEConfig()
	}

	if httpClient == nil {
		httpClient = &http.Client{
			Timeout: 0, // No timeout for SSE (long-lived connection)
		}
	}

	ctx, cancel := context.WithCancel(context.Background())

	return &SSEClient{
		baseURL:    strings.TrimSuffix(baseURL, "/"),
		httpClient: httpClient,
		config:     config,
		events:     make(chan *FlagUpdateEvent, config.EventBufferSize),
		status:     make(chan ConnectionStatus, 10),
		errors:     make(chan error, 10),
		ctx:        ctx,
		cancel:     cancel,
	}
}

// Connect establishes SSE connection and starts receiving events
func (c *SSEClient) Connect(flagKeys []string, flagIDs []int64) {
	c.mu.Lock()
	if c.isConnected {
		c.mu.Unlock()
		return
	}
	c.isConnected = true
	c.mu.Unlock()

	c.wg.Add(1)
	go c.connectionLoop(flagKeys, flagIDs)
}

// Disconnect closes the SSE connection
func (c *SSEClient) Disconnect() {
	c.mu.Lock()
	defer c.mu.Unlock()

	if !c.isConnected {
		return
	}

	c.cancel()
	c.wg.Wait()
	c.isConnected = false

	close(c.events)
	close(c.status)
	close(c.errors)
}

// Events returns the channel for receiving flag update events
func (c *SSEClient) Events() <-chan *FlagUpdateEvent {
	return c.events
}

// Status returns the channel for receiving connection status updates
func (c *SSEClient) Status() <-chan ConnectionStatus {
	return c.status
}

// Errors returns the channel for receiving errors
func (c *SSEClient) Errors() <-chan error {
	return c.errors
}

// IsConnected returns true if the client is connected
func (c *SSEClient) IsConnected() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.isConnected
}

// connectionLoop manages the SSE connection with automatic reconnection
func (c *SSEClient) connectionLoop(flagKeys []string, flagIDs []int64) {
	defer c.wg.Done()

	for {
		select {
		case <-c.ctx.Done():
			c.sendStatus(Disconnected)
			return
		default:
		}

		if c.config.MaxReconnectAttempts > 0 && c.reconnectAttempt >= c.config.MaxReconnectAttempts {
			c.sendError(fmt.Errorf("max reconnect attempts reached"))
			c.sendStatus(Disconnected)
			return
		}

		c.sendStatus(Connecting)
		if c.config.EnableDebugLogging {
			log.Printf("[SSEClient] Connecting to %s (attempt %d)", c.baseURL, c.reconnectAttempt+1)
		}

		err := c.connectAndListen(flagKeys, flagIDs)
		if err != nil {
			if c.config.EnableDebugLogging {
				log.Printf("[SSEClient] Connection error: %v", err)
			}
			c.sendError(err)
			c.sendStatus(Error)
		} else {
			c.sendStatus(Disconnected)
		}

		// Check if context was cancelled
		select {
		case <-c.ctx.Done():
			return
		default:
		}

		// Auto-reconnect logic
		if !c.config.AutoReconnect {
			return
		}

		c.reconnectAttempt++
		delay := c.calculateBackoff()

		if c.config.EnableDebugLogging {
			log.Printf("[SSEClient] Reconnecting in %v...", delay)
		}

		select {
		case <-time.After(delay):
			continue
		case <-c.ctx.Done():
			return
		}
	}
}

// connectAndListen establishes SSE connection and listens for events
func (c *SSEClient) connectAndListen(flagKeys []string, flagIDs []int64) error {
	url := fmt.Sprintf("%s/api/v1/realtime/sse", c.baseURL)

	// Add query parameters
	queryParams := []string{}
	for _, key := range flagKeys {
		queryParams = append(queryParams, fmt.Sprintf("flagKey=%s", key))
	}
	for _, id := range flagIDs {
		queryParams = append(queryParams, fmt.Sprintf("flagID=%d", id))
	}
	if len(queryParams) > 0 {
		url += "?" + strings.Join(queryParams, "&")
	}

	req, err := http.NewRequestWithContext(c.ctx, http.MethodGet, url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Accept", "text/event-stream")
	req.Header.Set("Cache-Control", "no-cache")
	req.Header.Set("Connection", "keep-alive")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to connect: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("unexpected status code: %d", resp.StatusCode)
	}

	c.sendStatus(Connected)
	c.reconnectAttempt = 0 // Reset on successful connection

	if c.config.EnableDebugLogging {
		log.Println("[SSEClient] Connected successfully")
	}

	// Read and parse SSE events
	scanner := bufio.NewScanner(resp.Body)
	var currentEvent SSEEvent

	for scanner.Scan() {
		line := scanner.Text()

		// Check context cancellation
		select {
		case <-c.ctx.Done():
			return nil
		default:
		}

		if line == "" {
			// Empty line means end of event
			if currentEvent.Data != "" {
				c.handleSSEEvent(&currentEvent)
				currentEvent = SSEEvent{}
			}
			continue
		}

		// Parse SSE line
		if strings.HasPrefix(line, "event:") {
			currentEvent.Event = strings.TrimSpace(line[6:])
		} else if strings.HasPrefix(line, "data:") {
			if currentEvent.Data != "" {
				currentEvent.Data += "\n"
			}
			currentEvent.Data += strings.TrimSpace(line[5:])
		} else if strings.HasPrefix(line, "id:") {
			currentEvent.ID = strings.TrimSpace(line[3:])
		}
	}

	if err := scanner.Err(); err != nil {
		return fmt.Errorf("scanner error: %w", err)
	}

	return nil
}

// handleSSEEvent processes an SSE event
func (c *SSEClient) handleSSEEvent(event *SSEEvent) {
	if c.config.EnableDebugLogging {
		log.Printf("[SSEClient] Received event: %s", event.Event)
	}

	switch event.Event {
	case "connection":
		// Connection acknowledgment
		if c.config.EnableDebugLogging {
			log.Printf("[SSEClient] Connection acknowledged: %s", event.Data)
		}
	case "flag.created", "flag.updated", "flag.deleted", "flag.toggled",
		"segment.updated", "variant.updated":
		// Flag update event
		var flagEvent FlagUpdateEvent
		if err := json.Unmarshal([]byte(event.Data), &flagEvent); err != nil {
			c.sendError(fmt.Errorf("failed to parse event: %w", err))
			return
		}

		select {
		case c.events <- &flagEvent:
		default:
			// Buffer full, drop oldest event
			if c.config.EnableDebugLogging {
				log.Println("[SSEClient] Event buffer full, dropping oldest event")
			}
		}
	default:
		if c.config.EnableDebugLogging {
			log.Printf("[SSEClient] Unknown event type: %s", event.Event)
		}
	}
}

// calculateBackoff calculates exponential backoff delay
func (c *SSEClient) calculateBackoff() time.Duration {
	// Exponential backoff: delay * 2^attempt, max 60 seconds
	maxAttempts := 6 // 2^6 = 64 seconds
	attempt := c.reconnectAttempt
	if attempt > maxAttempts {
		attempt = maxAttempts
	}

	delay := c.config.ReconnectDelay * time.Duration(1<<uint(attempt))
	maxDelay := 60 * time.Second
	if delay > maxDelay {
		delay = maxDelay
	}

	return delay
}

// sendStatus sends a status update (non-blocking)
func (c *SSEClient) sendStatus(status ConnectionStatus) {
	select {
	case c.status <- status:
	default:
	}
}

// sendError sends an error (non-blocking)
func (c *SSEClient) sendError(err error) {
	select {
	case c.errors <- err:
	default:
	}
}
