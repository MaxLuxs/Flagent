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

```swift
import FlagentDebugUI
import FlagentEnhanced

let manager = FlagentManager()
FlagentDebugUI.show(manager: manager)
```

## Features

- List all flags
- View flag details with evaluation logs
- Local overrides
- Evaluation logs viewer