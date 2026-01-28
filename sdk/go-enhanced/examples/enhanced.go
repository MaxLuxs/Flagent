package main

import (
	"context"
	"fmt"
	"log"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
	enhanced "github.com/MaxLuxs/Flagent/sdk/go-enhanced"
)

func main() {
	// Create base client
	client, err := flagent.NewClient("http://localhost:18000/api/v1")
	if err != nil {
		log.Fatal(err)
	}

	// Create enhanced manager with caching
	config := enhanced.DefaultConfig().
		WithCacheTTL(10 * time.Minute).
		WithDebugLogging(true)

	manager := enhanced.NewManager(client, config)
	defer manager.Close()

	ctx := context.Background()

	// Example 1: Simple evaluation (with caching)
	fmt.Println("=== Example 1: Simple Evaluation (Cached) ===")
	result, err := manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Enabled: %v\n", result.IsEnabled())
		if result.VariantKey != nil {
			fmt.Printf("Variant: %s\n", *result.VariantKey)
		}
	}

	// Second evaluation - should use cache
	result, err = manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Println("Second call used cache (check debug logs)")
	}

	// Example 2: Check if enabled
	fmt.Println("\n=== Example 2: Check if Enabled ===")
	enabled, err := manager.IsEnabled(ctx, "premium_features", "user456", map[string]interface{}{
		"tier": "premium",
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Premium features enabled: %v\n", enabled)
	}

	// Example 3: Get variant
	fmt.Println("\n=== Example 3: Get Variant ===")
	variant, err := manager.GetVariant(ctx, "experiment_checkout", "user789", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Assigned variant: %s\n", variant)
	}

	// Example 4: Batch evaluation
	fmt.Println("\n=== Example 4: Batch Evaluation ===")
	entities := []flagent.EvaluationEntity{
		{
			EntityID:      "user1",
			EntityContext: map[string]interface{}{"tier": "free"},
		},
		{
			EntityID:      "user2",
			EntityContext: map[string]interface{}{"tier": "premium"},
		},
	}

	results, err := manager.EvaluateBatch(ctx, []string{"feature_a", "feature_b"}, entities)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		for _, result := range results {
			fmt.Printf("%s for %s: %s\n", *result.FlagKey, *result.EntityID, *result.VariantKey)
		}
	}

	// Example 5: Cache management
	fmt.Println("\n=== Example 5: Cache Management ===")
	manager.EvictExpired()
	fmt.Println("Evicted expired entries")

	manager.ClearCache()
	fmt.Println("Cache cleared")

	// Example 6: With auto-refresh
	fmt.Println("\n=== Example 6: Auto-Refresh ===")
	autoRefreshConfig := enhanced.DefaultConfig().
		WithSnapshotRefreshInterval(30 * time.Second)

	autoRefreshManager := enhanced.NewManager(client, autoRefreshConfig)
	defer autoRefreshManager.Close()

	fmt.Println("Manager with auto-refresh created (refreshes every 30 seconds)")

	// Wait a bit to demonstrate auto-refresh
	time.Sleep(2 * time.Second)
	fmt.Println("Auto-refresh is running in background")
}
