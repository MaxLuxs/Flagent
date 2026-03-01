# Flagent Java Client

Java client for the Flagent API (feature flags, evaluation, health). Generated from the OpenAPI spec; uses Java 11+ `HttpClient` and Jackson.

Version: see root [VERSION](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) or [Releases](https://github.com/MaxLuxs/Flagent/releases).

## Unified entry point from Java (recommended)

For a single entry point with caching and `evaluate` / `isEnabled` / `evaluateBatch`, use the **Kotlin Enhanced** SDK from Java. It runs on the JVM and exposes a blocking API:

```java
// Add dependency: com.flagent:kotlin-enhanced (same version as other Flagent libs)
import com.flagent.enhanced.entry.Flagent;
import com.flagent.enhanced.entry.FlagentClientBlocking;
import com.flagent.client.models.EvalResult;

FlagentClientBlocking client = Flagent.INSTANCE.builder()
    .baseUrl("https://api.example.com/api/v1")
    .cache(true, 300_000L)
    .buildBlocking();

EvalResult result = client.evaluate("new_feature", null, "user123", "user", null, false);
boolean on = client.isEnabled("new_feature", "user123", null, null);
```

Gradle:

```kotlin
implementation("com.flagent:kotlin-enhanced:0.1.6")
```

Maven:

```xml
<dependency>
  <groupId>com.flagent</groupId>
  <artifactId>kotlin-enhanced</artifactId>
  <version>0.1.6</version>
</dependency>
```

For **Spring Boot**, use the [Flagent Spring Boot Starter](../spring-boot-starter) (auto-configuration and `FlagentEvaluationFacade`). For **standalone** Java without Spring, use `kotlin-enhanced` with `buildBlocking()` as above.

## Low-level API (this library)

This artifact (`flagent-java-client`) is the generated OpenAPI client. Use it when you need direct access to `EvaluationApi`, `FlagApi`, etc.

## Requirements

- Java 17+
- Run `./generate.sh` once (or when API changes) to generate code from `docs/api/openapi.yaml`. Requires [OpenAPI Generator](https://openapi-generator.tech/) (e.g. `npx @openapitools/openapi-generator-cli`).

## Testing

Unit tests for models run by default. **API tests** (`*ApiTest.java`) are **disabled by default** because they require a live Flagent server. They are intended for integration runs only.

To run API tests as integration tests:

1. Start a Flagent server (e.g. `./gradlew :backend:run` or your deployment).
2. Remove or comment out `@Disabled("Integration test: requires live Flagent server")` on the API test classes (or run with a JUnit tag that includes them).
3. Configure the test to use the server base URL (e.g. set `ApiClient` base path to `http://localhost:18000/api/v1` in test setup).
4. Run: `./gradlew :java-client:test`.

CI runs unit tests only; API tests are not executed unless explicitly enabled and the server URL is provided.

## Build

From the Flagent repo root:

```bash
./gradlew :java-client:build
```

From this directory (after generation):

```bash
cd ../.. && ./gradlew :java-client:build
```

## Usage (low-level API)

Add dependency (when published):

```xml
<dependency>
  <groupId>com.flagent</groupId>
  <artifactId>flagent-java-client</artifactId>
  <version>0.1.6</version>
</dependency>
```

Or with Gradle:

```kotlin
implementation("com.flagent:flagent-java-client:0.1.6")
```

Example:

```java
var client = new ApiClient();
client.setBasePath("http://localhost:18000/api/v1");
var evaluationApi = new EvaluationApi(client);
var context = new EvalContext().entityID("user-1").flagKey("my_flag");
EvalResult result = evaluationApi.postEvaluation(context).getBody();
```

## Spring Boot

Use the [Flagent Spring Boot Starter](../spring-boot-starter) for auto-configuration and beans.
