# Flagent Kotlin Debug UI

Debug UI library for Flagent Enhanced SDK using Compose Multiplatform.

## Installation

```kotlin
dependencies {
    implementation("com.flagent:kotlin-debug-ui:0.1.6")
}
```

**Note**: This library depends on `kotlin-enhanced` and `androidx.compose`.

## Declarative FeatureFlag

```kotlin
import com.flagent.debug.ui.FeatureFlag
import com.flagent.debug.ui.FlagentProvider

FlagentProvider(manager = manager) {
    FeatureFlag(key = "new_checkout", fallback = { OldCheckout() }) {
        NewCheckout()
    }
}
```

## Usage (Debug UI)

```kotlin
import com.flagent.debug.ui.FlagentDebugUI
import com.flagent.enhanced.manager.FlagentManager

val manager = FlagentManager(evaluationApi, config)
FlagentDebugUI.DebugScreen(manager)
```

## Features

- Evaluation form (flag key/ID, entity ID/type, enable debug)
- Result display with variant and segment info
- Eval debug log when debug is enabled
- Clear cache and evict expired entries
- Last evaluations list (recent results)