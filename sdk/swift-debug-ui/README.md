# Flagent Swift Debug UI

Debug UI library for Flagent Enhanced SDK using SwiftUI.

## Installation

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

**iOS:** The library does not present the UI automatically. Present `DebugView(manager:)` in a sheet from your app, for example:

```swift
import FlagentDebugUI
import FlagentEnhanced

// In your view:
.sheet(isPresented: $showDebug) {
    DebugView(manager: manager)
}
```

## Features

- Evaluation form (flag key/ID, entity ID/type, enable debug)
- Result display with variant and segment info
- Eval debug log when debug is enabled
- Clear cache and evict expired entries
- Last evaluations list (recent results)