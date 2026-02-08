# Flagent Go SDK

Go client library for Flagent feature flags and experimentation platform.

The base API layer (`api/` package) is generated from the OpenAPI specification. The `Client` in this package is a thin wrapper providing a convenient API (`Evaluate`, `EvaluateBatch`, `GetFlag`, etc.) and error conversion.

## Features

- ✅ **Idiomatic Go** - Clean, simple API following Go conventions
- ✅ **Context Support** - Full context.Context support for cancellation and timeouts
- ✅ **Type-Safe** - Strongly typed models with proper error handling
- ✅ **Auto-Retry** - Automatic retry on network errors with exponential backoff
- ✅ **Connection Pooling** - Efficient HTTP connection management
- ✅ **Go 1.21+** - Support for Go 1.21 and higher

## Installation

```bash
go get github.com/MaxLuxs/Flagent/sdk/go
```

## Quick Start

### Basic Usage

```go
package main

import (
    "context"
    "fmt"
    "log"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

func main() {
    // Create client
    client, err := flagent.NewClient("http://localhost:18000/api/v1")
    if err != nil {
        log.Fatal(err)
    }
    
    ctx := context.Background()
    
    // Evaluate a flag
    result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
        FlagKey:  flagent.StringPtr("new_payment_flow"),
        EntityID: flagent.StringPtr("user123"),
    })
    if err != nil {
        log.Fatal(err)
    }
    
    if result.IsEnabled() {
        fmt.Println("Feature is enabled")
        fmt.Printf("Variant: %s\n", *result.VariantKey)
    } else {
        fmt.Println("Feature is disabled")
    }
}
```

### With Context and Timeout

```go
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey:  flagent.StringPtr("new_feature"),
    EntityID: flagent.StringPtr("user123"),
    EntityContext: map[string]interface{}{
        "tier":   "premium",
        "region": "US",
    },
})
if err != nil {
    log.Fatal(err)
}

fmt.Printf("Enabled: %v\n", result.IsEnabled())
```

## API Reference

### Client Initialization

```go
// Basic client
client, err := flagent.NewClient("http://localhost:18000/api/v1")

// With options
client, err := flagent.NewClient(
    "http://localhost:18000/api/v1",
    flagent.WithAPIKey("your-api-key"),
    flagent.WithTimeout(30*time.Second),
    flagent.WithMaxRetries(3),
    flagent.WithRetryDelay(time.Second),
)
```

#### Client Options

- `WithAPIKey(apiKey string)` - Set API key for authentication
- `WithTimeout(timeout time.Duration)` - Set HTTP client timeout
- `WithHTTPClient(httpClient *http.Client)` - Use custom HTTP client
- `WithMaxRetries(maxRetries int)` - Set maximum retry attempts (default: 3)
- `WithRetryDelay(delay time.Duration)` - Set delay between retries (default: 1s)

### Evaluate Flag

```go
result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey:  flagent.StringPtr("feature_name"),  // Flag key (required if FlagID not provided)
    FlagID:   flagent.Int64Ptr(123),              // Flag ID (required if FlagKey not provided)
    EntityID: flagent.StringPtr("user123"),       // Entity ID for bucketing
    EntityType: flagent.StringPtr("user"),        // Entity type (optional)
    EntityContext: map[string]interface{}{        // Context for constraint matching
        "tier":   "premium",
        "region": "US",
        "age":    25,
    },
    EnableDebug: false,                           // Enable debug logs
})

// Check if enabled
if result.IsEnabled() {
    // Feature is enabled
    variantKey := *result.VariantKey
    
    // Access variant attachment
    color := result.GetAttachmentValue("color", "#000")
}
```

### Batch Evaluation

```go
results, err := client.EvaluateBatch(ctx, &flagent.BatchEvaluationRequest{
    Entities: []flagent.EvaluationEntity{
        {
            EntityID: "user1",
            EntityType: flagent.StringPtr("user"),
            EntityContext: map[string]interface{}{"tier": "free"},
        },
        {
            EntityID: "user2",
            EntityType: flagent.StringPtr("user"),
            EntityContext: map[string]interface{}{"tier": "premium"},
        },
    },
    FlagKeys:    []string{"feature_a", "feature_b"},
    EnableDebug: false,
})

for _, result := range results {
    fmt.Printf("%s: %s\n", result.GetFlagKey(), result.GetVariantKey())
}
```

