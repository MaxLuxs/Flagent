# Конфигурация

> [English](../../guides/configuration.md)

Flagent можно конфигурировать с помощью переменных окружения. Все параметры конфигурации загружаются при запуске из переменных окружения.

## Переменные окружения

Все настройки Flagent конфигурируются через переменные окружения. См. [AppConfig.kt](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/config/AppConfig.kt) для полного списка опций конфигурации.

## Конфигурация сервера

### Базовые настройки

```bash
# Хост сервера (по умолчанию: localhost)
HOST=0.0.0.0

# Порт сервера (по умолчанию: 18000)
PORT=18000

# Окружение (development, staging, production)
ENVIRONMENT=production
```

### Статические файлы (Frontend UI)

Когда backend отдаёт admin UI из того же процесса, он ищет статику в нескольких путях. Используйте `FLAGENT_STATIC_DIR` для переопределения (например, в Docker):

```bash
# Абсолютный путь к директории со собранным frontend (index.html, frontend.js и т.д.)
# При установке backend отдаёт UI из этой директории. Используется в Docker-образе (/app/static).
FLAGENT_STATIC_DIR=/app/static
```

Если не задано, backend ищет относительно рабочей директории: `frontend/build/kotlin-webpack/js/productionExecutable`, `frontend/build/dist/js/productionExecutable` и т.д.

### Логирование

```bash
# Уровень логирования (debug, info, warn, error)
FLAGENT_LOGRUS_LEVEL=info

# Формат логов (text, json)
FLAGENT_LOGRUS_FORMAT=json

# Включить pprof (для профилирования)
FLAGENT_PPROF_ENABLED=false
```

## Конфигурация базы данных

Flagent поддерживает базы данных PostgreSQL, MySQL и SQLite.

### PostgreSQL (Рекомендуется для Production)

```bash
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent?user=user&password=password&sslmode=disable
```

**Формат:** JDBC URL: `jdbc:postgresql://host:port/database?user=...&password=...`

**Параметры:**
- `sslmode` - Режим SSL (disable, require, verify-full)
- `connect_timeout` - Таймаут подключения в секундах

### MySQL

```bash
FLAGENT_DB_DBDRIVER=mysql
FLAGENT_DB_DBCONNECTIONSTR=jdbc:mysql://localhost:3306/flagent?user=root&password=root&parseTime=true
```

**Формат строки подключения:**
```
[user[:password]@]tcp([host][:port])[/database][?param1=value1&...]
```

**Параметры:**
- `parseTime` - Парсить строки времени/даты (true/false)
- `charset` - Набор символов (utf8mb4, latin1)
- `timeout` - Таймаут подключения

### SQLite (Только для разработки)

```bash
FLAGENT_DB_DBDRIVER=sqlite3
FLAGENT_DB_DBCONNECTIONSTR=/path/to/flagent.sqlite
```

**Примечание:** SQLite рекомендуется только для разработки. Используйте PostgreSQL или MySQL для production.

### Пулинг подключений

Пул подключений управляется HikariCP (по умолчанию: 10 подключений). Размер пула задаётся в `Database.kt` и не настраивается через переменные окружения. См. [Database.kt](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/repository/Database.kt).

## Конфигурация Middleware

### Gzip сжатие

```bash
# Включить Gzip сжатие (по умолчанию: true)
FLAGENT_MIDDLEWARE_GZIP_ENABLED=true
```

### Подробное логирование

```bash
# Включить подробное логирование запросов (по умолчанию: true)
FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_ENABLED=true

# Исключить URL из подробного логирования (через запятую)
FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_EXCLUDE_URLS=/health,/metrics
```

### Ограничение частоты запросов

```bash
# Ограничитель частоты для консольного логирования на флаг в секунду (по умолчанию: 100)
FLAGENT_RATELIMITER_PERFLAG_PERSECOND_CONSOLE_LOGGING=100
```

## Конфигурация оценки

### Режим отладки

```bash
# Включить режим отладки для оценок (по умолчанию: true)
FLAGENT_EVAL_DEBUG_ENABLED=true

# Включить логирование оценок (по умолчанию: true)
FLAGENT_EVAL_LOGGING_ENABLED=true
```

### Конфигурация кэша

```bash
# Таймаут обновления кэша (по умолчанию: 59s)
FLAGENT_EVALCACHE_REFRESHTIMEOUT=59s

# Интервал обновления кэша (по умолчанию: 3s)
FLAGENT_EVALCACHE_REFRESHINTERVAL=3s
```

**Формат длительности:**
- `30s` - 30 секунд
- `5m` - 5 минут
- `1h` - 1 час

### Режим только оценки

```bash
# Режим только оценки (read-only, без мутаций)
FLAGENT_EVAL_ONLY_MODE=false
```

При включении Flagent будет обрабатывать только запросы на оценку и отклонять все запросы на изменение (create, update, delete).

## Метрики и мониторинг

### Prometheus метрики

