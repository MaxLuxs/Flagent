# Flagent Samples

> [English](README.md) | [Русский](README.ru.md)

This folder contains sample applications demonstrating Flagent SDK and plugins for various platforms and programming languages.

## Available Samples

### Android Sample (`android/`)

Android application demonstrating the usage of Flagent Kotlin SDK for Android.

**Features:**
- Single flag evaluation
- Batch evaluation
- Enhanced SDK with caching
- Debug UI

**Run:**
```bash
cd samples/android
./gradlew build
```

### JavaScript Sample (`javascript/`)

Web application with HTML/JavaScript demonstrating Flagent API usage.

**Features:**
- Single flag evaluation
- Batch evaluation
- Entity context support
- Debug mode

**Run:**
```bash
# Open index.html in browser
open samples/javascript/index.html
```

### Kotlin Sample (`kotlin/`)

Kotlin/JVM console application demonstrating Flagent Kotlin SDK usage.

**Features:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Basic and Enhanced SDK

**Run:**
```bash
# From monorepo root
./gradlew :sample-kotlin:run
```

### Swift Sample (`swift/`)

Swift console application demonstrating Flagent Swift SDK usage.

**Features:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Basic and Enhanced SDK

**Run:**
```bash
cd samples/swift
swift build
swift run
```

### Flutter Sample (`flutter/`)

Dart console application demonstrating the usage of Flagent Flutter/Dart SDK.

**Features:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Enhanced SDK with caching

**Run:**
```bash
cd samples/flutter
dart pub get
dart run bin/main.dart
```

### Flutter App Sample (`flutter_app/`)

Flutter application **with UI** (Material): Home, Evaluate, and Debug screens.

**Features:**
- Single flag evaluation form and result display
- Debug screen: evaluate form, cache actions (clear / evict), last evaluations list
- Uses `flagent_enhanced` with caching

**Run:**
```bash
cd samples/flutter_app
flutter create . --org com.flagent   # first time: generate android/ios
flutter pub get
flutter run
```

### React Native Sample (`react-native/`)

Reference code for using Flagent SDK in React Native. Uses `@flagent/client` and `@flagent/enhanced-client`.

**See:** [sdk/REACT_NATIVE.md](../sdk/REACT_NATIVE.md) for full integration guide.

### Ktor Plugin Sample (`ktor/`)

Ktor application sample demonstrating Flagent Ktor plugin usage.

**Features:**
- Flagent plugin integration
- Custom endpoints with evaluation
- Batch evaluation
- Cache usage
- Plugin endpoints

**Run:**
```bash
# From monorepo root
./gradlew :sample-ktor:run
```

## Common Requirements

Before running any sample, ensure the Flagent backend server is running:

```bash
cd backend
./gradlew run
```

The server will start on `http://localhost:18000` by default.

## Structure

```
samples/
├── android/          # Android sample (Compose UI, includes SDK Debug UI)
├── javascript/       # JavaScript/HTML sample
├── kotlin/           # Kotlin/JVM console sample
├── swift/            # Swift console sample
├── flutter/          # Flutter/Dart console sample
├── flutter_app/     # Flutter app with UI (Home, Evaluate, Debug)
├── react-native/     # React Native reference
├── ktor/             # Ktor plugin sample
├── spring-boot/      # Spring Boot plugin sample
└── README.md         # This file
```

## UI and Debug UI

| Sample     | Has UI | SDK Debug UI integrated |
|------------|--------|--------------------------|
| Android    | Yes (Compose) | Yes — `kotlin-debug-ui` on Debug tab |
| Flutter App | Yes (Material) | Debug tab in-app (form, cache, last evals) |
| JavaScript | Yes (HTML) | Debug mode checkbox only (no React Debug Panel) |
| Swift      | No (CLI) | — |
| Flutter (console) | No (CLI) | — |
| Kotlin, Ktor, Spring Boot | No | — |

The **Android sample** uses the SDK Debug UI module (`kotlin-debug-ui`). The **Flutter App** sample implements its own Debug tab (evaluate form, cache actions, last evaluations) in Flutter; there is no separate Flutter Debug UI package in the SDK. Swift and the **Flutter console** sample are CLI-only.

## License

Apache 2.0 - See parent project license