### Get Flag Details

```go
flag, err := client.GetFlag(ctx, 123)
if err != nil {
    log.Fatal(err)
}

fmt.Printf("Key: %s\n", flag.Key)
fmt.Printf("Enabled: %v\n", flag.Enabled)
fmt.Printf("Segments: %d\n", len(flag.Segments))
fmt.Printf("Variants: %d\n", len(flag.Variants))
```

### List Flags

```go
enabled := true
flags, err := client.ListFlags(ctx, &flagent.ListFlagsOptions{
    Limit:   50,
    Offset:  0,
    Enabled: &enabled,  // Filter by enabled status
    Preload: true,      // Preload segments, variants, etc.
})

for _, flag := range flags {
    fmt.Printf("%s: %v\n", flag.Key, flag.Enabled)
}
```

### Get Snapshot (for client-side evaluation)

```go
snapshot, err := client.GetSnapshot(ctx)
if err != nil {
    log.Fatal(err)
}

fmt.Printf("Flags: %d\n", len(snapshot.Flags))
if snapshot.Revision != nil {
    fmt.Printf("Revision: %s\n", *snapshot.Revision)
}
```

### Health Check

```go
health, err := client.HealthCheck(ctx)
if err != nil {
    log.Fatal(err)
}

fmt.Printf("Status: %s\n", health.GetStatus())
```

## Advanced Usage

### Custom HTTP Client

```go
httpClient := &http.Client{
    Timeout: 60 * time.Second,
    Transport: &http.Transport{
        MaxIdleConns:        100,
        MaxIdleConnsPerHost: 10,
        IdleConnTimeout:     90 * time.Second,
    },
}

client, err := flagent.NewClient(
    "http://localhost:18000/api/v1",
    flagent.WithHTTPClient(httpClient),
)
```

### Authentication

```go
client, err := flagent.NewClient(
    "http://localhost:18000/api/v1",
    flagent.WithAPIKey("your-api-key"),
)
```

### Error Handling

```go
result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey:  flagent.StringPtr("nonexistent_flag"),
    EntityID: flagent.StringPtr("user123"),
})

if err != nil {
    switch e := err.(type) {
    case *flagent.FlagNotFoundError:
        fmt.Println("Flag not found")
        // Use default behavior
    case *flagent.EvaluationError:
        fmt.Printf("Evaluation failed: %v\n", e)
        // Use default behavior
    case *flagent.NetworkError:
        fmt.Printf("Network error: %v\n", e)
        // Retry or use cached value
    default:
        fmt.Printf("Unknown error: %v\n", err)
    }
}
```

### Integration with HTTP Server

```go
package main

import (
    "context"
    "encoding/json"
    "log"
    "net/http"
    
    flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

var flagentClient *flagent.Client

func init() {
    var err error
    flagentClient, err = flagent.NewClient("http://localhost:18000/api/v1")
    if err != nil {
        log.Fatal(err)
    }
}

func checkFeatureHandler(w http.ResponseWriter, r *http.Request) {
    featureName := r.URL.Query().Get("feature")
    userID := r.URL.Query().Get("user_id")
    
    ctx := r.Context()
    result, err := flagentClient.Evaluate(ctx, &flagent.EvaluationContext{
        FlagKey:  &featureName,
        EntityID: &userID,
    })
    
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
    
    response := map[string]interface{}{
        "feature": featureName,
        "enabled": result.IsEnabled(),
        "variant": result.VariantKey,
    }
    
    json.NewEncoder(w).Encode(response)
}

func main() {
    http.HandleFunc("/features", checkFeatureHandler)
    log.Fatal(http.ListenAndServe(":8080", nil))
}
```

### Concurrent Evaluations

