# Flagent Java Client

Java client for the Flagent API (feature flags, evaluation, health). Generated from the OpenAPI spec; uses Java 11+ `HttpClient` and Jackson.

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

## Usage

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
