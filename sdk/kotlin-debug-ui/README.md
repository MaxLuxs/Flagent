# Flagent Kotlin Debug UI

Debug UI library for Flagent Enhanced SDK using Compose Multiplatform.

## Installation

```kotlin
dependencies {
    implementation("com.flagent:kotlin-debug-ui:0.1.5")
}
```

**Note**: This library depends on `kotlin-enhanced`.

## Usage

```kotlin
import com.flagent.debug.ui.FlagentDebugUI
import com.flagent.enhanced.manager.FlagentManager

val manager = FlagentManager(evaluationApi, config)
FlagentDebugUI.show(manager)
```

## Features

- List all flags
- View flag details with evaluation logs
- Local overrides (test different flag values)
- Evaluation logs viewer
- Cache statistics