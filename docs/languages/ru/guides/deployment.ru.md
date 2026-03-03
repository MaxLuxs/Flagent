# Руководство по развертыванию

> [English](../../guides/deployment.md)

Это руководство охватывает развертывание Flagent в различных окружениях — от локальной разработки до production.

## Требования

- **JDK 21+** для запуска приложения
- **База данных**: PostgreSQL (рекомендуется), MySQL или SQLite (только для разработки)
- **Docker** (опционально, для контейнеризированного развертывания)
- **Kubernetes** (опционально, для Kubernetes развертывания)

## Быстрый старт с Docker

Самый быстрый способ начать работу с Flagent:

```bash
# Скачать Docker образ
docker pull ghcr.io/maxluxs/flagent

# Запустить Flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent

# Открыть Flagent UI
open http://localhost:18000
```

**Данные по умолчанию:**
- Email: `admin@local`
- Пароль: `admin`

## Docker Compose (с PostgreSQL)

Для полной настройки с PostgreSQL:

```bash
# Клонировать репозиторий
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent

# Запустить с Docker Compose
docker compose up -d

# Открыть UI
open http://localhost:18000
```

См. `docker-compose.yml` для полной конфигурации.

## Self-Hosted развертывание

### Сборка из исходников

1. **Клонировать репозиторий**

   ```bash
   git clone https://github.com/MaxLuxs/Flagent.git
   cd Flagent
   ```

2. **Собрать приложение**

   **Вариант A: installDist (скрипт)**

   ```bash
   ./gradlew :backend:installDist
   ```

   Создаётся `backend/build/install/backend/`. Запуск:

   ```bash
   ./backend/build/install/backend/bin/backend
   ```

   **Вариант B: Fat JAR**

   ```bash
   ./gradlew :backend:shadowJar
   ```

   Создаётся `backend/build/libs/backend-<version>-all.jar`. Подставьте версию из корневого файла `VERSION`. Запуск:

   ```bash
   # из корня репозитория
   java -jar backend/build/libs/backend-$(cat VERSION)-all.jar
   ```

   Или вручную, например для версии 0.1.6:

   ```bash
   java -jar backend/build/libs/backend-0.1.6-all.jar
   ```

   Версию см. в корневом файле `VERSION`.

### Конфигурация

Все настройки конфигурируются через переменные окружения. См. [Руководство по конфигурации](configuration.ru.md) для всех опций.

**Базовая конфигурация:**

```bash
export HOST=0.0.0.0
export PORT=18000
export FLAGENT_DB_DBDRIVER=postgres
export FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent?user=user&password=password
export FLAGENT_LOGRUS_LEVEL=info
export FLAGENT_LOGRUS_FORMAT=json

java -jar backend/build/libs/backend-$(cat VERSION)-all.jar
```

(Версию см. в корневом файле `VERSION`.)

## Production настройка

### Конфигурация базы данных

**PostgreSQL (Рекомендуется для Production):**

```bash
export FLAGENT_DB_DBDRIVER=postgres
export FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://db-host:5432/flagent?user=user&password=password&sslmode=require
```

**MySQL:**

```bash
export FLAGENT_DB_DBDRIVER=mysql
export FLAGENT_DB_DBCONNECTIONSTR=jdbc:mysql://db-host:3306/flagent?user=user&password=password&parseTime=true
```

### Конфигурация безопасности

**Включить аутентификацию:**

```bash
# JWT аутентификация
export FLAGENT_JWT_AUTH_ENABLED=true
export FLAGENT_JWT_AUTH_SECRET=your-secure-secret-key

# Или базовая аутентификация
export FLAGENT_BASIC_AUTH_ENABLED=true
export FLAGENT_BASIC_AUTH_USERNAME=admin
export FLAGENT_BASIC_AUTH_PASSWORD=secure-password

# Или через заголовок (пользователь из X-Email и т.п.)
export FLAGENT_HEADER_AUTH_ENABLED=true
export FLAGENT_HEADER_AUTH_USER_FIELD=X-Email
```

