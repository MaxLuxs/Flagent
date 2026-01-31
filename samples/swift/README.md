# Flagent Swift Sample

Sample Swift console application demonstrating the usage of Flagent SDK for feature flagging and A/B testing.

## Features

- **Single Flag Evaluation** - Evaluate individual flags using flag key or flag ID
- **Batch Evaluation** - Evaluate multiple flags for multiple entities efficiently
- **Entity Context** - Provide additional context for evaluation (region, tier, etc.)
- **Debug Mode** - Enable debug mode for detailed evaluation logs
- **Simple Console Output** - Clean output showing evaluation results

## Requirements

- Swift 5.1+
- Xcode 12.0+ (for iOS/macOS development)
- Flagent backend server running (default: `http://localhost:18000`)

## Setup

### 1. Start Flagent Backend

Before running the sample, ensure the Flagent backend server is running:

```bash
cd backend
./gradlew run
```

The server will start on `http://localhost:18000` by default.

### 2. Configure Base URL (Optional)

By default, the sample uses `http://localhost:18000/api/v1`. You can override this with an environment variable:

```bash
export FLAGENT_BASE_URL=http://localhost:18000/api/v1
```

### 3. Build and Run

#### Using Swift Package Manager

```bash
cd samples/swift
swift build
swift run
```

#### Using Xcode

1. Open the project in Xcode:
   ```bash
   cd samples/swift
   open Package.swift
   ```

2. Select the `FlagentSwiftSample` scheme
3. Build and run (Cmd+R)

## Usage

The sample demonstrates three examples:

1. **Single Flag Evaluation** - Evaluate a flag using flag key, entity ID, and context
2. **Batch Evaluation** - Evaluate multiple flags for multiple entities
3. **Evaluation with Flag ID** - Evaluate a flag using flag ID instead of key

### Example Output

```
============================================================
Flagent Swift SDK Sample
============================================================
Base URL: http://localhost:18000/api/v1

Example 1: Single Flag Evaluation
------------------------------------------------------------
Flag Key: my_feature_flag
Variant Key: enabled
Flag ID: 1
Variant ID: 2

Debug Log:
  Segment ID: 1

Example 2: Batch Evaluation
------------------------------------------------------------
Total Results: 6

Result 1:
  Flag Key: flag1
  Variant Key: control
  Entity ID: user1

...
```

## SDK Integration

This sample uses the Flagent Swift SDK. The SDK provides two options:

### Basic SDK

The basic SDK provides direct API access:

```swift
import FlagentClient

let configuration = Configuration(basePath: "http://localhost:18000/api/v1")
let evaluationAPI = EvaluationAPI(configuration: configuration)

let evalContext = EvalContext(
    flagKey: "my_feature_flag",
    entityID: "user123",
    entityContext: ["region": "US"]
)

let result = try await evaluationAPI.postEvaluation(evalContext: evalContext)
```

### Enhanced SDK (with caching)

The enhanced SDK adds automatic caching:

```swift
import FlagentEnhanced

// Enhanced SDK usage is available in the SDK package
// See SDK documentation for examples
```

## Project Structure

```
swift/
├── Sources/FlagentSwiftSample/
│   └── main.swift           # Main application entry point
├── Package.swift            # Swift Package Manager configuration
└── README.md                # This file
```

## Troubleshooting

### Connection Issues

- Ensure Flagent backend is running
- Check base URL is correct (`http://localhost:18000/api/v1`)
- Verify network connectivity

### Build Issues

- Ensure Swift 5.1+ is installed: `swift --version`
- Run `swift package clean` and rebuild
- Check SDK packages are correctly referenced in `Package.swift`

### SDK Dependencies

The sample uses **local SDK packages** from the same repository:
- `../../sdk/swift` - Base Swift SDK
- `../../sdk/swift-enhanced` - Enhanced SDK with caching

These are referenced as local packages in `Package.swift`. This allows you to:
- Make changes to SDK and see them immediately in the sample app
- Develop SDK and sample app together without publishing
- Test SDK changes before releasing

**Note**: If SDK packages fail to compile, fix them first as the sample app depends on them.

### Platform-Specific Notes

- **macOS**: Works out of the box
- **iOS**: Requires iOS 11.0+ and Xcode 12.0+
- **Linux**: May require additional network libraries depending on Swift version

## License

Apache 2.0 - See parent project license
