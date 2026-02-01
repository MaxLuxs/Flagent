# Flagent Frontend

Modern feature flags management frontend built with **Kotlin Multiplatform** and **Compose for Web**.

## Features

- 🚀 **Modern Architecture** - Clean architecture with ViewModels, State Management, and separation of concerns
- 🎨 **Beautiful UI** - Responsive design with smooth animations and transitions
- 📊 **Metrics & Analytics** - Real-time metrics visualization with charts
- 🤖 **AI-Powered Features** - Smart rollouts and anomaly detection
- 🔄 **Real-time Updates** - Server-Sent Events (SSE) for live updates
- 🔐 **Authentication** - JWT and SSO/SAML support
- 🏢 **Multi-tenancy** - Full tenant management and isolation
- 💳 **Billing** - Stripe integration for subscription management
- ♿ **Accessibility** - ARIA attributes and keyboard navigation
- 🌍 **Localization** - Multi-language support

## Architecture

### Layers

```
frontend/
├── api/           # HTTP client & API models
├── viewmodel/     # Business logic & state management
├── components/    # UI components (presentation)
│   ├── common/    # Reusable UI components
│   ├── flags/     # Flags CRUD
│   ├── metrics/   # Metrics & analytics
│   ├── rollout/   # Smart rollout
│   ├── anomaly/   # Anomaly detection
│   ├── auth/      # Authentication
│   ├── tenants/   # Multi-tenancy
│   ├── billing/   # Billing
│   └── realtime/  # Real-time updates
├── navigation/    # Routing
├── state/         # Global state
├── service/       # Services (SSE, etc.)
├── theme/         # Theme & styles
├── i18n/          # Localization
├── util/          # Utilities (Logger, ErrorHandler)
└── config/        # Configuration
```

### Key Principles

1. **Separation of Concerns** - ViewModels handle business logic, components only handle UI
2. **Singleton ApiClient** - Centralized HTTP client with error handling
3. **Type Safety** - Strong typing with Kotlin sealed classes for routes and errors
4. **Reactive State** - Compose state management for reactive UI updates
5. **Error Handling** - Centralized error handling with user-friendly messages
6. **Logging** - Configurable logging for development and production

## Getting Started

### Prerequisites

- JDK 11 or higher
- Gradle 8.0 or higher
- Node.js (for webpack)

### Build

Run from the **repository root** (no `gradlew` in `frontend/`):

```bash
# Build the project
./gradlew :frontend:build

# Run in development mode
./gradlew :frontend:jsBrowserDevelopmentRun

# Build for production
./gradlew :frontend:jsBrowserProductionWebpack
```

### Environment Configuration

Configure environment variables in `src/jsMain/resources/index.html` (or via your deployment):

```javascript
window.ENV_API_BASE_URL = "http://localhost:18000";
window.ENV_DEBUG_MODE = "true";
window.ENV_API_TIMEOUT = "30000";

// Edition: "open_source" (default) or "enterprise"
window.ENV_EDITION = "open_source";

// Feature flags (Enterprise-only features are gated by ENV_EDITION in code)
window.ENV_FEATURE_AUTH = "true";
window.ENV_FEATURE_REALTIME = "true";
window.ENV_FEATURE_METRICS = "true";   // effective only when ENV_EDITION=enterprise
window.ENV_FEATURE_MULTI_TENANCY = "true";
window.ENV_FEATURE_SSO = "true";
window.ENV_FEATURE_BILLING = "true";
```

See [EDITION_GUIDE.md](EDITION_GUIDE.md) for Open Source vs Enterprise and all env vars.

## Development

### Adding New Features

1. **Create ViewModel** - Add business logic in `viewmodel/`
2. **Create API Models** - Add request/response models in `api/`
3. **Create Components** - Add UI components in `components/`
4. **Add Routes** - Update routing in `navigation/Router.kt`
5. **Test** - Add tests in `jsTest/`

### Code Style

- Follow Kotlin coding conventions
- Use Compose idioms (remember, LaunchedEffect, etc.)
- Document public APIs with KDoc
- Keep components small and focused
- Use ViewModels for business logic

### Common Patterns

#### ViewModel Pattern

```kotlin
class MyViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    var data by mutableStateOf<List<Item>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    fun loadData() {
        scope.launch {
            isLoading = true
            ErrorHandler.withErrorHandling(
                block = {
                    data = ApiClient.getData()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
            isLoading = false
        }
    }
}
```

#### Component Pattern

```kotlin
@Composable
fun MyComponent() {
    val viewModel = remember { MyViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // UI rendering
    if (viewModel.isLoading) {
        SkeletonLoader()
    } else if (viewModel.data.isEmpty()) {
        EmptyState("No data")
    } else {
        // Render data
    }
}
```

## Testing

```bash
# Run tests (from repo root)
./gradlew :frontend:jsTest

# Run with coverage
./gradlew :frontend:jsTest --coverage
```

Note: JS tests are currently disabled in the default build (`jsBrowserTest` and `compileTestKotlinJs` are set to `enabled = false` in `build.gradle.kts`). See [TESTING.md](TESTING.md).

## Deployment

### Production Build

From repo root:

```bash
./gradlew :frontend:jsBrowserProductionWebpack
```

Output will be in `frontend/build/distributions/`.

### Serve Static Files

```bash
# From repo root after production build
cd frontend/build/distributions
python3 -m http.server 8080

# Or
npx serve frontend/build/distributions
```

## Troubleshooting

### Module Loading Issues

If you see module loading errors, ensure all Kotlin/JS modules are loaded in correct order in `index.html`.

### CORS Issues

Configure CORS on backend or use proxy in development:

```kotlin
window.ENV_API_BASE_URL = "http://localhost:18000"
```

### Styling Issues

Clear browser cache and rebuild:

```bash
./gradlew clean
./gradlew :frontend:build
```

## Contributing

1. Follow the architecture patterns
2. Write tests for new features
3. Update documentation
4. Use meaningful commit messages

## License

See LICENSE file in root directory.
