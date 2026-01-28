# Flagent Frontend - Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- JDK 11+
- Gradle 8.0+
- Node.js (for webpack)

### Installation

```bash
# Clone the repository
cd flagent/frontend

# Build the project
./gradlew build

# Run in development mode
./gradlew jsBrowserDevelopmentRun

# The app will be available at http://localhost:8080
```

### Production Build

```bash
# Build for production
./gradlew jsBrowserProductionWebpack

# Output will be in build/distributions/
# Serve with any static file server:
cd build/distributions
python3 -m http.server 8080
```

---

## ⚙️ Configuration

### Environment Variables

Edit `src/jsMain/resources/index.html`:

```javascript
window.ENV_API_BASE_URL = "http://localhost:18000";
window.ENV_DEBUG_MODE = "true";

// Feature flags
window.ENV_FEATURE_METRICS = "true";
window.ENV_FEATURE_REALTIME = "true";
```

### API Base URL

The frontend expects the backend to run on `http://localhost:18000` by default. To change:

1. Update `ENV_API_BASE_URL` in `index.html`
2. Or set it dynamically in your deployment

---

## 🏗️ Project Structure

```
frontend/
├── src/
│   ├── jsMain/kotlin/flagent/frontend/
│   │   ├── api/          # HTTP client & models
│   │   ├── viewmodel/    # Business logic
│   │   ├── components/   # UI components
│   │   │   ├── common/   # Reusable components
│   │   │   ├── flags/    # Flags management
│   │   │   ├── metrics/  # Metrics & charts
│   │   │   └── ...       # Other features
│   │   ├── navigation/   # Routing
│   │   ├── state/        # Global state
│   │   ├── config/       # Configuration
│   │   └── util/         # Utilities
│   └── jsTest/           # Tests
└── build.gradle.kts
```

---

## 🧪 Running Tests

```bash
# Run all tests
./gradlew jsTest

# Run with verbose output
./gradlew jsTest --info

# Run specific test
./gradlew jsTest --tests "FlagsViewModelTest"
```

---

## 🎨 Key Features

### 1. Flags Management
- View all flags in a responsive table
- Create, edit, delete flags
- Search and filter
- Toggle enabled/disabled

### 2. Metrics & Analytics
- View metrics with interactive Chart.js graphs
- Filter by metric type and time range
- Aggregated statistics

### 3. Smart Rollout
- Configure AI-powered gradual rollouts
- Monitor rollout progress
- Manual execution control

### 4. Anomaly Detection
- View and resolve anomaly alerts
- Configure detection thresholds
- Severity-based filtering

### 5. Real-time Updates
- Live notifications via SSE
- Connection status indicator
- Automatic reconnection

---

## 📱 Responsive Design

The UI adapts to:
- **Mobile** (< 768px): Card layouts, full-width buttons
- **Tablet** (769-1024px): Optimized spacing
- **Desktop** (> 1024px): Full table layouts

---

## 🔧 Common Tasks

### Adding a New Feature

1. **Create ViewModel** in `viewmodel/`
```kotlin
class MyFeatureViewModel {
    var data by mutableStateOf<List<Item>>(emptyList())
    
    fun loadData() {
        // Business logic
    }
}
```

2. **Create Component** in `components/myfeature/`
```kotlin
@Composable
fun MyFeatureComponent() {
    val viewModel = remember { MyFeatureViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // UI rendering
}
```

3. **Add Route** in `navigation/Router.kt`
```kotlin
sealed class Route {
    // ...
    object MyFeature : Route()
}
```

### Adding API Endpoint

Update `api/ApiClient.kt`:

```kotlin
suspend fun getMyData(): List<MyData> {
    return client.get(getApiPath("/my-data")).body()
}
```

### Adding Tests

Create test in `jsTest/`:

```kotlin
class MyViewModelTest {
    @Test
    fun testLoadData() {
        val viewModel = MyViewModel()
        assertEquals(0, viewModel.data.size)
    }
}
```

---

## 🐛 Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### API Connection Issues

1. Check backend is running on `http://localhost:18000`
2. Verify `ENV_API_BASE_URL` in `index.html`
3. Check browser console for CORS errors

### Module Loading Errors

Clear browser cache and rebuild:
```bash
./gradlew clean build
```

### Chart.js Not Working

Ensure Chart.js is loaded in `index.html`:
```html
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
```

---

## 📚 Documentation

- [README.md](README.md) - Comprehensive guide
- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture details
- [TESTING.md](TESTING.md) - Testing guide
- [CHANGELOG.md](CHANGELOG.md) - Version history

---

## 🤝 Contributing

1. Follow Kotlin coding conventions
2. Use ViewModels for business logic
3. Keep components small and focused
4. Write tests for new features
5. Update documentation

---

## 📞 Support

For issues or questions:
1. Check documentation first
2. Review troubleshooting section
3. Check browser console for errors
4. Verify backend API is running

---

## 🎯 Next Steps

1. ✅ Start backend: `./gradlew :backend:run`
2. ✅ Start frontend: `./gradlew :frontend:jsBrowserDevelopmentRun`
3. ✅ Open http://localhost:8080
4. ✅ Create your first flag!

Happy coding! 🚀
