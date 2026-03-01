# Flagent Swift Debug UI

Debug UI library for Flagent Enhanced SDK using SwiftUI.

## Installation

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

Add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/MaxLuxs/Flagent.git", from: "0.1.6"),
]
```

## Usage

**macOS:** A debug panel opens automatically.

```swift
import FlagentDebugUI
import FlagentEnhanced

let manager = FlagentManager()
FlagentDebugUI.show(manager: manager)
```

**iOS:** Present `DebugView(manager:flagsProvider:)` in a sheet from your app:

```swift
import FlagentDebugUI
import FlagentEnhanced

.sheet(isPresented: $showDebug) {
    DebugView(manager: manager)
}
```

With **list of all flags** (e.g. from FlagAPI in FlagentClient). The `.async()` extension (Combine `Publisher` → async/await) is provided by FlagentEnhanced:

```swift
import FlagentDebugUI
import FlagentEnhanced  // provides .async() on AnyPublisher
import FlagentClient    // provides FlagAPI

let flagsProvider: () async throws -> [FlagRow] = {
    let flags = try await FlagAPI.findFlags(limit: 1000).async()
    return flags.map { FlagRow(key: $0.key, id: $0.id, enabled: $0.enabled, variantKeys: $0.variants?.map(\.key) ?? []) }
}

FlagentDebugUI.show(manager: manager, flagsProvider: flagsProvider)
// or on iOS:
DebugView(manager: manager, flagsProvider: flagsProvider)
```

## Features

- **Flags list** — when `flagsProvider` is set, loads and shows all flags; Refresh and Clear all overrides; per-flag Override picker (variant or disabled)
- **Local overrides** — set variant per flag for testing; overrides apply to Evaluate
- Evaluation form (flag key/ID, entity ID/type, enable debug)
- Result display with variant and segment info
- Eval debug log when debug is enabled
- Clear cache and evict expired entries
- Last evaluations list (recent results)