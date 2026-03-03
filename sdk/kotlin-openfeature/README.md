# Flagent Kotlin OpenFeature-like client (KMP)

`kotlin-openfeature` is a Kotlin Multiplatform module that provides an
OpenFeature-like client API on top of the existing Flagent Kotlin client
(`:kotlin-client`). It exposes a small, typed interface for resolving flags
and experiments while delegating network I/O and serialization to the
generated Flagent SDK.

## Targets

The module is KMP and supports:

- JVM / Android
- iOS (arm64 + simulator)
- JavaScript (IR)
- LinuxX64, MingwX64, MacosX64

All resolution logic lives in `commonMain`; platforms only provide HTTP
engines via `:kotlin-client`.

## Dependency

Gradle (Kotlin DSL), consuming from the same Maven repository as other
Flagent artifacts:

```kotlin
dependencies {
    implementation("com.flagent:kotlin-openfeature:<version>")
}
```

Replace `<version>` with the value from the root `VERSION` file or the latest
release on GitHub.

## Basic usage

```kotlin
import com.flagent.client.infrastructure.ApiClient
import com.flagent.openfeature.DefaultOpenFeatureClient
import com.flagent.openfeature.EvaluationContext
import com.flagent.openfeature.AttributeValue
import com.flagent.openfeature.FlagentOpenFeatureConfig
import com.flagent.openfeature.FlagentOpenFeatureProvider

suspend fun example() {
    val provider = FlagentOpenFeatureProvider(
        FlagentOpenFeatureConfig(
            baseUrl = "http://localhost:18000/api/v1",
            authConfig = { api: ApiClient ->
                // Configure authentication if needed, for example:
                // api.setApiKey("your-api-key", paramName = "X-API-Key")
            }
        )
    )

    val client = DefaultOpenFeatureClient(provider)

    val ctx = EvaluationContext(
        targetingKey = "user-123",
        attributes = mapOf(
            "country" to AttributeValue.StringValue("BY"),
            "beta" to AttributeValue.BooleanValue(true),
            "age" to AttributeValue.IntValue(30),
        )
    )

    val enabled = client.getBooleanValue(
        key = "new_checkout",
        defaultValue = false,
        context = ctx
    )

    val maxItems = client.getIntValue(
        key = "cart_max_items",
        defaultValue = 20,
        context = ctx
    )

    val discount = client.getDoubleValue(
        key = "discount_rate",
        defaultValue = 0.0,
        context = ctx
    )
}
```

## How it works

- `FlagentOpenFeatureProvider` calls the Flagent `/evaluation` endpoint via
  the generated `EvaluationApi` from `:kotlin-client`.
- Request context is built from `EvaluationContext` and `AttributeValue`
  into a JSON payload understood by the backend.
- Responses are mapped to `FlagEvaluationDetails<T>` with:
  - `value` (or default on error),
  - `variant` (for experiments),
  - `reason` (`TARGETING_MATCH`, `DEFAULT`, `ERROR`, `UNKNOWN`),
  - optional `errorCode`.

The goal is to provide a thin, OpenFeature-inspired facade without
duplicating the underlying HTTP or serialization logic.

