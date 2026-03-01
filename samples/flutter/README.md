# Flagent Flutter Sample

Sample Dart console application demonstrating the usage of Flagent SDK for feature flagging and A/B testing.

## Features

- **Single Flag Evaluation** - Evaluate individual flags using flag key or flag ID
- **Batch Evaluation** - Evaluate multiple flags for multiple entities efficiently
- **Entity Context** - Provide additional context for evaluation (region, tier, etc.)
- **Debug Mode** - Enable debug mode for detailed evaluation logs
- **Caching** - Uses FlagentManager with in-memory caching

## Requirements

- Dart 3.0+
- Flutter SDK (optional, for `flutter pub get`) or Dart SDK
- Flagent backend server running (default: `http://localhost:18000`)

## Setup

### 1. Start Flagent Backend

```bash
cd backend
./gradlew run
```

The server will start on `http://localhost:18000` by default.

### 2. Configure Base URL (Optional)

```bash
export FLAGENT_BASE_URL=http://localhost:18000/api/v1
```

### 3. Run Sample

```bash
cd samples/flutter
dart pub get
dart run flagent_flutter_sample
# or: dart run bin/main.dart
```

Or with Flutter:

```bash
cd samples/flutter
flutter pub get
dart run flagent_flutter_sample
# or: dart run bin/main.dart
```

## Project Structure

```
samples/flutter/
├── bin/
│   └── main.dart       # Main application entry point
├── pubspec.yaml        # Dependencies (local SDK paths)
└── README.md           # This file
```

## SDK Integration

This sample uses Flagent Flutter SDK with local path dependencies:

- `flagent_client` - Base Dart SDK (from OpenAPI)
- `flagent_enhanced` - Enhanced SDK with caching

For production, use published packages. Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

```yaml
dependencies:
  flagent_client: ^0.1.6
  flagent_enhanced: ^0.1.6
```

## License

Apache 2.0 - See parent project license
