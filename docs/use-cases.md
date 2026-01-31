# Use Cases

> [English](use-cases.md) | [Русский](use-cases.ru.md)

Flagent can be used in various scenarios to improve software delivery, experimentation, and user experience. Here are some common use cases and real-world examples.

## Feature Flags for Safe Deployments

Deploy code to production behind feature flags, enabling instant rollbacks without code changes.

### Example: Gradual Rollout of New Payment System

```kotlin
val client = FlagentClient.create(
    baseUrl = "http://localhost:18000/api/v1",
    apiKey = "your-api-key"
)

// Check if new payment system is enabled
if (client.isEnabled("new_payment_system", entityContext = userContext)) {
    // Use new payment implementation
    newPaymentService.processPayment(amount, currency)
} else {
    // Fallback to legacy payment system
    legacyPaymentService.processPayment(amount, currency)
}
```

**Benefits:**
- Deploy code to production safely
- Instant rollback if issues are detected
- Gradual rollout (1%, 10%, 50%, 100%)
- A/B test new implementation

## A/B Testing for Product Optimization

Run experiments to test new features and measure their impact on key metrics.

### Example: Checkout Flow A/B Test

```kotlin
val assignment = client.evaluate(
    flagKey = "checkout_experiment",
    entityContext = mapOf(
        "user_id" to userId,
        "country" to "US",
        "tier" to userTier
    )
)

when (assignment?.variant) {
    "control" -> showOldCheckoutFlow()
    "variant_a" -> showNewCheckoutFlowA()
    "variant_b" -> showNewCheckoutFlowB()
}

// Track conversion
analytics.track("checkout_completed", mapOf(
    "variant" to assignment?.variant,
    "amount" to orderAmount
))
```

**Benefits:**
- Test multiple variants simultaneously
- Statistical significance calculation
- Deterministic bucketing (consistent results)
- Real-time analytics

## Gradual Rollouts with Kill Switches

Roll out features gradually and instantly disable if issues are detected.

### Example: New Feature Rollout

```kotlin
// Start with 1% rollout
val enabled = client.isEnabled(
    "new_feature",
    entityContext = mapOf(
        "region" to "US",
        "tier" to "premium"
    )
)

if (enabled) {
    try {
        // New feature implementation
        newFeature.execute()
    } catch (e: Exception) {
        // Log error and disable feature via Flagent UI
        logger.error("New feature failed", e)
        // Feature will be automatically disabled in Flagent
    }
} else {
    // Fallback to old implementation
    oldFeature.execute()
}
```

**Benefits:**
- Start with small percentage (1%)
- Monitor metrics and errors
- Increase gradually (10%, 50%, 100%)
- Instant kill switch if issues occur

## Dynamic Configuration

Update application settings without redeployment.

### Example: Dynamic Rate Limits

```kotlin
val config = client.getVariantAttachment("app_config")
val maxRetries = config?.get("max_retries")?.toIntOrNull() ?: 3
val timeout = config?.get("timeout_ms")?.toLongOrNull() ?: 5000
val rateLimit = config?.get("rate_limit_per_minute")?.toIntOrNull() ?: 100

// Use dynamic configuration
retryService.maxRetries = maxRetries
httpClient.timeout = Duration.ofMillis(timeout)
rateLimiter.limitPerMinute = rateLimit
```

**Benefits:**
- Change settings without code deployment
- Environment-specific configuration
- Instant updates across all instances
- Rollback configuration changes

## User Segmentation and Targeting

Target specific user segments with complex rules.

### Example: Premium Features for Specific Regions

```kotlin
val enabled = client.isEnabled(
    "premium_feature",
    entityContext = mapOf(
        "tier" to "premium",
        "region" to "US",
        "subscription_age_days" to subscriptionAge.toString(),
        "account_value" to accountValue.toString()
    )
)

if (enabled) {
    // Show premium feature
    showPremiumFeature()
} else {
    // Show standard feature
    showStandardFeature()
}
```

**Benefits:**
- Target by multiple attributes
- Complex targeting rules (AND/OR logic)
- Percentage-based targeting
- Region-specific features

## Canary Deployments

Gradually roll out new versions to a subset of users.

### Example: Canary Deployment of New Version