### Конфигурация мониторинга

**Включить Prometheus метрики:**

```bash
export FLAGENT_PROMETHEUS_ENABLED=true
export FLAGENT_PROMETHEUS_PATH=/metrics
```

Метрики будут доступны по адресу `http://localhost:18000/metrics`

**Включить StatsD метрики:**

```bash
export FLAGENT_STATSD_ENABLED=true
export FLAGENT_STATSD_HOST=127.0.0.1
export FLAGENT_STATSD_PORT=8125
export FLAGENT_STATSD_PREFIX=flagent.
```

### Конфигурация логирования

**Production логирование:**

```bash
export FLAGENT_LOGRUS_LEVEL=info
export FLAGENT_LOGRUS_FORMAT=json
export FLAGENT_EVAL_DEBUG_ENABLED=false
export FLAGENT_EVAL_LOGGING_ENABLED=true
```

## Kubernetes развертывание

Flagent без состояния (stateless) и может быть развернут в Kubernetes. Вот базовая конфигурация Kubernetes:

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flagent
spec:
  replicas: 3
  selector:
    matchLabels:
      app: flagent
  template:
    metadata:
      labels:
        app: flagent
    spec:
      containers:
      - name: flagent
        image: ghcr.io/maxluxs/flagent:latest
        ports:
        - containerPort: 18000
        env:
        - name: HOST
          value: "0.0.0.0"
        - name: PORT
          value: "18000"
        - name: FLAGENT_DB_DBDRIVER
          value: "postgres"
        - name: FLAGENT_DB_DBCONNECTIONSTR
          valueFrom:
            secretKeyRef:
              name: flagent-secrets
              key: database-url
        - name: FLAGENT_JWT_AUTH_SECRET
          valueFrom:
            secretKeyRef:
              name: flagent-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /api/v1/health
            port: 18000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/health
            port: 18000
          initialDelaySeconds: 10
          periodSeconds: 5
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: flagent
spec:
  selector:
    app: flagent
  ports:
  - port: 80
    targetPort: 18000
  type: LoadBalancer
```

### Secrets

Создать секреты для конфиденциальной конфигурации:

```bash
kubectl create secret generic flagent-secrets \
  --from-literal=database-url=postgresql://user:password@db:5432/flagent \
  --from-literal=jwt-secret=your-secret-key
```

## Docker развертывание

### Dockerfile

Flagent включает Dockerfile для контейнеризированного развертывания:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY backend/build/libs/backend-*-all.jar app.jar
EXPOSE 18000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Сборка и запуск

```bash
# Собрать Docker образ
docker build -t flagent:latest .

# Запустить контейнер
docker run -d \
  -p 18000:18000 \
  -e FLAGENT_DB_DBDRIVER=postgres \
  -e FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://db:5432/flagent?user=user&password=password \
  flagent:latest
```

## Конфигурация для разных окружений

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

### Staging

```bash
# .env.staging
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=staging
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://staging-db:5432/flagent?user=user&password=password
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=staging-secret-key
```

### Production

```bash
# .env.production
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=production
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://prod-db:5432/flagent?user=user&password=password&sslmode=require
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_STATSD_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=production-secret-key
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=kafka1:9092,kafka2:9092
```

## Масштабирование

### Горизонтальное масштабирование

Flagent без состояния и может быть масштабирован горизонтально:

1. **Развернуть несколько экземпляров** за load balancer
2. **Использовать общую БД** (PostgreSQL или MySQL)
3. **Настроить sticky sessions** если нужно (опционально)

**Пример с Docker Compose:**

```yaml
version: '3.8'
services:
  flagent:
    image: ghcr.io/maxluxs/flagent:latest
    deploy:
      replicas: 3
    ports:
      - "18000:18000"
    environment:
      - FLAGENT_DB_DBDRIVER=postgres
      - FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://db:5432/flagent?user=user&password=password
