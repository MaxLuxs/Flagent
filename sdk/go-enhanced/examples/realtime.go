package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
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

	// Configure offline manager with debug logging
	config := enhanced.DefaultOfflineConfig().
		WithDebugLogging(true).
		WithAutoRefresh(false) // Disable polling, use SSE instead

	// Create offline manager
	manager := enhanced.NewOfflineManager(client, config)
	defer manager.Close()

	ctx := context.Background()

	// Bootstrap
	fmt.Println("=== Bootstrapping Manager ===")
	if err := manager.Bootstrap(ctx, false); err != nil {
		log.Printf("Bootstrap failed: %v", err)
		return
	}
	fmt.Println("Manager bootstrapped successfully")

	// Enable real-time updates via SSE
	fmt.Println("\n=== Enabling Real-Time Updates (SSE) ===")
	if err := manager.EnableRealtimeUpdates(
		"http://localhost:18000",
		nil, // All flags
		nil, // All flag IDs
	); err != nil {
		log.Printf("Failed to enable real-time updates: %v", err)
		return
	}
	fmt.Println("Real-time updates enabled!")

	// Check status
	fmt.Printf("Realtime enabled: %v\n", manager.IsRealtimeEnabled())

	// Example 1: Evaluate a flag
	fmt.Println("\n=== Example 1: Initial Evaluation ===")
	result, err := manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
	if err != nil {
		log.Printf("Error: %v", err)
	} else {
		fmt.Printf("Flag: %s\n", *result.FlagKey)
		fmt.Printf("Enabled: %v\n", result.IsEnabled())
		if result.VariantKey != nil {
			fmt.Printf("Variant: %s\n", *result.VariantKey)
		}
	}

	// Example 2: Listen for updates
	fmt.Println("\n=== Example 2: Listening for Real-Time Updates ===")
	fmt.Println("Waiting for flag updates...")
	fmt.Println("Try updating a flag in the admin UI at http://localhost:18000")
	fmt.Println("Press Ctrl+C to exit")

	// Setup graceful shutdown
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)

	// Periodically evaluate to show updated values
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			result, err := manager.Evaluate(ctx, "new_payment_flow", "user123", nil)
			if err != nil {
				log.Printf("Evaluation error: %v", err)
			} else {
				fmt.Printf("\n[%s] Evaluated flag: %s, Enabled: %v",
					time.Now().Format("15:04:05"),
					*result.FlagKey,
					result.IsEnabled())
				if result.VariantKey != nil {
					fmt.Printf(", Variant: %s", *result.VariantKey)
				}
				fmt.Println()
			}

		case <-sigChan:
			fmt.Println("\n\nReceived interrupt signal, shutting down...")
			return
		}
	}
}