```go
import "golang.org/x/sync/errgroup"

func evaluateForUsers(ctx context.Context, flagKey string, userIDs []string) ([]*flagent.EvaluationResult, error) {
    g, ctx := errgroup.WithContext(ctx)
    results := make([]*flagent.EvaluationResult, len(userIDs))
    
    for i, userID := range userIDs {
        i, userID := i, userID // Capture loop variables
        g.Go(func() error {
            result, err := flagentClient.Evaluate(ctx, &flagent.EvaluationContext{
                FlagKey:  &flagKey,
                EntityID: &userID,
            })
            if err != nil {
                return err
            }
            results[i] = result
            return nil
        })
    }
    
    if err := g.Wait(); err != nil {
        return nil, err
    }
    
    return results, nil
}

// Usage
userIDs := []string{"user1", "user2", "user3"}
results, err := evaluateForUsers(ctx, "experiment_checkout", userIDs)
```

## Exception Handling

### Error Types Hierarchy

```
FlagentError (base)
├── FlagNotFoundError
├── EvaluationError
├── NetworkError
└── InvalidConfigError
```

### Example

```go
import "errors"

result, err := client.Evaluate(ctx, evalCtx)
if err != nil {
    var flagNotFound *flagent.FlagNotFoundError
    if errors.As(err, &flagNotFound) {
        // Handle flag not found
    }
    
    var evalError *flagent.EvaluationError
    if errors.As(err, &evalError) {
        // Handle evaluation error
    }
}
```

## Helper Functions

The SDK provides helper functions for creating pointers:

```go
// String pointer
flagKey := flagent.StringPtr("my_flag")

// Int64 pointer
flagID := flagent.Int64Ptr(123)

// Bool pointer
enabled := flagent.BoolPtr(true)
```

## Best Practices

### 1. Reuse Client Instances

```go
// Good: Create once, reuse
var flagentClient *flagent.Client

func init() {
    var err error
    flagentClient, err = flagent.NewClient("http://localhost:18000/api/v1")
    if err != nil {
        log.Fatal(err)
    }
}

func evaluateFeatureA() (*flagent.EvaluationResult, error) {
    return flagentClient.Evaluate(ctx, &flagent.EvaluationContext{
        FlagKey:  flagent.StringPtr("feature_a"),
        EntityID: flagent.StringPtr("user123"),
    })
}

// Bad: Creating new client for each request
func badEvaluate() {
    client, _ := flagent.NewClient("http://localhost:18000/api/v1")
    result, _ := client.Evaluate(ctx, evalCtx)
}
```

### 2. Always Use Context

```go
// Good: Use context for cancellation and timeouts
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

result, err := client.Evaluate(ctx, evalCtx)
```

### 3. Handle Errors Gracefully

```go
func safeFeatureCheck(ctx context.Context, flagKey, userID string, defaultEnabled bool) bool {
    result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
        FlagKey:  &flagKey,
        EntityID: &userID,
    })
    if err != nil {
        return defaultEnabled // Fallback to default on error
    }
    return result.IsEnabled()
}
```

### 4. Use Batch Evaluation for Multiple Entities

```go
// More efficient than multiple single evaluations
results, err := client.EvaluateBatch(ctx, &flagent.BatchEvaluationRequest{
    Entities: entities,
    FlagKeys: []string{"feature_a"},
})
```

## Performance

### Connection Pooling

The SDK uses Go's `http.Client` with connection pooling by default, which reuses TCP connections for better performance.

### Timeouts

Configure appropriate timeouts based on your latency requirements:

```go
client, err := flagent.NewClient(
    "http://localhost:18000/api/v1",
    flagent.WithTimeout(5*time.Second), // 5 seconds for low-latency environments
)
```

### Batch Evaluation

Use batch evaluation when evaluating multiple flags for multiple users to reduce network overhead.

## Testing

Run tests:

```bash
go test ./...
```

Run tests with coverage:

```bash
go test -cover ./...
```

Run tests with race detector:

```bash
go test -race ./...
```

## License

Apache 2.0

## Links

- **Documentation**: https://maxluxs.github.io/Flagent
- **GitHub**: https://github.com/MaxLuxs/Flagent
- **Go Package**: https://pkg.go.dev/github.com/MaxLuxs/Flagent/sdk/go
