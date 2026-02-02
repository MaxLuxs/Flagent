# Testing Guide

## Test Structure

Tests are organized in `src/jsTest/kotlin/` with the same package structure as main code.

## Running Tests

Run from the **repository root**:

```bash
# Run all tests
./gradlew :frontend:jsTest

# Run specific test
./gradlew :frontend:jsTest --tests "ErrorHandlerTest"

# Run with coverage
./gradlew :frontend:jsTest --coverage
```

**Note:** In `frontend/build.gradle.kts`, the tasks `jsBrowserTest` and `compileTestKotlinJs` are currently **disabled** (`enabled = false`) to avoid Kotlin MPP + Compose ordering issues in the global build. So `./gradlew :frontend:jsTest` may not execute the browser tests by default. To run JS tests, re-enable these tasks in `build.gradle.kts` and run `./gradlew :frontend:jsBrowserTest` when needed.

## Test Categories

### Unit Tests

Test individual components in isolation:

- **Utilities** - Logger, ErrorHandler, Validation
- **ViewModels** - Business logic with mocked ApiClient
- **State** - GlobalState, notification management

### Integration Tests

Test component interactions:

- **API Integration** - ApiClient with test backend
- **Navigation** - Route changes and browser history
- **Component Lifecycle** - Mount, update, unmount

### E2E Tests (Playwright)

End-to-end user flows in `frontend/e2e/`:

- **Landing page** - Title, description, navigation buttons
- **Dashboard** - Stats, layout
- **Flags** - List, create flag, flag detail
- **Debug Console** - Evaluation testing
- **Navigation** - Navbar, URL routing

**Run E2E tests:**

```bash
# 1. Start backend + frontend (from repo root)
./gradlew run

# 2. In another terminal
cd frontend/e2e && npm install && npm run test

# Or use Gradle task (servers must be running)
./gradlew :frontend:e2e
```

**CI mode** (auto-starts servers):

```bash
cd frontend/e2e && CI=true npm run test
```

See `frontend/e2e/README.md` for details.

## Writing Tests

### Unit Test Example

```kotlin
class MyViewModelTest {
    @Test
    fun testLoadData() {
        val viewModel = MyViewModel()
        
        // Act
        viewModel.loadData()
        
        // Assert
        assertTrue(viewModel.isLoading)
    }
}
```

### Component Test Example

```kotlin
@Test
fun testButtonClick() = runTest {
    composition {
        var clicked = false
        Button({ onClick { clicked = true } }) {
            Text("Click me")
        }
        
        // Simulate click
        // Assert
        assertTrue(clicked)
    }
}
```

## Best Practices

1. **Arrange-Act-Assert** - Structure tests clearly
2. **Single Responsibility** - One assertion per test
3. **Descriptive Names** - Test names explain what they test
4. **Isolated Tests** - Tests don't depend on each other
5. **Mock External Dependencies** - Mock API, LocalStorage, etc.

## Coverage Goals

- **Utilities**: 90%+
- **ViewModels**: 80%+
- **Components**: 70%+
- **Overall**: 75%+

## CI/CD Integration

When JS tests are enabled, they can run on:
- Pull request creation
- Commit to main branch
- Release tag creation

(Currently JS test tasks are disabled in the default build; see note above.)

## Debugging Tests

```bash
# Run with verbose output
./gradlew :frontend:jsTest --info

# Run with stack traces
./gradlew :frontend:jsTest --stacktrace
```

## Known Issues

- SSE testing requires EventSource polyfill
- Some browser APIs need mocking in Node environment