```kotlin
val variant = client.evaluate(
    "api_version",
    entityContext = mapOf(
        "user_id" to userId,
        "api_key" to apiKey
    )
)?.variant ?: "v1"

when (variant) {
    "v1" -> useAPIv1()
    "v2" -> useAPIv2() // Canary version
    "v3" -> useAPIv3() // Future version
}
```

**Benefits:**
- Test new versions with real users
- Monitor performance and errors
- Instant rollback if issues occur
- Gradual migration strategy

## Dark Mode / Beta Features

Enable features for specific user groups (internal testing, beta users).

### Example: Beta Feature Access

```kotlin
val isBetaUser = client.isEnabled(
    "beta_features",
    entityContext = mapOf(
        "user_id" to userId,
        "user_role" to userRole,
        "beta_program" to "enabled"
    )
)

if (isBetaUser) {
    // Show beta features
    showBetaFeatures()
}
```

**Benefits:**
- Internal testing without affecting production
- Beta user program management
- Feature gating for premium users
- Gradual feature release

## Performance Optimization

Test performance optimizations with A/B testing.

### Example: Database Query Optimization

```kotlin
val variant = client.evaluate(
    "db_query_optimization",
    entityContext = mapOf(
        "user_id" to userId
    )
)?.variant ?: "old"

when (variant) {
    "old" -> {
        // Use old query
        val result = database.oldQuery()
        // Track performance
        metrics.record("query_time", oldQueryTime, mapOf("variant" to "old"))
    }
    "optimized" -> {
        // Use optimized query
        val result = database.optimizedQuery()
        // Track performance
        metrics.record("query_time", optimizedQueryTime, mapOf("variant" to "optimized"))
    }
}
```

**Benefits:**
- Test performance improvements safely
- Compare old vs optimized implementations
- Measure real-world performance impact
- Rollout optimized version gradually

## Emergency Kill Switches

Instantly disable features in production if critical issues are detected.

### Example: Payment System Kill Switch

```kotlin
val paymentEnabled = client.isEnabled(
    "payment_system",
    entityContext = emptyMap() // Global flag
)

if (!paymentEnabled) {
    // Payment system disabled globally
    return ErrorResponse("Payment system temporarily unavailable")
}

// Normal payment processing
processPayment()
```

**Benefits:**
- Instant global feature disable
- Emergency response to critical issues
- No code deployment required
- User-friendly error messages

## Multi-Tenant Configuration

Different configuration for different tenants or customers.

### Example: Tenant-Specific Features

```kotlin
val tenantFeatures = client.evaluate(
    "tenant_features",
    entityContext = mapOf(
        "tenant_id" to tenantId,
        "plan" to tenantPlan
    )
)?.variantAttachment

val maxUsers = tenantFeatures?.get("max_users")?.toIntOrNull() ?: 10
val storageGB = tenantFeatures?.get("storage_gb")?.toIntOrNull() ?: 100
val enabledFeatures = tenantFeatures?.get("features")?.split(",") ?: emptyList()
```

**Benefits:**
- Customize features per tenant
- Different plans and configurations
- Easy plan upgrades/downgrades
- Tenant isolation

## Real-World Examples

### E-commerce Platform
- **Use Case**: A/B test checkout flow variants
- **Benefits**: Increased conversion rate by 15% after identifying best variant
- **Implementation**: 50/50 split between old and new checkout, measured conversion rates

### SaaS Application
- **Use Case**: Gradual rollout of new UI design
- **Benefits**: Reduced user complaints by rolling out gradually (1% → 10% → 50% → 100%)
- **Implementation**: Region-based rollout, monitored user feedback

### Mobile App
- **Use Case**: Feature flags for new app features
- **Benefits**: Enable features for beta users first, then gradually expand
- **Implementation**: User tier-based targeting (beta → premium → all users)

## Best Practices

1. **Start Small**: Begin with 1% rollout and gradually increase
2. **Monitor Metrics**: Track key metrics (conversion, errors, performance)
3. **Set Up Alerts**: Configure alerts for error rates and performance degradation
4. **Document Flags**: Document what each flag does and its expected impact
5. **Clean Up**: Remove unused flags to reduce complexity
6. **Test Locally**: Use local overrides for testing before production
7. **Version Control**: Track flag changes in version control (when using flags as code)

## Next Steps

- [Get Started](getting-started.md) - Set up Flagent
- [Architecture](architecture/backend.md) - Understand how Flagent works
- [API Documentation](api/endpoints.md) - Explore API endpoints
- [Code Examples](examples/README.md) - See more examples
