# Flagent Android Sample Application

Sample Android application demonstrating the usage of Flagent SDK for feature flagging and A/B testing.

## Features

- **Single Flag Evaluation** - Evaluate individual flags using basic or enhanced SDK
- **Batch Evaluation** - Evaluate multiple flags for multiple entities efficiently  
- **Caching** - Enhanced SDK provides automatic caching of evaluation results
- **Debug UI** - Visual debugging interface for flags and evaluations
- **Settings** - Configure API base URL, cache settings, and authentication

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 24 (Android 7.0) or later
- Flagent backend server running (default: `http://localhost:18000`)

## Setup

### 1. Start Flagent Backend

Before running the Android app, ensure the Flagent backend server is running:

```bash
cd backend
./gradlew run
```

The server will start on `http://localhost:18000` by default.

### 2. Configure Android Emulator

If you're using Android Emulator, the default base URL (`http://10.0.2.2:18000/api/v1`) is already configured to connect to the host machine's localhost.

For physical devices, you'll need to:
1. Find your machine's IP address on the local network
2. Update the base URL in the app's Settings screen to `http://<your-ip>:18000/api/v1`

### 3. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project: `./gradlew build`
4. Run on emulator or device

## Usage

### Home Screen

The home screen provides an overview of the app and quick navigation to all features.

### Evaluation Screen

Evaluate a single flag:

1. Select SDK type (Basic or Enhanced with caching)
2. Enter flag key or flag ID
3. Enter entity ID and entity type
4. Optionally provide entity context as JSON (e.g., `{"region": "US", "tier": "premium"}`)
5. Toggle debug mode if needed
6. Click "Evaluate"

The result will show:
- Assigned variant key
- Flag and segment information
- Debug log (if enabled)
- Cache indicator (for Enhanced SDK)

### Batch Evaluation Screen

Evaluate multiple flags for multiple entities:

1. Enter comma-separated flag keys (e.g., `flag1,flag2,flag3`)
2. Configure at least one entity with:
   - Entity ID
   - Entity Type
   - Entity Context (JSON)
3. Optionally add a second entity
4. Click "Evaluate Batch"

Results are displayed in a table showing variant assignments for each flag-entity combination.

### Settings Screen

Configure the app:

- **API Configuration**: Set the base URL of your Flagent server
- **Cache Settings**: Enable/disable caching and set TTL (time-to-live) in seconds
- **Authentication**: Configure authentication if your server requires it:
  - None (default)
  - Basic Auth (username/password)
  - Bearer Token (JWT)

### Debug Screen

Visual debugging interface for flags and evaluations (to be fully implemented).

## SDK Comparison

### Basic SDK

The basic SDK (`com.flagent:kotlin-client`) provides direct API access:
- Simple and lightweight
- No caching
- Direct API calls every time

### Enhanced SDK

The enhanced SDK (`com.flagent:kotlin-enhanced`) adds:
- Automatic caching of evaluation results
- Configurable cache TTL
- Reduced API calls for better performance
- Same API interface as basic SDK

## Architecture

The app follows Android best practices:

- **MVVM Architecture** - ViewModels manage UI state and business logic
- **Jetpack Compose** - Modern declarative UI framework
- **Material Design 3** - Latest Material Design components
- **Coroutines** - Asynchronous operations with Kotlin Coroutines
- **DataStore** - Type-safe data storage for settings

## Project Structure

```
android-sample/
├── app/
│   ├── src/main/java/com/flagent/sample/
│   │   ├── MainActivity.kt          # Main activity with navigation
│   │   ├── di/
│   │   │   └── AppModule.kt        # Dependency injection
│   │   ├── ui/
│   │   │   ├── theme/              # Material3 theme
│   │   │   ├── screens/            # Screen composables
│   │   │   └── components/         # Reusable UI components
│   │   └── viewmodel/              # ViewModels
│   └── build.gradle.kts
└── build.gradle.kts
```

## Troubleshooting

### Connection Issues

- Ensure Flagent backend is running
- Check base URL in Settings (should be `http://10.0.2.2:18000/api/v1` for emulator)
- For physical devices, use your machine's IP address
- Verify network permissions in AndroidManifest.xml

### Build Issues

- Ensure JDK 17+ is configured
- Sync Gradle files: `File > Sync Project with Gradle Files`
- Clean and rebuild: `Build > Clean Project` then `Build > Rebuild Project`

### SDK Dependencies

The app uses **local SDK modules** from the same repository:
- `:kotlin-client` - Base Kotlin SDK (from `../kotlin`)
- `:kotlin-enhanced` - Enhanced SDK with caching (from `../kotlin-enhanced`)
- `:kotlin-debug-ui` - Debug UI library (from `../kotlin-debug-ui`)

These are included as Gradle subprojects in `settings.gradle.kts`. This allows you to:
- Make changes to SDK and see them immediately in the sample app
- Develop SDK and sample app together without publishing to Maven
- Test SDK changes before releasing

**Note**: If SDK modules fail to compile, fix them first as the sample app depends on them.

## License

Apache 2.0 - See parent project license
