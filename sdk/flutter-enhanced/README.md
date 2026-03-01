# Flagent Flutter Enhanced SDK

Enhanced Flutter/Dart client for Flagent API with caching and convenient API.

## Features

- **Unified entry point**: Create client via `Flagent.create()` (same pattern as Kotlin/JS/Swift SDKs)
- **Caching**: In-memory cache for evaluation results with configurable TTL
- **Convenient API**: `evaluate`, `isEnabled`, `evaluateBatch` without low-level API knowledge
- **Cache Management**: Clear cache, evict expired entries

## Installation

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

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
  flagent_enhanced: ^0.1.6
```

## Usage

**Recommended: create the client with `Flagent.create()`**

```dart
import 'package:flagent_enhanced/flagent_enhanced.dart';

final client = Flagent.create(
  baseUrl: 'https://api.example.com/api/v1',
  config: FlagentConfig(
    cacheTtlMs: 5 * 60 * 1000, // 5 minutes
    enableCache: true,
  ),
);

// Boolean flag check
final on = await client.isEnabled(
  flagKey: 'new_feature',
  entityID: 'user123',
  entityContext: {'region': 'US'},
);

// Full evaluation
final result = await client.evaluate(
  flagKey: 'new_feature',
  entityID: 'user123',
  entityContext: {'region': 'US'},
);

// Batch evaluation
final results = await client.evaluateBatch(
  flagKeys: ['flag_a', 'flag_b'],
  entities: [buildEvaluationEntity(entityID: 'user123')],
);

// Clean up when done
client.destroy();
```

Alternative: `Flagent.managed(baseUrl, config)` when you already have a `FlagentConfig`, or `Flagent.fromOptions(FlagentOptions(...))` for an options bag.

### Advanced: direct FlagentManager

If you need to construct the client manually (e.g. custom Dio):

```dart
final manager = FlagentManager(
  'https://api.example.com/api/v1',
  dio: myDio,
  config: FlagentConfig(enableCache: true),
);
```

The object returned by `Flagent.create()` is a `FlagentManager`; the facade only provides a single recommended way to obtain it.
