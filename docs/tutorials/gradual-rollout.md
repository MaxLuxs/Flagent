# Tutorial: Gradual Rollout

Learn how to safely roll out a new feature using Flagent's gradual rollout capabilities.

## Scenario

You're launching a new payment flow and want to:
1. Test with 1% of users first
2. Gradually increase to 10%, 50%, 100%
3. Monitor metrics at each stage
4. Have a kill switch ready if issues occur

## Step 1: Create Flag

### Via UI

1. Navigate to Flagent UI: `http://localhost:18000`
2. Click **"Create Flag"**
3. Fill in details:
   - **Key**: `new_payment_flow`
   - **Description**: `New payment experience with improved UX`
   - **Enabled**: Check
   - **Entity Type**: `user`
4. Click **"Save"**

### Via API

```bash
curl -X POST http://localhost:18000/api/v1/flags \
  -H "Content-Type: application/json" \
  -d '{
    "key": "new_payment_flow",
    "description": "New payment experience",
    "enabled": true,
    "entityType": "user"
  }'
```

## Step 2: Add Variants

Create two variants: `control` (old flow) and `treatment` (new flow)

### Via UI

1. Open your flag
2. Go to **Variants** tab
3. Add two variants:
   - Variant 1: Key = `control`
   - Variant 2: Key = `treatment`
4. Save

### Via API

```bash
# Add control variant
curl -X POST http://localhost:18000/api/v1/flags/1/variants \
  -H "Content-Type: application/json" \
  -d '{"key": "control"}'

# Add treatment variant
curl -X POST http://localhost:18000/api/v1/flags/1/variants \
  -H "Content-Type: application/json" \
  -d '{"key": "treatment"}'
```

## Step 3: Create Segment with 1% Rollout

### Via UI

1. Go to **Segments** tab
2. Click **"Add Segment"**
3. Configure:
   - **Description**: `Gradual rollout segment`
   - **Rollout**: `1%`
   - **Rank**: `1`
4. Add distribution:
   - **Variant**: `treatment`
   - **Percent**: `100%`
5. Save

### Via API

```bash
# Create segment with 1% rollout
curl -X POST http://localhost:18000/api/v1/flags/1/segments \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Gradual rollout segment",
    "rolloutPercent": 1,
    "rank": 1
  }'

# Add distribution (100% treatment)
curl -X POST http://localhost:18000/api/v1/flags/1/segments/1/distributions \
  -H "Content-Type: application/json" \
  -d '{
    "variantKey": "treatment",
    "percent": 100
  }'
```

## Step 4: Implement in Code

### Kotlin

```kotlin
val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")

fun processPayment(user: User, amount: Double) {
    val result = client.evaluate(
        flagKey = "new_payment_flow",
        entityID = user.id
    )
    
    when (result.variantKey) {
        "treatment" -> {
            // Use new payment flow
            newPaymentService.process(user, amount)
            
            // Track that user saw treatment
            analytics.track("payment_flow_variant", mapOf(
                "variant" to "treatment",
                "user_id" to user.id
            ))
        }
        else -> {
            // Use old payment flow (control or no match)
            legacyPaymentService.process(user, amount)
            
            analytics.track("payment_flow_variant", mapOf(
                "variant" to "control",
                "user_id" to user.id
            ))
        }
    }
}
```

### Python

```python
from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")

async def process_payment(user, amount):
    result = await client.evaluate(
        flag_key="new_payment_flow",
        entity_id=user.id
    )
    
    if result.variant_key == "treatment":
        # Use new payment flow
        await new_payment_service.process(user, amount)
        
        # Track variant
        await analytics.track("payment_flow_variant", {
            "variant": "treatment",
            "user_id": user.id
        })
    else:
        # Use old payment flow
        await legacy_payment_service.process(user, amount)
        
        await analytics.track("payment_flow_variant", {
            "variant": "control",
            "user_id": user.id
        })
```

### JavaScript

```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1'
});

async function processPayment(user, amount) {
  const result = await client.evaluate({
    flagKey: 'new_payment_flow',
    entityID: user.id
  });
  
  if (result.variantKey === 'treatment') {
    // Use new payment flow
    await newPaymentService.process(user, amount);
    
    analytics.track('payment_flow_variant', {
      variant: 'treatment',
      userId: user.id
    });
  } else {
    // Use old payment flow
    await legacyPaymentService.process(user, amount);
    
    analytics.track('payment_flow_variant', {
      variant: 'control',
      userId: user.id
    });
  }
}
```

## Step 5: Deploy and Monitor (1% Rollout)

1. Deploy your code to production
2. Monitor key metrics for 24-48 hours:
   - **Error rates** - Should not increase
   - **Payment success rate** - Should be stable or improve
   - **Latency** - p50, p95, p99 should be acceptable
   - **User feedback** - Check support tickets

