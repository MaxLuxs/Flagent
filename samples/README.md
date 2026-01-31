# Flagent Samples

Эта папка содержит примеры использования Flagent SDK и плагинов для различных платформ и языков программирования.

## Доступные примеры

### Android Sample (`android/`)

Пример Android приложения, демонстрирующий использование Flagent Kotlin SDK для Android.

**Особенности:**
- Single flag evaluation
- Batch evaluation
- Enhanced SDK с кэшированием
- Debug UI

**Запуск:**
```bash
cd samples/android
./gradlew build
```

### JavaScript Sample (`javascript/`)

Веб-приложение с HTML/JavaScript, демонстрирующее использование Flagent API.

**Особенности:**
- Single flag evaluation
- Batch evaluation
- Entity context support
- Debug mode

**Запуск:**
```bash
# Откройте index.html в браузере
open samples/javascript/index.html
```

### Kotlin Sample (`kotlin/`)

Консольное Kotlin/JVM приложение, демонстрирующее использование Flagent Kotlin SDK.

**Особенности:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Basic и Enhanced SDK

**Запуск:**
```bash
cd samples/kotlin
./gradlew run
```

### Swift Sample (`swift/`)

Консольное Swift приложение, демонстрирующее использование Flagent Swift SDK.

**Особенности:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Basic и Enhanced SDK

**Запуск:**
```bash
cd samples/swift
swift build
swift run
```

### Ktor Plugin Sample (`ktor/`)

Пример Ktor приложения, демонстрирующий использование Flagent Ktor плагина.

**Особенности:**
- Flagent plugin integration
- Custom endpoints с evaluation
- Batch evaluation
- Cache usage
- Plugin endpoints

**Запуск:**
```bash
cd samples/ktor
./gradlew run
```

## Общие требования

Перед запуском любого примера убедитесь, что Flagent backend сервер запущен:

```bash
cd backend
./gradlew run
```

Сервер запустится на `http://localhost:18000` по умолчанию.

## Структура

```
samples/
├── android/          # Android sample application
├── javascript/       # JavaScript/HTML sample
├── kotlin/           # Kotlin/JVM sample
├── swift/            # Swift sample
├── ktor/             # Ktor plugin sample
└── README.md         # Этот файл
```

## Лицензия

Apache 2.0 - См. лицензию родительского проекта
