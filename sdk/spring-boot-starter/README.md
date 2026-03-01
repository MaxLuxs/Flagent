# Flagent Spring Boot Starter

Auto-configuration for the Flagent Java client in Spring Boot. Registers `ApiClient`, `EvaluationApi`, `HealthApi`, optional Caffeine cache for evaluation, `FlagentEvaluationFacade` (evaluate with optional cache), and when actuator is on classpath — `FlagentHealthIndicator`.

## Requirements

- Spring Boot 3.2+
- Java 17+

## Dependency

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

From the Flagent repo (when published to Maven):

```xml
<dependency>
  <groupId>com.flagent</groupId>
  <artifactId>flagent-spring-boot-starter</artifactId>
  <version>0.1.6</version>
</dependency>
```

Or Gradle:

```kotlin
implementation("com.flagent:flagent-spring-boot-starter:0.1.6")
```

## Configuration

`application.yml`:

```yaml
flagent:
  enabled: true
  base-url: http://localhost:18000
  connect-timeout-ms: 5000
  read-timeout-ms: 10000
  cache:
    enabled: true
    ttl-ms: 60000
```

Disable the client:

```yaml
flagent:
  enabled: false
```

## Usage

Inject `FlagentEvaluationFacade` (recommended — uses cache when enabled) or `EvaluationApi` / `HealthApi`:

```java
@Service
public class MyService {
    private final FlagentEvaluationFacade flagentFacade;

    public MyService(FlagentEvaluationFacade flagentEvaluationFacade) {
        this.flagentFacade = flagentEvaluationFacade;
    }

    public void checkFlag() throws ApiException {
        var context = new EvalContext().entityID("user-1").flagKey("my_flag");
        EvalResult result = flagentFacade.evaluate(context);
        // use result.getVariantKey(), etc.
    }
}
```

Bean names: `flagentApiClient`, `flagentEvaluationApi`, `flagentHealthApi`, `flagentEvaluationFacade`, optional `flagentEvalCache`. Use `@Qualifier("flagentEvaluationApi")` if you have multiple API clients.

## Actuator health

With `spring-boot-starter-actuator` on the classpath, `FlagentHealthIndicator` is auto-registered and Flagent health is exposed via `/actuator/health`.
