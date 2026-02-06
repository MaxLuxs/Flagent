# Flagent Kotlin Sample

Sample Kotlin/JVM console application demonstrating the usage of Flagent SDK for feature flagging and A/B testing.

## Features

- **Single Flag Evaluation** - Evaluate individual flags using flag key or flag ID
- **Batch Evaluation** - Evaluate multiple flags for multiple entities efficiently
- **Entity Context** - Provide additional context for evaluation (region, tier, etc.)
- **Debug Mode** - Enable debug mode for detailed evaluation logs
- **Simple Console Output** - Clean output showing evaluation results

## Requirements

- JDK 17+
- Gradle 8.0+
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

```bash
cd samples/kotlin
./gradlew build
./gradlew run
```

Or run directly with Gradle:

```bash
cd samples/kotlin
./gradlew run
```

## Usage

The sample demonstrates:

1. **Single Flag Evaluation with entityContext** - Evaluate a flag with context (region, tier) for constraint-based targeting
2. **Batch Evaluation** - Evaluate multiple flags for multiple entities

**Constraint-based evaluation:** When your flag has segments with constraints (e.g., `region` EQ `US`), pass `entityContext` with matching attributes. See `Main.kt` for the example with `entityContext = mapOf("region" to "US", "tier" to "premium")`.

### Example Output

```
============================================================
Flagent Kotlin SDK Sample
============================================================
Base URL: http://localhost:18000/api/v1

Example 1: Single Flag Evaluation
------------------------------------------------------------
Flag Key: my_feature_flag
Variant Key: enabled
Flag ID: 1
Variant ID: 2

Debug Log:
  Segment Matched: 1

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

This sample uses the Flagent Kotlin SDK. The SDK provides two options:

### Basic SDK

The basic SDK provides direct API access:

```kotlin
import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.*
import com.flagent.client.infrastructure.*

val configuration = Configuration(
    baseUrl = "http://localhost:18000/api/v1"
)

val evaluationApi = EvaluationApi(configuration)

val result = evaluationApi.postEvaluation(
    EvalContext(
        flagKey = "my_feature_flag",
        entityID = "user123",
        entityContext = mapOf("region" to "US")
    )
)
```

### Enhanced SDK (with caching)

The enhanced SDK adds automatic caching:

```kotlin
// Enhanced SDK usage is available in the SDK package
// See samples/kotlin/src/main/kotlin for examples
```

## Project Structure

```
kotlin/
├── src/main/kotlin/flagent/sample/
│   └── Main.kt          # Main application entry point
├── build.gradle.kts     # Build configuration
├── settings.gradle.kts  # Project settings
└── README.md            # This file
```

## Troubleshooting

### Connection Issues

- Ensure Flagent backend is running
- Check base URL is correct (`http://localhost:18000/api/v1`)
- Verify network connectivity

### Build Issues

- Ensure JDK 17+ is configured
- Run `./gradlew clean build` to rebuild from scratch
- Check SDK modules are correctly included in `settings.gradle.kts`

### SDK Dependencies

The sample uses **local SDK modules** from the same repository:
- `:sdk:kotlin` - Base Kotlin SDK (from `../../sdk/kotlin`)
- `:sdk:kotlin-enhanced` - Enhanced SDK with caching (from `../../sdk/kotlin-enhanced`)

These are included as Gradle subprojects in `settings.gradle.kts`. This allows you to:
- Make changes to SDK and see them immediately in the sample app
- Develop SDK and sample app together without publishing to Maven
- Test SDK changes before releasing

**Note**: If SDK modules fail to compile, fix them first as the sample app depends on them.

## License

Apache 2.0 - See parent project license
