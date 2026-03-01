# Flagent Koin Module

Koin DI module for Flagent Kotlin client. Auto-configures `HttpClient`, `EvaluationApi`, and `FlagentManager`.

## Installation

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

```kotlin
dependencies {
    implementation("com.flagent:flagent-koin:0.1.6")
    implementation("io.insert-koin:koin-android:4.0.0")  // for Android
    implementation("io.insert-koin:koin-androidx-compose:4.0.0")  // for Compose ViewModel
}
```

## Usage

### Android

```kotlin
import com.flagent.koin.flagentClientModule
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(flagentClientModule(
                baseUrl = "http://10.0.2.2:18000",
                httpClientEngine = Android.create()
            ))
        }
    }
}

// In ViewModel or Composable
class MyViewModel : ViewModel() {
    private val manager: FlagentManager by inject()
    // ...
}
```

### JVM / Ktor / Desktop

```kotlin
import com.flagent.koin.flagentClientModule
import org.koin.core.context.startKoin

startKoin {
    modules(flagentClientModule(baseUrl = "http://localhost:18000"))
}

// Inject
val manager: FlagentManager by inject()
```

### Ktor Server

```kotlin
import com.flagent.koin.flagentKtorModule

startKoin {
    modules(flagentKtorModule {
        flagentBaseUrl = "http://localhost:18000"
        enableCache = true
    })
}
```

## Configuration

- `baseUrl` — Flagent API base URL
- `config` — `FlagentConfig` (cache TTL, etc.)
- `useEnhanced` — if true, registers `FlagentManager`; else only `EvaluationApi`
- `httpClientEngine` — pass `Android.create()` for Android; `null` for CIO (JVM)
