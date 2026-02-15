package flagent.sample

import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.entry.Flagent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun main() = runBlocking {
    val baseUrl = System.getenv("FLAGENT_BASE_URL") ?: "http://localhost:18000/api/v1"
    println("============================================================")
    println("Flagent Kotlin SDK Sample")
    println("============================================================")
    println("Base URL: $baseUrl")
    println("Tip: Start backend with ./gradlew :backend:runDev")
    println("      Seed demo flags: ./scripts/seed-demo-data.sh")
    println()

    val client = Flagent.builder()
        .baseUrl(baseUrl)
        .cache(true)
        .build()

    try {
        println("Example 1: Single flag evaluation")
        println("------------------------------------------------------------")
        val result = client.evaluate(
            flagKey = "my_feature_flag",
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("region" to "US", "tier" to "premium"),
            enableDebug = false
        )
        println("  flagKey=${result.flagKey}, variantKey=${result.variantKey}, flagID=${result.flagID}, variantID=${result.variantID}")
        println()

        println("Example 2: isEnabled")
        println("------------------------------------------------------------")
        val enabled = client.isEnabled("my_feature_flag", "user123")
        println("  isEnabled(my_feature_flag, user123) = $enabled")
        println()

        println("Example 3: Batch evaluation (multiple flags × entities)")
        println("------------------------------------------------------------")
        val batchResults = client.evaluateBatch(
            flagKeys = listOf("my_feature_flag", "sample_flag_2"),
            entities = listOf(
                EvaluationEntity(
                    entityID = "user1",
                    entityType = "user",
                    entityContext = buildJsonObject {
                        put("country", "US")
                        put("tier", "premium")
                    }
                ),
                EvaluationEntity(
                    entityID = "user2",
                    entityType = "user",
                    entityContext = buildJsonObject {
                        put("country", "EU")
                        put("tier", "basic")
                    }
                )
            ),
            enableDebug = false
        )
        println("  Total results: ${batchResults.size}")
        batchResults.forEachIndexed { i, r ->
            println("  [${i + 1}] flagKey=${r.flagKey}, variantKey=${r.variantKey}, entityID=${r.evalContext?.entityID}")
        }
        println()
        println("Done. The SDK supports caching and debug logs (enableDebug = true).")
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        System.err.println("Ensure Flagent backend is running at $baseUrl and flags exist (e.g. run scripts/seed-demo-data.sh).")
        kotlin.system.exitProcess(1)
    }
}
