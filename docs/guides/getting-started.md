# Getting Started with Flagent

> [English](getting-started.md) | [Русский](getting-started.ru.md)

Welcome to Flagent! This guide will help you get started with feature flags and experimentation in less than 10 minutes.

## What is Flagent?

Flagent is an open-source feature flag and experimentation platform that helps you:

- **Deploy Safely** - Roll out features gradually with kill switches
- **Run Experiments** - A/B test new features with statistical rigor
- **Target Users** - Show features to specific user segments
- **Move Fast** - Deploy code to production without risk

## Quick Start (5 minutes)

### 1. Start Flagent Server

The backend requires a **database** and **admin credentials** (and JWT secret for login). Without them you get "Admin credentials not configured" and cannot log in to the UI.

#### Using Docker (Recommended)

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 \
  -e FLAGENT_ADMIN_EMAIL=admin@local \
  -e FLAGENT_ADMIN_PASSWORD=admin \
  -e FLAGENT_JWT_AUTH_SECRET=change-me-min-32-chars-for-dev-only \
  -v flagent-data:/data \
  ghcr.io/maxluxs/flagent
```

- Default DB in the image: SQLite at `/data/flagent.sqlite`. Use `-v flagent-data:/data` so data persists.
- For production use Docker Compose with PostgreSQL (see below).

#### Using Docker Compose (PostgreSQL, production-ready)

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
docker compose up -d
```

Uses PostgreSQL and preconfigured admin/JWT from `docker-compose.yml`.

#### Build from Source

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
./gradlew build
```

Set **required** environment variables before running (otherwise login fails):

```bash
# Minimal for local run (SQLite + admin login)
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
export FLAGENT_ADMIN_EMAIL=admin@local
export FLAGENT_ADMIN_PASSWORD=admin
export FLAGENT_JWT_AUTH_SECRET=dev-secret-at-least-32-characters-long
export PORT=18000
./gradlew :backend:run
```

For PostgreSQL use `FLAGENT_DB_DBDRIVER=postgres` and `FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent?user=postgres&password=postgres`. See [Configuration](configuration.md).

### 2. Access Flagent UI

Open your browser and navigate to:

```
http://localhost:18000
```

**Default credentials:**
- Email: `admin@local`
- Password: `admin`

### 3. Create Your First Flag

1. Click **"Create Flag"** button
2. Enter flag details:
   - **Key**: `my_first_flag`
   - **Description**: `My first feature flag`
   - **Enabled**: ✓ Check
3. Add a **Segment**:
   - **Rollout**: 100%
4. Add a **Variant**:
   - **Key**: `enabled`
5. Add a **Distribution**:
   - **Variant**: `enabled`
   - **Percent**: 100%
6. Click **"Save"**

### 4. Evaluate from Your App

Choose your language:

**Kotlin:**
```kotlin
val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")

val result = client.evaluate(
    flagKey = "my_first_flag",
    entityID = "user123"
)

if (result.variantKey == "enabled") {
    // Feature is enabled
}
```

**Python:**
```python
from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")
result = await client.evaluate(flag_key="my_first_flag", entity_id="user123")

if result.is_enabled():
    # Feature is enabled
```

**JavaScript:**
```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1'
});

const result = await client.evaluate({
  flagKey: 'my_first_flag',
  entityID: 'user123'
});

if (result.variantKey === 'enabled') {
  // Feature is enabled
}
```

**Swift:**
```swift
let client = FlagentClient(baseURL: "http://localhost:18000/api/v1")

let result = try await client.evaluate(
    flagKey: "my_first_flag",
    entityID: "user123"
)

