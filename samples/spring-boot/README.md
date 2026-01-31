# Flagent Spring Boot Sample

Minimal Spring Boot app using the Flagent Spring Boot Starter.

## Run

1. Start Flagent server (e.g. from backend) on port 18000.
2. From repo root:

```bash
./gradlew :sample-spring-boot:bootRun
```

3. Call evaluation:

```bash
curl "http://localhost:8080/eval?flagKey=my_flag&entityId=user-1"
```

## Configuration

See `src/main/resources/application.yml`. Override `flagent.base-url` if Flagent runs elsewhere.
