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

	// Configure offline manager
	config := enhanced.DefaultOfflineConfig().
		WithPersistence(true).                  // Enable persistent cache
		WithSnapshotTTL(5 * time.Minute).       // 5 minute TTL
		WithAutoRefresh(true).                  // Auto-refresh in background
		WithRefreshInterval(60 * time.Second).  // Refresh every 60 seconds
		WithDebugLogging(true)                  // Enable debug logs

	// Create offline manager
	manager := enhanced.NewOfflineManager(client, config)
	defer manager.Close()

	ctx := context.Background()

	// Example 1: Bootstrap (once on app start)
	fmt.Println("=== Example 1: Bootstrap ===")
	if err := manager.Bootstrap(ctx, false); err != nil {
		log.Printf("Bootstrap failed: %v", err)
		// In production, you might want to handle this differently
		return
	}
	fmt.Println("Manager bootstrapped and ready")

	// Check if ready
	if !manager.IsReady() {
		log.Fatal("Manager not ready")
	}

	// Example 2: Simple evaluation (local, < 1ms)
	fmt.Println("\n=== Example 2: Simple Evaluation (Client-Side) ===")
	result, err := manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Flag: %s\n", *result.FlagKey)
		fmt.Printf("Enabled: %v\n", result.IsEnabled())
		if result.VariantKey != nil {
			fmt.Printf("Variant: %s\n", *result.VariantKey)
		}
		if len(result.DebugLogs) > 0 {
			fmt.Println("Debug logs:")
			for _, msg := range result.DebugLogs {
				fmt.Printf("  - %s\n", msg)
			}
		}
	}

	// Example 3: Check if enabled
	fmt.Println("\n=== Example 3: Check if Enabled ===")
	enabled, err := manager.IsEnabled(ctx, "premium_features", "user456", map[string]interface{}{
		"tier": "premium",
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Premium features enabled: %v\n", enabled)
	}

	// Example 4: Get variant
	fmt.Println("\n=== Example 4: Get Variant ===")
	variant, err := manager.GetVariant(ctx, "experiment_checkout", "user789", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Assigned variant: %s\n", variant)
	}

	// Example 5: Batch evaluation
	fmt.Println("\n=== Example 5: Batch Evaluation ===")
	flagKey1 := "feature_a"
	flagKey2 := "feature_b"
	requests := []*enhanced.OfflineEvaluationRequest{
		{
			FlagKey:  &flagKey1,
			EntityID: "user1",
			EntityContext: map[string]interface{}{
				"tier": "free",
			},
		},
		{
			FlagKey:  &flagKey2,
			EntityID: "user2",
			EntityContext: map[string]interface{}{
				"tier": "premium",
			},
		},
	}

	results, err := manager.EvaluateBatch(ctx, requests)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		for _, result := range results {
			if result.FlagKey != nil && result.VariantKey != nil {
				fmt.Printf("%s for %s: %s\n", *result.FlagKey, *result.EntityID, *result.VariantKey)
			}
		}
	}

	// Example 6: Check snapshot status
	fmt.Println("\n=== Example 6: Snapshot Status ===")
	age, err := manager.GetSnapshotAge()
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Snapshot age: %dms\n", age)
	}

	expired := manager.IsSnapshotExpired()
	fmt.Printf("Snapshot expired: %v\n", expired)

	// Example 7: Manual refresh
	fmt.Println("\n=== Example 7: Manual Refresh ===")
	if err := manager.Refresh(ctx); err != nil {
		log.Printf("Refresh failed: %v", err)
	} else {
		fmt.Println("Snapshot refreshed successfully")
	}

	// Example 8: Performance comparison
	fmt.Println("\n=== Example 8: Performance Comparison ===")

	// Client-side evaluation (local)
	startLocal := time.Now()
	for i := 0; i < 1000; i++ {
		manager.Evaluate(ctx, "test_flag", fmt.Sprintf("user%d", i), nil)
	}
	localDuration := time.Since(startLocal)
	fmt.Printf("Client-side: 1000 evaluations in %v (avg: %v per eval)\n",
		localDuration, localDuration/1000)

	// Server-side evaluation (for comparison, if you have regular manager)
	// fmt.Printf("Server-side: ~50-200ms per evaluation\n")
	// fmt.Printf("Speed improvement: ~%dx faster\n", int(200*time.Millisecond/localDuration*1000))

	// Wait for auto-refresh to demonstrate background updates
	fmt.Println("\n=== Example 9: Auto-Refresh (waiting 5 seconds) ===")
	fmt.Println("Manager will auto-refresh in background every 60 seconds")
	time.Sleep(5 * time.Second)
	fmt.Println("Auto-refresh is running...")

	// Example 10: Clear cache (cleanup)
	fmt.Println("\n=== Example 10: Clear Cache ===")
	if err := manager.ClearCache(); err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Println("Cache cleared")
	}

	fmt.Println("\nOffline manager demo completed!")
}
