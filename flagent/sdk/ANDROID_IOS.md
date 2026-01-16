# Использование Flagent SDK на Android и iOS

## Решение

- **Android**: Используйте [Kotlin SDK](./kotlin/) - работает нативно на Android через JVM
- **iOS**: Используйте [Swift SDK](./swift/) - нативный Swift SDK для iOS

Оба SDK генерируются из одной OpenAPI спецификации и полностью совместимы.

## Использование

### Android - Kotlin SDK

#### Gradle (build.gradle.kts)

```kotlin
dependencies {
    implementation("com.flagent:flagent-kotlin-client:1.1.19")
}
```

#### Использование в Android

```kotlin
import com.flagent.client.*
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private val client = HttpClient(Android)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val api = DefaultApi(
            baseUrl = "https://api.example.com/api/v1",
            httpClient = client
        )
        
        lifecycleScope.launch {
            try {
                val flags = api.findFlags()
                // Использовать флаги
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}
```

### iOS - Swift SDK

#### Swift Package Manager

Добавьте в `Package.swift` или через Xcode:
```swift
.package(url: "https://github.com/MaxLuxs/Flagent.git", from: "1.1.19")
```

#### Использование в iOS

```swift
import FlagentClient

let api = DefaultAPI(
    Configuration(basePath: "https://api.example.com/api/v1")
)

// Использование с async/await
Task {
    do {
        let flags = try await api.findFlags()
        // Использовать флаги
    } catch {
        print("Error: \(error)")
    }
}
```

## Альтернативные варианты (если нужны)

### Вариант 1: Kotlin Multiplatform SDK

Создать KMP SDK, который будет работать на Android, iOS, и других платформах.

#### Создание KMP SDK

1. **Создать новую структуру** (`sdk/kotlin-multiplatform/`):

```bash
mkdir -p sdk/kotlin-multiplatform
```

2. **Обновить generate.sh** для использования `multiplatform` библиотеки:

```bash
openapi-generator-cli generate \
    -i "$OPENAPI_SPEC" \
    -g kotlin \
    -o "$OUTPUT_DIR" \
    --additional-properties=library=multiplatform,packageName=com.flagent.client,groupId=com.flagent,artifactId=flagent-kmp-client,artifactVersion=1.1.19,serializationLibrary=kotlinx_serialization,dateLibrary=kotlinx-datetime
```

3. **Настроить build.gradle.kts** для KMP:

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    android()
    ios()
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Ktor Client Multiplatform
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                
                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:2.3.7")
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.3.7")
            }
        }
    }
}
```

### Вариант 2: Kotlin Multiplatform SDK (для общего кода)

Текущий JVM SDK можно использовать на Android:

#### Android Gradle (build.gradle.kts)

```kotlin
dependencies {
    implementation("com.flagent:flagent-kotlin-client:1.1.19")
}
```

#### Использование в Android

```kotlin
import com.flagent.client.*
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private val client = HttpClient(Android)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val api = DefaultApi(
            baseUrl = "https://api.example.com/api/v1",
            httpClient = client
        )
        
        lifecycleScope.launch {
            try {
                val flags = api.findFlags()
                // Использовать флаги
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}
```

### Вариант 3: JavaScript SDK (для React Native или Web)

Для React Native или веб-приложений можно использовать JavaScript SDK:

```bash
npm install @flagent/client
```

## Рекомендация

**Для нативных приложений:**
- **Android**: Используйте Kotlin SDK (уже доступен)
- **iOS**: Используйте Swift SDK (уже доступен)

Оба SDK генерируются автоматически из OpenAPI спецификации и всегда синхронизированы с API.
