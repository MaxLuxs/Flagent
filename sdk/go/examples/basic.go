package main

import (
	"context"
	"fmt"
	"log"
	"time"

	flagent "github.com/MaxLuxs/Flagent/sdk/go"
)

func main() {
	// Create client
	client, err := flagent.NewClient(
		"http://localhost:18000/api/v1",
		flagent.WithTimeout(10*time.Second),
	)
	if err != nil {
		log.Fatal(err)
	}

	ctx := context.Background()

	// Example 1: Simple evaluation
	fmt.Println("=== Example 1: Simple Evaluation ===")
	result, err := client.Evaluate(ctx, &flagent.EvaluationContext{
		FlagKey:  flagent.StringPtr("new_payment_flow"),
		EntityID: flagent.StringPtr("user123"),
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Flag: %s\n", *result.FlagKey)
		fmt.Printf("Enabled: %v\n", result.IsEnabled())
		if result.VariantKey != nil {
			fmt.Printf("Variant: %s\n", *result.VariantKey)
		}
	}

	// Example 2: Evaluation with context
	fmt.Println("\n=== Example 2: Evaluation with Context ===")
	result, err = client.Evaluate(ctx, &flagent.EvaluationContext{
		FlagKey:  flagent.StringPtr("premium_features"),
		EntityID: flagent.StringPtr("user456"),
		EntityContext: map[string]interface{}{
			"tier":   "premium",
			"region": "US",
		},
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Enabled: %v\n", result.IsEnabled())
	}

	// Example 3: Batch evaluation
	fmt.Println("\n=== Example 3: Batch Evaluation ===")
	results, err := client.EvaluateBatch(ctx, &flagent.BatchEvaluationRequest{
		Entities: []flagent.EvaluationEntity{
			{
				EntityID:      "user1",
				EntityContext: map[string]interface{}{"tier": "free"},
			},
			{
				EntityID:      "user2",
				EntityContext: map[string]interface{}{"tier": "premium"},
			},
		},
		FlagKeys: []string{"feature_a", "feature_b"},
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		for i, result := range results {
			flagKey := ""
			if result.FlagKey != nil {
				flagKey = *result.FlagKey
			}
			variantKey := ""
			if result.VariantKey != nil {
				variantKey = *result.VariantKey
			}
			entityID := "user1"
			if i >= len(results)/2 {
				entityID = "user2"
			}
			fmt.Printf("%s for %s: %s\n", flagKey, entityID, variantKey)
		}
	}

	// Example 4: Get flag details
	fmt.Println("\n=== Example 4: Get Flag Details ===")
	flag, err := client.GetFlag(ctx, 1)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Key: %s\n", flag.Key)
		fmt.Printf("Enabled: %v\n", flag.Enabled)
		fmt.Printf("Segments: %d\n", len(flag.Segments))
		fmt.Printf("Variants: %d\n", len(flag.Variants))
	}

	// Example 5: List flags
	fmt.Println("\n=== Example 5: List Flags ===")
	enabled := true
	flags, err := client.ListFlags(ctx, &flagent.ListFlagsOptions{
		Limit:   10,
		Enabled: &enabled,
		Preload: true,
	})
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Found %d flags:\n", len(flags))
		for _, flag := range flags {
			fmt.Printf("  - %s: %v\n", flag.Key, flag.Enabled)
		}
	}

	// Example 6: Health check
	fmt.Println("\n=== Example 6: Health Check ===")
	health, err := client.HealthCheck(ctx)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Status: %s\n", health.GetStatus())
	}

	// Example 7: Get snapshot
	fmt.Println("\n=== Example 7: Get Snapshot ===")
	snapshot, err := client.GetSnapshot(ctx)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Flags in snapshot: %d\n", len(snapshot.Flags))
		if snapshot.Revision != nil {
			fmt.Printf("Revision: %s\n", *snapshot.Revision)
		}
	}
}
