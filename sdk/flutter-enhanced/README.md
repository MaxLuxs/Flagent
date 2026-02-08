# Flagent Flutter Enhanced SDK

Enhanced Flutter/Dart client for Flagent API with caching and convenient API.

## Features

- **Caching**: In-memory cache for evaluation results with configurable TTL
- **Convenient API**: High-level API for flag evaluation
- **Batch Evaluation**: Support for batch evaluation
- **Cache Management**: Clear cache, evict expired entries

## Installation

```yaml
dependencies:
  flagent_enhanced:
    git:
      url: https://github.com/MaxLuxs/Flagent.git
      path: sdk/flutter-enhanced
```

Or when published to pub.dev:

```yaml
dependencies:
  flagent_enhanced: ^0.1.5
```

## Usage

```dart
import 'package:flagent_enhanced/flagent_enhanced.dart';

final manager = FlagentManager(
  'https://api.example.com/api/v1',
  config: FlagentConfig(
    cacheTtlMs: 5 * 60 * 1000, // 5 minutes
    enableCache: true,
  ),
);

// Evaluate a flag
final result = await manager.evaluate(
  flagKey: 'new_feature',
  entityID: 'user123',
  entityContext: {'region': 'US'},
);

if (result.variantKey != null) {
  // Feature is enabled
}
```