```bash
# Включить Prometheus endpoint (по умолчанию: false)
FLAGENT_PROMETHEUS_ENABLED=true

# Путь для Prometheus метрик (по умолчанию: /metrics)
FLAGENT_PROMETHEUS_PATH=/metrics
```

После включения метрики доступны по адресу: `http://localhost:18000/metrics`

### StatsD метрики

```bash
# Включить StatsD метрики (по умолчанию: false)
FLAGENT_STATSD_ENABLED=true

# StatsD хост (по умолчанию: 127.0.0.1)
FLAGENT_STATSD_HOST=127.0.0.1

# StatsD порт (по умолчанию: 8125)
FLAGENT_STATSD_PORT=8125

# StatsD префикс (по умолчанию: flagent.)
FLAGENT_STATSD_PREFIX=flagent.
```

### Health Check

Health check endpoint всегда доступен по адресу: `http://localhost:18000/api/v1/health`

## Конфигурация аутентификации

### JWT аутентификация

```bash
FLAGENT_JWT_AUTH_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=your-secret-key
```

### Базовая аутентификация

```bash
FLAGENT_BASIC_AUTH_ENABLED=true
FLAGENT_BASIC_AUTH_USERNAME=admin
FLAGENT_BASIC_AUTH_PASSWORD=admin
```

### Аутентификация через заголовок

Идентификатор пользователя берётся из заголовка запроса (например X-Email для Cloudflare Access):

```bash
FLAGENT_HEADER_AUTH_ENABLED=true
FLAGENT_HEADER_AUTH_USER_FIELD=X-Email
```

### Аутентификация через cookie

```bash
FLAGENT_COOKIE_AUTH_ENABLED=true
FLAGENT_COOKIE_AUTH_USER_FIELD=CF_Authorization
```

### Enterprise Dev Mode (только для локальной разработки)

> **ПРЕДУПРЕЖДЕНИЕ БЕЗОПАСНОСТИ: Никогда не устанавливайте в production.** Отключает проверку X-API-Key и изоляцию мультитенантности.

```bash
# Включить режим разработки (требуется для dev-функций)
FLAGENT_DEV_MODE=true

# При FLAGENT_DEV_MODE=true: X-API-Key опционален, используется первый активный tenant
# Обе переменные обязательны. Создайте tenant через POST /admin/tenants.
FLAGENT_DEV_SKIP_TENANT_AUTH=true
```

## Admin Auth (Enterprise)

При включении маршруты `/admin/*` (создание/список tenants) требуют JWT от `POST /auth/login` (email/пароль админа) или заголовок `X-Admin-Key`. **По умолчанию включено** для безопасности.

```bash
# По умолчанию true. Установите false для открытого доступа (только dev).
FLAGENT_ADMIN_AUTH_ENABLED=true
FLAGENT_ADMIN_EMAIL=admin@example.com
FLAGENT_ADMIN_PASSWORD=your-secret-password
FLAGENT_ADMIN_API_KEY=your-admin-api-key   # опционально
FLAGENT_JWT_AUTH_SECRET=at-least-32-characters-secret
```

**Шаги:** 1) Задать переменные. 2) Открыть UI → Вход (email/пароль) или использовать `X-Admin-Key`. 3) Tenants → Создать первого tenant → использовать выданный API key для `/api/v1/*`.

## Конфигурация записи данных

### Kafka

```bash
# Включить запись в Kafka (по умолчанию: false)
FLAGENT_KAFKA_ENABLED=true

# Kafka брокеры (через запятую)
FLAGENT_KAFKA_BROKERS=localhost:9092

# Kafka топик (по умолчанию: flagent-evaluations)
FLAGENT_KAFKA_TOPIC=flagent-evaluations

# Kafka client ID (по умолчанию: flagent)
FLAGENT_KAFKA_CLIENT_ID=flagent

# Тип сжатия Kafka (none, gzip, snappy, lz4, zstd)
FLAGENT_KAFKA_COMPRESSION_TYPE=gzip
```

### AWS Kinesis

```bash
# Включить запись в Kinesis (по умолчанию: false)
FLAGENT_KINESIS_ENABLED=true

# Имя Kinesis stream
FLAGENT_KINESIS_STREAM_NAME=flagent-evaluations

# AWS регион (по умолчанию: us-east-1)
FLAGENT_KINESIS_REGION=us-east-1

# AWS учетные данные (или использовать IAM role)
FLAGENT_KINESIS_ACCESS_KEY_ID=your-access-key
FLAGENT_KINESIS_SECRET_ACCESS_KEY=your-secret-key
```

### Google Pub/Sub

```bash
# Включить запись в Pub/Sub (по умолчанию: false)
FLAGENT_PUBSUB_ENABLED=true

# Pub/Sub project ID
FLAGENT_PUBSUB_PROJECT_ID=your-project-id

# Имя Pub/Sub топика
FLAGENT_PUBSUB_TOPIC_NAME=flagent-evaluations

# Путь к файлу учетных данных Google
FLAGENT_PUBSUB_CREDENTIALS_FILE=/path/to/credentials.json
```

