# Testing SDK Clients

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

## Интеграционное тестирование

Для полного тестирования нужен запущенный Flagent сервер:

```bash
# Запустить сервер (из корня проекта)
cd backend
./gradlew run
```

Затем можно тестировать SDK против реального API.
