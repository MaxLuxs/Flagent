# Flagent Swift Enhanced SDK

Enhanced Swift client library for Flagent API with caching and management.

## Installation

Add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/MaxLuxs/Flagent.git", from: "0.1.0"),
]
```

## Usage

```swift
import FlagentEnhanced
import FlagentClient

let config = FlagentConfig()
let manager = FlagentManager(config: config)

// Evaluate a flag
let result = try await manager.evaluate(
    flagKey: "new_feature",
    entityID: "user123",
    entityType: "user"
)
```

## Features

- Caching with configurable TTL
- Convenient API
- Batch evaluation support
- Cache management