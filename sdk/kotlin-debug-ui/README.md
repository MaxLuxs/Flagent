# Flagent Kotlin Debug UI

Debug UI library for Flagent Enhanced SDK using Compose Multiplatform.

## Installation

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

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

With **list of all flags** (e.g. from FlagApi or OfflineFlagentManager):

```kotlin
import com.flagent.debug.ui.FlagentDebugUI
import com.flagent.debug.ui.FlagRow
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.client.apis.FlagApi

// Option A: OfflineFlagentManager only (flags from snapshot, evaluate offline)
val offlineManager: OfflineFlagentManager = ...
offlineManager.bootstrap()
FlagentDebugUI.DebugScreen(offlineManager = offlineManager)

// Option B: FlagApi.findFlags() for server-side FlagentManager (with flags list)
val manager: FlagentManager = ...
val flagApi: FlagApi = ...
FlagentDebugUI.DebugScreen(
    manager = manager,
    flagsProvider = {
        flagApi.findFlags(limit = 1000).body().map { f ->
            FlagRow(f.key, f.id, f.enabled, f.variants?.map { it.key } ?: emptyList(), f.description)
        }
    }
)
```

## Features

- **Flags list** — when `flagsProvider` is set, loads and shows all flags (key, enabled, variants); Refresh and Clear all overrides
- **Local overrides** — set variant (or disabled) per flag for testing without changing server; overrides apply to Evaluate and list
- Evaluation form (flag key/ID, entity ID/type, enable debug)
- Result display with variant and segment info
- Eval debug log when debug is enabled
- Clear cache and evict expired entries
- Last evaluations list (recent results)