```

### Вертикальное масштабирование

Увеличить ресурсы для лучшей производительности:

- **Память**: 2GB+ рекомендуется для production
- **CPU**: 2+ ядра рекомендуется для высокого трафика
- **База данных**: HikariCP пул (по умолчанию 10 подключений; см. Database.kt для настройки)

## Health проверки

Flagent предоставляет health check endpoints:

### Health Endpoint

```bash
curl http://localhost:18000/api/v1/health
```

Ответ:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Metrics Endpoint

```bash
curl http://localhost:18000/metrics
```

Возвращает метрики в формате Prometheus.

## Мониторинг

### Интеграция с Prometheus

Flagent экспортирует Prometheus метрики на `/metrics`. Настройте Prometheus для сбора:

```yaml
scrape_configs:
  - job_name: 'flagent'
    static_configs:
      - targets: ['flagent:18000']
```

### Grafana дашборды

Создайте Grafana дашборды используя Prometheus метрики:
- Количество и латентность HTTP запросов
- Количество оценок по флагам
- Процент ошибок
- Метрики пула подключений к БД

### Логирование

Настройте агрегацию логов:
- **JSON формат** для структурированных логов
- **Уровни логов**: debug (dev), info (prod)
- **Централизованное логирование**: Отправка логов в ELK, Splunk или аналоги

## Резервное копирование и восстановление

### Резервные копии базы данных

**PostgreSQL:**

```bash
# Резервная копия
pg_dump -h db-host -U user flagent > flagent-backup.sql

# Восстановление
psql -h db-host -U user flagent < flagent-backup.sql
```

**MySQL:**

```bash
# Резервная копия
mysqldump -h db-host -u user -p flagent > flagent-backup.sql

# Восстановление
mysql -h db-host -u user -p flagent < flagent-backup.sql
```

### Point-in-Time восстановление

Настройте PostgreSQL или MySQL для point-in-time восстановления:
- Включить WAL архивирование (PostgreSQL)
- Включить binary логирование (MySQL)
- Регулярные резервные копии с политикой хранения

## Устранение неполадок

### Приложение не запускается

1. **Проверить подключение к БД**
   ```bash
   # Тест PostgreSQL подключения
   psql postgresql://user:password@host:5432/flagent
   ```

2. **Проверить доступность порта**
   ```bash
   # Проверить, занят ли порт
   lsof -i :18000
   ```

3. **Проверить логи**
   ```bash
   # Посмотреть логи приложения
   docker logs flagent
   ```

### Проблемы с производительностью

1. **Пулинг подключений к БД**
   - Размер пула настраивается в `Database.kt` (по умолчанию: 10). Измените `maximumPoolSize` для высокой нагрузки.
   - Проверить подключения к БД: `SELECT count(*) FROM pg_stat_activity;`

2. **Конфигурация кэша**
   - Включить кэширование: `FLAGENT_EVALCACHE_REFRESHINTERVAL=3s`
   - Проверить cache hit rates в метриках

3. **Лимиты ресурсов**
   - Увеличить память: `-Xmx2g`
   - Увеличить количество CPU ядер

### Распространенные проблемы

**Проблема**: Ошибки подключения к БД

**Решение**: Проверить URL БД, учетные данные и сетевую связность

**Проблема**: Высокое использование памяти

**Решение**: Увеличить heap size или уменьшить размер кэша

**Проблема**: Медленное время оценки

**Решение**: Включить кэширование, оптимизировать запросы к БД, проверить сетевую латентность

## Следующие шаги

- 📖 [Руководство по конфигурации](configuration.ru.md) - Полные опции конфигурации
- 🏗️ [Архитектура](../architecture/backend.md) - Поймите архитектуру Flagent
- 📚 [Документация API](../api/endpoints.md) - Изучите API endpoints
- 💻 [Примеры](../examples/README.ru.md) - Примеры кода и туториалы