### Example Monitoring Dashboard

```
Metric                | Control | Treatment | Change
---------------------|---------|-----------|--------
Error Rate           | 0.5%    | 0.6%      | +0.1% OK
Payment Success      | 98.5%   | 98.7%     | +0.2% OK
p95 Latency          | 250ms   | 240ms     | -10ms OK
Support Tickets      | 5       | 4         | -1 OK
```

## Step 6: Increase to 10% Rollout

If metrics look good after 24-48 hours, increase rollout:

### Via UI

1. Open flag > Segments tab
2. Edit segment
3. Change **Rollout** from `1%` to `10%`
4. Save

### Via API

```bash
curl -X PUT http://localhost:18000/api/v1/flags/1/segments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "rolloutPercent": 10
  }'
```

### Monitor for 2-3 Days

Continue monitoring metrics with larger sample size.

## Step 7: Increase to 50% Rollout

After 2-3 days at 10% with stable metrics:

```bash
curl -X PUT http://localhost:18000/api/v1/flags/1/segments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "rolloutPercent": 50
  }'
```

Monitor for 3-5 days.

## Step 8: Full Rollout (100%)

After 3-5 days at 50% with positive results:

```bash
curl -X PUT http://localhost:18000/api/v1/flags/1/segments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "rolloutPercent": 100
  }'
```

## Step 9: Clean Up (Remove Flag)

After 1-2 weeks at 100% with no issues:

1. Remove old code path:

```kotlin
fun processPayment(user: User, amount: Double) {
    // Remove flag check, use new flow directly
    newPaymentService.process(user, amount)
}
```

2. Delete flag from Flagent UI

3. Remove flag evaluation code

## Kill Switch (Emergency Rollback)

If you detect issues at any stage, instantly disable the flag:

### Via UI

1. Open flag
2. Uncheck **"Enabled"**
3. Save

All users immediately revert to control (old flow).

### Via API

```bash
curl -X PUT http://localhost:18000/api/v1/flags/1/enabled \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'
```

## Advanced: Rollout with Constraints

Roll out only to specific segments first (e.g., premium users):

### Add Constraint

```bash
curl -X POST http://localhost:18000/api/v1/flags/1/segments/1/constraints \
  -H "Content-Type: application/json" \
  -d '{
    "property": "tier",
    "operator": "EQ",
    "value": "premium"
  }'
```

### Update Evaluation

```kotlin
val result = client.evaluate(
    flagKey = "new_payment_flow",
    entityID = user.id,
    context = mapOf("tier" to user.tier) // Add context
)
```

Now rollout only affects premium users.

## Rollout Timeline Example

| Stage | Rollout % | Duration | Action |
|-------|-----------|----------|--------|
| 1 | 1% | 1-2 days | Initial test |
| 2 | 10% | 2-3 days | Validate at scale |
| 3 | 50% | 3-5 days | Larger validation |
| 4 | 100% | 1-2 weeks | Full rollout |
| 5 | - | - | Remove flag and old code |

**Total Time**: 2-3 weeks for safe rollout

## Best Practices

### 1. Start Small

Always start with 1-5% rollout to catch issues early.

### 2. Monitor Key Metrics

Focus on:
- Error rates
- Performance (latency)
- Business metrics (conversion, revenue)
- User feedback

### 3. Wait Between Stages

Give each stage time to accumulate data before proceeding.

### 4. Have a Rollback Plan

Always keep the kill switch ready.

### 5. Document Decisions

Keep notes on why you increased/decreased rollout.

### 6. Communicate with Team

Keep stakeholders informed of rollout progress.

## Troubleshooting

### Issue: No users seeing treatment

**Problem**: Flag enabled but everyone gets control

**Check**:
1. Rollout percentage > 0%
2. Distribution configured
3. Constraints match users

### Issue: Too many users seeing treatment

**Problem**: Expected 10% but getting 15%

**Cause**: Consistent hashing distributes based on entity ID

**Solution**: This is normal variance. Over time it converges to exact percentage.

### Issue: Metrics look bad

**Problem**: Error rates increased at 10% rollout

**Action**:
1. Disable flag immediately (kill switch)
2. Investigate errors
3. Fix issues
4. Re-enable at 1% after fix

## Next Steps

- [Client-Side Evaluation](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/CLIENT_SIDE_EVALUATION.md) - Faster, offline evaluation
- [Use Cases](../guides/use-cases.md) - More real-world examples
- [API Documentation](../api/endpoints.md) - Full API reference

## Summary

Gradual rollout with Flagent:
1. Create flag with variants
2. Start at 1% rollout
3. Monitor metrics
4. Gradually increase: 10% to 50% to 100%
5. Keep kill switch ready
6. Clean up after full rollout

This approach minimizes risk while ensuring quality.
