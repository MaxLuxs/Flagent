# Testing SDK Clients

## Покрытие тестами

| SDK | Тесты | Команда |
|-----|-------|---------|
| Kotlin (base) | Model + API | `./gradlew :kotlin-client:test` |
| Kotlin Enhanced | Manager, Cache, Evaluator, Storage, Realtime | `./gradlew :kotlin-enhanced:jvmTest` |
| Java | Model + API | `./gradlew :java-client:test` |
| JavaScript Enhanced | FlagentManager, EvaluationCache | `cd sdk/javascript-enhanced && npm test` |
| Go | client_test | `cd sdk/go && go test ./...` |
| Go Enhanced | evaluator, manager | `cd sdk/go-enhanced && go test ./...` |
| Dart (base) | Model + API (generated) | `cd sdk/dart && dart pub get && dart run build_runner build && dart test` |
| Flutter Enhanced | Cache, Manager, buildEvaluationEntity | `cd sdk/flutter-enhanced && dart pub get && dart test` |
| Swift Enhanced | FlagentEnhancedTests | `cd sdk/swift-enhanced && swift test` |
| Python | client, models | `cd sdk/python && pip install -e ".[dev]" && pytest` |
| JavaScript (base) | EvaluationApi, exports | `cd sdk/javascript && npm test` |

## Как проверить SDK

### 1. Проверка генерации

Сначала нужно сгенерировать SDK код из OpenAPI спецификации:

```bash
# Установить OpenAPI Generator (если еще не установлен)
npm install -g @openapitools/openapi-generator-cli

# Сгенерировать Kotlin SDK
cd sdk/kotlin
./generate.sh

# Сгенерировать JavaScript SDK
cd ../javascript
./generate.sh

# Или сгенерировать все сразу
cd ../../..
make -C sdk generate
```

### 2. Проверка Kotlin SDK

#### Сборка проекта

```bash
cd sdk/kotlin
./gradlew build
```

#### Простой тест

Создайте файл `src/test/kotlin/TestClient.kt`:

```kotlin
import com.flagent.client.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = HttpClient(CIO)
    val api = DefaultApi(
        baseUrl = "http://localhost:18000/api/v1",
        httpClient = client
    )
    
    try {
        // Тест health check
        val health = api.getHealth()
        println("Health: ${health.status}")
        
        // Тест получения флагов
        val flags = api.findFlags()
        println("Flags count: ${flags.size}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        client.close()
    }
}
```

Запустите тест (после сборки):
```bash
./gradlew run
```

### 3. Проверка JavaScript/TypeScript SDK

#### Установка зависимостей

```bash
cd sdk/javascript
npm install
```

#### Сборка

```bash
npm run build
```

#### Простой тест

Создайте файл `test.js`:

```javascript
import { Configuration, DefaultApi } from './dist/index.js';

const config = new Configuration({
  basePath: 'http://localhost:18000/api/v1',
});

const api = new DefaultApi(config);

// Тест health check
api.getHealth()
  .then(response => {
    console.log('Health:', response.data.status);
  })
  .catch(error => {
    console.error('Error:', error.message);
  });
```

Запустите:
```bash
node test.js
```

### 4. Dart / Flutter Enhanced

```bash
cd sdk/dart
dart pub get
dart test

cd ../flutter-enhanced
dart pub get
dart test
```

### 5. Go

```bash
cd sdk/go && go test ./...
cd ../go-enhanced && go test ./...
```

## Интеграционное тестирование

Для полного тестирования нужен запущенный Flagent сервер:

```bash
# Запустить сервер (из корня проекта)
cd backend
./gradlew run
```

Затем можно тестировать SDK против реального API.
