# Flagent Java Client

Java client for the Flagent API (feature flags, evaluation, health). Generated from the OpenAPI spec; uses Java 11+ `HttpClient` and Jackson.

## Requirements

- Java 17+
- Run `./generate.sh` once (or when API changes) to generate code from `docs/api/openapi.yaml`. Requires [OpenAPI Generator](https://openapi-generator.tech/) (e.g. `npx @openapitools/openapi-generator-cli`).

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
  <version>0.1.4</version>
</dependency>
```

Or with Gradle:

```kotlin
implementation("com.flagent:flagent-java-client:0.1.4")
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