if result.variantKey == "enabled" {
    // Feature is enabled
}
```

### 5. Test Your Flag

Run your app and verify that the flag evaluation works!

## Core Concepts

### Flag

A **flag** is a feature toggle that controls whether a feature is enabled or disabled.

```kotlin
val result = client.evaluate(flagKey = "new_payment_flow", entityID = "user123")
```

### Variant

A **variant** represents different versions of a feature (e.g., control, treatment_a, treatment_b).

```kotlin
when (result.variantKey) {
    "control" -> showOldUI()
    "treatment_a" -> showNewUIv1()
    "treatment_b" -> showNewUIv2()
}
```

### Segment

A **segment** defines rules for targeting specific users.

**Example**: Show feature to premium users in the US
- Constraint 1: `tier == "premium"`
- Constraint 2: `region == "US"`
- Rollout: 50%

### Constraint

A **constraint** is a rule that matches user context.

**Operators:**
- `EQ` (equals)
- `NEQ` (not equals)
- `IN` (in list)
- `GT/LT` (greater/less than)
- `CONTAINS` (string contains)
- `EREG` (regex match)

**Example:**
```json
{
  "property": "region",
  "operator": "IN",
  "value": "US,CA,UK"
}
```

### Distribution

A **distribution** controls the percentage split between variants.

**Example**: 50/50 A/B test
- Variant A: 50%
- Variant B: 50%

### Rollout

A **rollout** controls what percentage of matched users see the feature.

**Example**: Gradual rollout
1. Start: 1% rollout
2. After 1 day: 10% rollout
3. After 1 week: 50% rollout
4. After 2 weeks: 100% rollout

## Common Use Cases

### 1. Kill Switch

**Scenario**: Instantly disable a problematic feature

```kotlin
// Setup: Create flag with 100% enabled
val flag = createFlag(key = "new_feature", enabled = true)

// In production: Toggle flag in UI if issues occur
if (client.evaluate("new_feature", entityID).isEnabled()) {
    showNewFeature()
} else {
    showOldFeature()
}
```

### 2. Gradual Rollout

**Scenario**: Roll out feature to 1%, 10%, 50%, 100%

```kotlin
// Setup: Create flag with rollout=1%
val segment = Segment(rolloutPercent = 1)

// Monitor metrics, increase rollout gradually
// Day 1: 1% -> Day 2: 10% -> Week 1: 50% -> Week 2: 100%
```

### 3. A/B Test

**Scenario**: Test checkout flow variant A vs B

```kotlin
val result = client.evaluate("checkout_experiment", entityID = user.id)

when (result.variantKey) {
    "control" -> showOldCheckout()
    "variant_a" -> showNewCheckoutA()
    "variant_b" -> showNewCheckoutB()
}

// Track conversion
analytics.track("checkout_completed", mapOf("variant" to result.variantKey))
```

### 4. User Segmentation

**Scenario**: Show premium feature only to premium users

```kotlin
// Setup: Add constraint tier=="premium"
val result = client.evaluate(
    flagKey = "premium_feature",
    entityID = user.id,
    context = mapOf("tier" to user.tier)
)

if (result.isEnabled()) {
    showPremiumFeature()
}
```

### 5. Regional Features

**Scenario**: Show feature only in specific regions

```kotlin
// Setup: Add constraint region IN ["US","CA","UK"]
val result = client.evaluate(
    flagKey = "regional_feature",
    entityID = user.id,
    context = mapOf("region" to user.region)
)
```

## Best Practices

### 1. Name Flags Clearly

**Good:**
- `new_payment_flow`
- `checkout_experiment_v2`
- `dark_mode_enabled`

**Bad:**
- `test_flag`
- `flag_123`
- `new_thing`

### 2. Start with Small Rollouts

Always start with 1-5% rollout and gradually increase after monitoring metrics.

### 3. Use Constraints for Targeting

Instead of hardcoding user IDs, use constraints:

```kotlin
// Bad: Hardcode user IDs in code
if (userId in listOf("user1", "user2")) {
    showNewFeature()
}

// Good: Use flag with constraint
val result = client.evaluate(
    flagKey = "new_feature",
    entityID = userId,
    context = mapOf("tier" to "premium")
)
```

### 4. Clean Up Old Flags

Remove flags after feature is fully rolled out or experiment is completed.

### 5. Monitor Metrics

Always monitor key metrics when rolling out new features:
- Error rates
- Performance metrics
- User engagement
- Conversion rates

### 6. Use Debug Mode

Enable debug mode during development to see evaluation logs:

```kotlin
val result = client.evaluate(
    flagKey = "test_flag",
    entityID = "test_user",
    enableDebug = true
)