## Интеграция Firebase

### Синхронизация Firebase Remote Config

Синхронизирует флаги Flagent в Firebase Remote Config, чтобы мобильные приложения продолжали читать из Firebase, а Flagent оставался источником правды.

```bash
# Включить синхронизацию (по умолчанию: false)
FLAGENT_FIREBASE_RC_SYNC_ENABLED=true

# ID проекта Firebase (обязательно при включённой синхронизации)
FLAGENT_FIREBASE_RC_PROJECT_ID=your-firebase-project-id

# Учётные данные сервисного аккаунта: JSON или путь к файлу
FLAGENT_FIREBASE_RC_CREDENTIALS_JSON={"type":"service_account",...}
# Или:
FLAGENT_FIREBASE_RC_CREDENTIALS_FILE=/path/to/service-account.json
# Или переменная GOOGLE_APPLICATION_CREDENTIALS

# Интервал синхронизации (по умолчанию: 5s). Примеры: 5s, 1m, 5m
FLAGENT_FIREBASE_RC_SYNC_INTERVAL=5m

# Опциональный префикс для ключей параметров в Firebase RC (напр. flagent_)
FLAGENT_FIREBASE_RC_PARAMETER_PREFIX=flagent_
```

**Маппинг:** Один флаг Flagent → один параметр Firebase RC. Boolean-флаги → `"true"`/`"false"`. Эксперименты → JSON `{"variant":"control","attachment":{...}}`. Лимит Firebase RC: 2000 параметров.

### Firebase Analytics (GA4 Measurement Protocol)

Отправляет события оценок в GA4 для отображения в том же property, что и Firebase Analytics.

```bash
# Включить reporter (по умолчанию: false)
FLAGENT_FIREBASE_ANALYTICS_ENABLED=true

# API secret Measurement Protocol (GA4 Admin → Data streams → Measurement Protocol)
FLAGENT_FIREBASE_ANALYTICS_API_SECRET=your-api-secret

# Measurement ID (напр. G-XXXXXXXXXX) или Firebase App ID для мобильных
FLAGENT_FIREBASE_ANALYTICS_MEASUREMENT_ID=G-XXXXXXXXXX

# Ключи в entityContext для идентификации пользователя в GA4 (по умолчанию: app_instance_id, client_id)
FLAGENT_FIREBASE_ANALYTICS_APP_INSTANCE_ID_KEY=app_instance_id
FLAGENT_FIREBASE_ANALYTICS_CLIENT_ID_KEY=client_id
```

**Требуется:** Передавать `app_instance_id` (Firebase app) или `client_id` (web) в `entityContext` при evaluation, чтобы события привязывались к пользователю в GA4.

При включённом UI админки вы можете посмотреть текущий статус интеграции Firebase (только для чтения) в разделе **Settings → Firebase**.

## Примеры файлов конфигурации

### Разработка

```bash
# .env.development
HOST=localhost
PORT=18000
ENVIRONMENT=development
FLAGENT_DB_DBDRIVER=sqlite3
FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
FLAGENT_LOGRUS_LEVEL=debug
FLAGENT_LOGRUS_FORMAT=text
FLAGENT_EVAL_DEBUG_ENABLED=true
FLAGENT_PROMETHEUS_ENABLED=false
```

### Production

```bash
# .env.production
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=production
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://db:5432/flagent?user=user&password=password&sslmode=require
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_PROMETHEUS_PATH=/metrics
FLAGENT_JWT_AUTH_SECRET=your-secure-secret-key
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=kafka1:9092,kafka2:9092
FLAGENT_RECORDER_KAFKA_TOPIC=flagent-records
```

## Валидация конфигурации

Flagent валидирует конфигурацию при запуске. Если какая-либо обязательная конфигурация отсутствует или невалидна, приложение не запустится с понятным сообщением об ошибке.

## Приоритет переменных окружения

1. Системные переменные окружения (наивысший приоритет)
2. Файл `.env` в рабочей директории
3. Значения по умолчанию (наименьший приоритет)

## Лучшие практики безопасности

1. **Никогда не коммитьте секреты** - Используйте переменные окружения или системы управления секретами
2. **Используйте безопасные строки подключения** - Включайте SSL для подключений к базе данных в production
3. **Регулярно ротируйте секреты** - Периодически меняйте JWT секреты и API ключи
4. **Ограничьте доступ к базе данных** - Используйте пользователей БД с минимальными необходимыми правами
5. **Включайте аутентификацию** - Всегда включайте аутентификацию в production
6. **Используйте HTTPS** - Настройте reverse proxy с HTTPS в production

## Следующие шаги

- 📖 [Руководство по развертыванию](deployment.ru.md) - Узнайте, как развернуть Flagent
- 🏗️ [Архитектура](../architecture/backend.md) - Поймите архитектуру Flagent
- 📚 [Документация API](../api/endpoints.md) - Изучите API endpoints
