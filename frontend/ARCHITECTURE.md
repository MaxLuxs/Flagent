# Flagent Frontend Architecture

## Overview

The Flagent frontend follows **Clean Architecture** principles with clear separation of concerns across layers.

## Architecture Layers

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (Components, UI, Navigation)       │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│      Application Layer              │
│  (ViewModels, State Management)     │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│      Infrastructure Layer           │
│  (ApiClient, Services, Utils)       │
└─────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer

**Location**: `components/`

**Responsibility**: Rendering UI and handling user interactions

**Components**:
- `common/` - Reusable UI components (Pagination, SkeletonLoader, EmptyState, etc.)
- `flags/` - Flag management UI
- `metrics/` - Metrics visualization (Enterprise-only when `AppConfig.Features.enableMetrics`)
- `rollout/` - Smart rollout UI (Enterprise-only)
- `anomaly/` - Anomaly alerts UI (Enterprise-only)
- `auth/` - Authentication UI
- `tenants/` - Multi-tenancy UI (Enterprise-only)
- `billing/` - Billing UI (Enterprise-only)
- `realtime/` - Real-time status indicators

Feature visibility is controlled by `AppConfig` (edition and feature flags). See [EDITION_GUIDE.md](EDITION_GUIDE.md).

**Rules**:
- Components are **stateless** where possible
- Use ViewModels for business logic
- Only handle UI rendering and events
- No direct API calls (use ViewModels)

### 2. Application Layer

**Location**: `viewmodel/`, `state/`

**Responsibility**: Business logic and state management

**Components**:
- `FlagsViewModel` - Flags list management
- `FlagEditorViewModel` - Flag editing logic
- `MetricsViewModel` - Metrics data management
- `SmartRolloutViewModel` - Rollout logic
- `AnomalyViewModel` - Anomaly detection logic
- `AuthViewModel` - Authentication logic
- `TenantViewModel` - Multi-tenancy logic
- `BillingViewModel` - Billing logic
- `RealtimeViewModel` - Real-time updates
- `GlobalState` - Application-wide state

**Rules**:
- ViewModels manage state with `mutableStateOf`
- All API calls go through ViewModels
- Error handling with ErrorHandler
- Logging with AppLogger
- Use coroutines for async operations

### 3. Infrastructure Layer

**Location**: `api/`, `service/`, `util/`, `config/`

**Responsibility**: External integrations and utilities

**Components**:
- `ApiClient` - Singleton HTTP client
- `RealtimeService` - SSE integration
- `ErrorHandler` - Centralized error handling
- `AppLogger` - Logging utility
- `AppConfig` - Configuration from environment

**Rules**:
- ApiClient is singleton with auth/tenant interceptors
- All HTTP calls use ErrorHandler
- Services are stateless
- Utilities are pure functions

## Data Flow

```
User Action
    ↓
Component Event Handler
    ↓
ViewModel Method
    ↓
ApiClient / Service
    ↓
Backend API
    ↓
Response
    ↓
ViewModel State Update
    ↓
Component Re-render
```

## State Management

### Local State

Use `remember` and `mutableStateOf` for component-local state:

```kotlin
val isOpen = remember { mutableStateOf(false) }
```

### ViewModel State

ViewModels expose state as `var` with `mutableStateOf`:

```kotlin
class MyViewModel {
    var data by mutableStateOf<List<Item>>(emptyList())
        private set
}
```

### Global State

Use `GlobalState` via `CompositionLocalProvider`:

```kotlin
val globalState = LocalGlobalState.current
globalState.addNotification(...)
```

## Error Handling

All errors are handled through `ErrorHandler`:

```kotlin
ErrorHandler.withErrorHandling(
    block = { /* API call */ },
    onError = { error ->
        // Handle error
        val message = ErrorHandler.getUserMessage(error)
    }
)
```

## Routing

Type-safe routing with sealed classes (`navigation/Router.kt`):

```kotlin
sealed class Route {
    object Home : Route()
    data class FlagDetail(val flagId: Int) : Route()
    object CreateFlag : Route()
    data class DebugConsole(val flagKey: String? = null) : Route()
    // ...
}
```

## API Integration

### Request Flow

1. Component calls ViewModel method
2. ViewModel uses ApiClient
3. ApiClient adds auth/tenant headers
4. Request sent to backend
5. Response handled in ViewModel
6. State updated, UI re-renders

### Error Handling

- HTTP errors mapped to `AppError` types
- User-friendly messages generated
- Errors logged for debugging
- UI shows error state

## Real-time Updates

SSE integration for live updates:

```kotlin
val viewModel = RealtimeViewModel { notification ->
    globalState.addNotification(notification)
}
viewModel.connect(flagKeys = listOf("my_flag"))
```

## Performance Optimizations

- **Lazy Loading**: Components load data on mount
- **Memoization**: Use `remember` for expensive computations
- **Pagination**: Large lists are paginated
- **Skeleton Loaders**: Show loading state without blocking UI
- **Debouncing**: Search inputs are debounced

## Testing Strategy

### Unit Tests

- ViewModels with mocked ApiClient
- Utilities with pure function tests
- Error handlers with various error types

### Integration Tests

- API integration with test backend
- Route navigation flows
- Component interactions

### E2E Tests (Future)

- Critical user flows
- Multi-step processes

## Security

- **Auth tokens** stored in localStorage
- **API keys** sent in headers
- **HTTPS** enforced in production
- **Input validation** on client side
- **XSS prevention** via Compose escaping

## Accessibility

- **ARIA attributes** on interactive elements
- **Keyboard navigation** support
- **Focus management** in modals
- **Semantic HTML** for screen readers
- **Color contrast** meets WCAG AA

## Future Improvements

- [ ] Virtualized lists for better performance
- [ ] Service workers for offline support
- [ ] WebSockets as alternative to SSE
- [ ] E2E test coverage
- [ ] Performance monitoring
- [ ] Error tracking integration (Sentry)
- [ ] Analytics integration
- [ ] A/B testing framework
