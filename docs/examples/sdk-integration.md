# Flagent Usage Examples

Quick reference for integrating Flagent in your application. See [samples/](../../samples) for full runnable examples.

## Summary by Platform

| Platform | Sample | Key Features |
|----------|--------|--------------|
| **Ktor** | [samples/ktor](../../samples/ktor) | Plugin, single/batch eval, entityContext |
| **Spring Boot** | [samples/spring-boot](../../samples/spring-boot) | Starter, single/batch eval, entityContext |
| **Kotlin** | [samples/kotlin](../../samples/kotlin) | SDK, Enhanced with cache, entityContext |
| **JavaScript** | [samples/javascript](../../samples/javascript) | Web UI, fetch API |
| **Swift** | [samples/swift](../../samples/swift) | Swift SDK, async/await |

## Ktor

```kotlin
// build.gradle.kts
implementation(project(":ktor-flagent"))

// Application.kt
installFlagent {
    flagentBaseUrl = "http://localhost:18000"
    enableEvaluation = true
    enableCache = true
}

routing {
    get("/feature/{flagKey}") {
        val client = call.application.getFlagentClient()
        val result = client?.evaluate(EvaluationRequest(
            flagKey = call.parameters["flagKey"]!!,
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "US", "tier" to "premium")
        ))
        call.respond(mapOf("variant" to (result?.variantKey ?: "disabled")))
    }
}
```

**Run:** `./gradlew :sample-ktor:runSample`

## Spring Boot

```java
// build.gradle.kts
implementation(project(":flagent-spring-boot-starter"))

// EvalController.java
@GetMapping("/eval")
public EvalResult eval(
        @RequestParam String flagKey,
        @RequestParam String entityId,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String tier) throws ApiException {
    Map<String, Object> ctx = new HashMap<>();
    if (country != null) ctx.put("country", country);
    if (tier != null) ctx.put("tier", tier);
    return flagentFacade.evaluate(
        new EvalContext().flagKey(flagKey).entityID(entityId).entityContext(ctx));
}
```

**Run:** `./gradlew :sample-spring-boot:bootRun`

## Kotlin (Standalone)

```kotlin
// Using Enhanced SDK with cache
val manager = FlagentManager(evaluationApi, FlagentConfig(enableCache = true))

val result = manager.evaluate(
    flagKey = "my_feature_flag",
    entityID = "user123",
    entityType = "user",
    entityContext = mapOf("region" to "US", "tier" to "premium")
)
```

**Run:** `./gradlew :sample-kotlin:runSample`

## JavaScript

```javascript
const response = await fetch('http://localhost:18000/api/v1/evaluation', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        flagKey: 'my_feature_flag',
        entityID: 'user123',
        entityContext: { region: 'US', tier: 'premium' }
    })
});
const result = await response.json();
```

## Swift

```swift
let evalContext = EvalContext(
    flagKey: "my_feature_flag",
    entityID: "user123",
    entityContext: ["region": "US", "tier": "premium"]
)
let result = try await evaluationAPI.postEvaluation(evalContext: evalContext)
```

## cURL (Direct API)

```bash
# Single evaluation
curl -X POST http://localhost:18000/api/v1/evaluation \
  -H "Content-Type: application/json" \
  -d '{"flagKey":"my_flag","entityID":"user123","entityContext":{"country":"US"}}'

# Batch evaluation
curl -X POST http://localhost:18000/api/v1/evaluation/batch \
  -H "Content-Type: application/json" \
  -d '{
    "entities":[{"entityID":"user1","entityContext":{"country":"US"}}],
    "flagKeys":["flag1","flag2"]
  }'
```

## EntityContext for Constraints

Pass `entityContext` when your flag has segment constraints (e.g., `country` EQ `US`):

```json
{
  "flagKey": "new_feature",
  "entityID": "user123",
  "entityContext": {
    "country": "US",
    "tier": "premium",
    "region": "eu-west"
  }
}
```

Supported constraint operators: `EQ`, `NEQ`, `IN`, `NOTIN`, `LT`, `LTE`, `GT`, `GTE`, `EREG`, `CONTAINS`. See [compatibility](../guides/compatibility.md).

## See Also

- [API Compatibility](../guides/compatibility.md)
- [Getting Started](../guides/getting-started.md)
- [API Endpoints](../api/endpoints.md)
