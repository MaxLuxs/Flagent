# Flagent Flutter App Sample (UI)

Flutter application with UI demonstrating Flagent SDK: Home, Evaluate, and Debug screens.

## Features

- **Home** — Configuration summary and navigation overview
- **Evaluate** — Single flag evaluation (flag key, entity ID/type, context), result display
- **Debug** — Evaluation form, last result, cache actions (clear / evict expired), last 10 evaluations list

## Requirements

- Flutter SDK 3.0+
- Flagent backend running (default: `http://localhost:18000`)

## Setup

### 1. Generate platform folders (first time)

If the project was created without platform folders, run from this directory:

```bash
cd samples/flutter_app
flutter create . --org com.flagent
```

### 2. Install dependencies

```bash
cd samples/flutter_app
flutter pub get
```

### 3. Run

```bash
flutter run
```

For Android emulator, the app uses `10.0.2.2` instead of `localhost` for the API. Override base URL:

```bash
flutter run --dart-define=FLAGENT_BASE_URL=http://10.0.2.2:18000/api/v1
```

Or set environment variable before running (platform-dependent).

## Tests

```bash
flutter test
```

Runs widget tests for HomeScreen, EvaluateScreen, and DebugScreen (title/config, form and buttons, cache actions).

## Project structure

```
samples/flutter_app/
├── lib/
│   ├── main.dart           # App entry, FlagentManager, bottom nav
│   └── screens/
│       ├── home_screen.dart
│       ├── evaluate_screen.dart
│       └── debug_screen.dart
├── test/
│   └── widget_test.dart    # Widget tests for all three screens
├── pubspec.yaml            # Flutter + flagent_client, flagent_enhanced (path)
└── README.md
```

## SDK

Uses local path dependencies:

- `../../sdk/dart` — base Dart client
- `../../sdk/flutter-enhanced` — enhanced manager with caching

No separate Flutter Debug UI package; the Debug tab is implemented in this sample (evaluation form, cache actions, last evals).

## License

Apache 2.0 — see parent project.
