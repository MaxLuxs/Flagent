# Tutorial: Gradual Rollout of New Payment on Ktor in 30 Minutes

This tutorial walks you through rolling out a new payment flow in a Ktor app using Flagent: start with 10% of users, increase to 50%, then 100%, with a kill switch if needed.

## Scenario

We're adding a new payment flow. We will:

1. Show the new flow to **10%** of users first.
2. Increase to **50%**, then **100%** when stable.
3. Use a **kill switch** (disable the flag) if something goes wrong so everyone gets the old flow immediately.

**Time:** about 30 minutes.

## Prerequisites

- **Flagent running.** Easiest: from repo root run `./scripts/run-golden-path.sh` (starts Flagent + seeds one flag + Ktor sample). Or start Flagent only: [Golden path](../guides/getting-started.md#golden-path-run-flagent--ktor-sample-in-one-go) or [Quick Start](../guides/getting-started.md#quick-start-5-minutes).
- **JDK 17+**, **Gradle** (wrapper in repo).
- **Repo clone** if you want to run the sample: `git clone https://github.com/MaxLuxs/Flagent.git && cd Flagent`.

## Step 1: Create the Flag in the UI

1. Open Flagent UI: [http://localhost:18000](http://localhost:18000). Log in (default: `admin@local` / `admin` if you used the golden path or standard Docker setup).
2. Click **Create Flag**.
3. Fill in:
   - **Key:** `new_payment_flow`
   - **Description:** `New payment experience with improved UX`
   - **Enabled:** checked
   - **Entity Type:** `user`
4. Click **Save**.

![Create Flag](../assets/screenshots/screenshot-create-flag.png)

**Optional (API):**

```bash
curl -X POST http://localhost:18000/api/v1/flags \
  -H "Content-Type: application/json" \
  -d '{"key":"new_payment_flow","description":"New payment experience","enabled":true,"entityType":"user"}'
```

Note the flag **ID** from the response (e.g. `1`). You will need it for segments and variants.

## Step 2: Add Variants

We need two variants: `control` (old flow) and `treatment` (new flow).

1. Open the flag **new_payment_flow** in the UI.
2. Go to the **Variants** tab.
3. Add:
   - Variant key: `control`
   - Variant key: `treatment`
4. Save.

**Optional (API):** Replace `{flagID}` with your flag ID (e.g. `1`).

```bash
curl -X POST http://localhost:18000/api/v1/flags/{flagID}/variants \
  -H "Content-Type: application/json" -d '{"key":"control"}'
curl -X POST http://localhost:18000/api/v1/flags/{flagID}/variants \
  -H "Content-Type: application/json" -d '{"key":"treatment"}'
```

## Step 3: Create a Segment with 10% Rollout

1. Go to the **Segments** tab for the flag.
2. Click **Add Segment**.
3. Set:
   - **Description:** `Gradual rollout 10%`
   - **Rollout:** `10` (percent)
   - **Rank:** `1`
4. In **Distributions**, assign **100%** to variant `treatment` for this segment. (Users who fall into this 10% segment all get `treatment`.)
5. Save.

Result: 10% of users (by deterministic bucketing) get `treatment`, 90% get the default (e.g. `control`). To change the rollout later, edit the segment and set Rollout to 50 or 100.

**Optional (API):** Create segment then set distributions. See [Gradual Rollout](gradual-rollout.md) for the exact payloads.

## Step 4: Add the Payment Route in Ktor

In your Ktor app, use the Flagent plugin to evaluate the flag and branch on the variant. Example route that switches between legacy and new payment flow:

```kotlin
import io.ktor.flagent.getFlagentClient
import io.ktor.http.HttpStatusCode
import flagent.api.model.EvaluationRequest

// In your routing block:
get("/pay") {
    val entityID = call.request.queryParameters["entityID"] ?: "guest-${java.util.UUID.randomUUID()}"
    val client = call.application.getFlagentClient()
    if (client == null) {
        call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "Flagent not available"))
        return@get
    }
    val request = EvaluationRequest(
        flagKey = "new_payment_flow",
        entityID = entityID,
        entityType = "user"
    )
    val result = client.evaluate(request)
    when (result.variantKey) {
        "treatment" -> {
            // New payment flow
            call.respond(mapOf(
                "flow" to "new",
                "variant" to "treatment",
                "message" to "Using new payment experience"
            ))
        }
        else -> {
            // Control or no match: legacy flow
            call.respond(mapOf(
                "flow" to "legacy",
                "variant" to (result.variantKey ?: "control"),
                "message" to "Using legacy payment"
            ))
        }
    }
}
```

If you use the [Ktor sample](https://github.com/MaxLuxs/Flagent/tree/main/samples/ktor), the existing `GET /feature/{flagKey}` already does the evaluation; you can call it for `new_payment_flow` or add a dedicated `/pay` route as above.

**Plugin setup** (if starting from scratch): In `Application.kt`, `installFlagent { flagentBaseUrl = "http://localhost:18000"; enableEvaluation = true; enableCache = true }`. See [samples/ktor](https://github.com/MaxLuxs/Flagent/tree/main/samples/ktor).

## Step 5: Verify 10% vs 90%

1. Start the Ktor sample (if not already running): from repo root, `./gradlew :sample-ktor:runSample`. Sample runs on port 8080.
2. Call the endpoint with different `entityID` values. About 10% should get `treatment`, 90% `control` (or legacy):

```bash
# Multiple users (deterministic: same entityID always gets same variant)
curl -s "http://localhost:8080/feature/new_payment_flow?entityID=user1"
curl -s "http://localhost:8080/feature/new_payment_flow?entityID=user2"
curl -s "http://localhost:8080/feature/new_payment_flow?entityID=user3"
```

3. In Flagent UI, open **Debug Console**. Evaluate `new_payment_flow` for a few entity IDs to see which variant they get.
4. When ready, **edit the segment** and set Rollout to **50%**, then **100%**. Repeat the curls; you should see more (then all) users getting `treatment`.

## Step 6: Kill Switch

If you need to revert immediately:

1. In Flagent UI, open the flag **new_payment_flow**.
2. **Disable** the flag (toggle off).
3. Save.

All users will now receive the default/control behaviour (your code’s `else` branch). No redeploy needed.

To roll out again, re-enable the flag and optionally reduce the segment rollout (e.g. back to 10%) and increase gradually.

## Next Steps

- [Gradual Rollout](gradual-rollout.md) — full API and UI steps for segments and distributions.
- [Golden path](../guides/getting-started.md#golden-path-run-flagent--ktor-sample-in-one-go) — one command to run Flagent + Ktor sample.
- [Ktor sample README](https://github.com/MaxLuxs/Flagent/tree/main/samples/ktor) — plugin configuration and endpoints.