println(result.debugLogs) // See why flag matched or didn't match
```

## SDK Setup

### Kotlin/Android

```kotlin
dependencies {
    implementation("com.flagent:kotlin-client:0.1.6")
}
```

```kotlin
val client = FlagentClient.create(
    baseUrl = "http://localhost:18000/api/v1"
)
```

### Python

```bash
pip install flagent-python-client
```

```python
from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")
```

### JavaScript/Node.js

```bash
npm install @flagent/client
```

```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1'
});
```

### Swift/iOS

```swift
// Package.swift
.package(url: "https://github.com/MaxLuxs/Flagent.git", from: "0.1.6")
```

```swift
let client = FlagentClient(baseURL: "http://localhost:18000/api/v1")
```

## Advanced Features

### Client-Side Evaluation (Offline-First)

Evaluate flags locally without API calls:

```kotlin
val manager = OfflineFlagentManager(exportApi, flagApi)
manager.bootstrap() // Load snapshot once

// Fast, local evaluation (< 1ms, no API call)
val result = manager.evaluate(flagKey = "feature", entityID = "user123")
```

**Benefits:**
- Much faster than server round-trips (works offline)
- Significantly reduces server load

See [Client-Side Evaluation Guide](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/CLIENT_SIDE_EVALUATION.md)

### Real-Time Updates

Get instant flag updates via Server-Sent Events:

```kotlin
manager.enableRealtimeUpdates(baseUrl = "http://localhost:18000")

// Flags auto-refresh when updated (< 1s latency)
```

See [Real-Time Updates Guide](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/REALTIME_UPDATES.md)

## Troubleshooting: First Run

### Cannot log in (500 or "Admin credentials not configured")

The server needs admin credentials and a JWT secret to issue login tokens. Set these **before** first run:

1. **Required env vars:** `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD`, `FLAGENT_JWT_AUTH_SECRET` (min 32 chars). See [Configuration](configuration.md#admin-auth-enterprise).
2. **Docker:** Pass them with `-e` (see Quick Start above). Without them the image’s built-in defaults may not be enough depending on build.
3. **From source:** Always export the three variables; otherwise login will fail.
4. **Login uses email** — use the value of `FLAGENT_ADMIN_EMAIL` (e.g. `admin@local`), not a username.

### UnsupportedClassVersionError

Java 21 is required. Set `JAVA_HOME` to JDK 21 or use Gradle's toolchain.

### No UI at localhost:18000

Docker image includes UI. If you see only Swagger at /docs, ensure you use the latest image. For self-built backend, run `./gradlew :frontend:jsBrowserDevelopmentWebpack` before `./gradlew :backend:run`.

## Next Steps

- **[API Documentation](../api/endpoints.md)** - Explore all API endpoints
- **[Architecture](../architecture/backend.md)** - Understand how Flagent works
- **[Use Cases](use-cases.md)** - See real-world examples
- **[Examples](../examples/README.md)** - Code examples for common scenarios

## Troubleshooting

### Flag not found

**Problem**: Evaluation returns "flag not found"

**Solutions**:
1. Verify flag exists in UI
2. Check flag key spelling
3. Ensure flag is enabled

### Evaluation always returns null

**Problem**: Flag enabled but evaluation returns null variant

**Reasons**:
1. No segments configured
2. Segment constraints don't match
3. Rollout percentage is 0%
4. No distributions configured

**Solutions**:
1. Add at least one segment
2. Check constraint matching with debug mode
3. Increase rollout percentage
4. Add distributions to segment

### Slow evaluation

**Problem**: Flag evaluation takes > 100ms

**Solutions**:
1. Use client-side evaluation for < 1ms latency
2. Enable caching in SDK
3. Use batch evaluation for multiple flags
4. Check network latency to server

### Docker container won't start

**Problem**: Container exits immediately

**Solutions**:
1. Check Docker logs: `docker logs <container_id>`
2. Ensure port 18000 is not in use
3. Check database connection if using external DB

## See Also

- [API Compatibility](compatibility.md) - Evaluation API format and migration guide
- [Examples](../examples/README.md) - Code snippets for API, Ktor, and [SDK integration](../examples/sdk-integration.md)

## Support

- **Documentation**: https://maxluxs.github.io/Flagent/guides/getting-started.md
- **GitHub Issues**: https://github.com/MaxLuxs/Flagent/issues
- **Contact**: max.developer.luxs@gmail.com

## License

Flagent is open-source under Apache 2.0 license.
