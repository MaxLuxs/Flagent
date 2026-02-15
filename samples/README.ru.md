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
# Из корня monorepo
./gradlew :sample-kotlin:run
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

### Flutter Sample (`flutter/`)

Консольное Dart приложение, демонстрирующее использование Flagent Flutter/Dart SDK.

**Особенности:**
- Single flag evaluation
- Batch evaluation
- Flag ID evaluation
- Enhanced SDK с кэшированием

**Запуск:**
```bash
cd samples/flutter
dart pub get
dart run bin/main.dart
```

### Flutter App Sample (`flutter_app/`)

Flutter приложение **с UI** (Material): экраны Home, Evaluate и Debug.

**Особенности:**
- Форма оценки флага и отображение результата
- Вкладка Debug: форма оценки, действия с кэшем (clear / evict), список последних оценок

**Запуск:**
```bash
cd samples/flutter_app
flutter create . --org com.flagent   # первый раз: сгенерировать android/ios
flutter pub get
flutter run
```

### React Native Sample (`react-native/`)

Эталонный код для использования Flagent SDK в React Native. Использует `@flagent/client` и `@flagent/enhanced-client`.

**См.:** [sdk/REACT_NATIVE.md](../sdk/REACT_NATIVE.md) для полного руководства по интеграции.

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
# Из корня monorepo
./gradlew :sample-ktor:run
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
├── android/          # Android sample (Compose UI, подключён SDK Debug UI)
├── javascript/       # JavaScript/HTML sample
├── kotlin/            # Kotlin/JVM консольный sample
├── swift/            # Swift консольный sample
├── flutter/          # Flutter/Dart консольный sample
├── flutter_app/      # Flutter приложение с UI (Home, Evaluate, Debug)
├── react-native/     # React Native reference
├── ktor/             # Ktor plugin sample
├── spring-boot/      # Spring Boot plugin sample
└── README.md         # Этот файл
```

## UI и Debug UI

| Sample      | Есть UI | Подключён SDK Debug UI |
|-------------|---------|------------------------|
| Android     | Да (Compose) | Да — вкладка Debug, `kotlin-debug-ui` |
| Flutter App | Да (Material) | Встроенная вкладка Debug (форма, кэш, последние оценки) |
| JavaScript  | Да (HTML) | Только чекбокс Debug mode (без React Debug Panel) |
| Swift       | Нет (CLI) | — |
| Flutter (консоль) | Нет (CLI) | — |
| Kotlin, Ktor, Spring Boot | Нет | — |

**Android** использует модуль SDK Debug UI (`kotlin-debug-ui`). **Flutter App** — свой экран Debug во Flutter; отдельного пакета Flutter Debug UI в SDK нет.

## Лицензия

Apache 2.0 - См. лицензию родительского проекта
