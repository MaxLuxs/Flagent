package flagent.sample

import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.client.models.EvaluationEntity
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val baseUrl = System.getenv("FLAGENT_BASE_URL") ?: "http://localhost:18000/api/v1"
    println("============================================================")
    println("Flagent Kotlin SDK Sample")
    println("============================================================")
    println("Base URL: $baseUrl")
    println()

    val evaluationApi = EvaluationApi(baseUrl = baseUrl)
    val manager = FlagentManager(evaluationApi, FlagentConfig(enableCache = true))

    try {
        println("Example 1: Single Flag Evaluation")
        println("------------------------------------------------------------")
        val result = manager.evaluate(
            flagKey = "my_feature_flag",
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("region" to "US", "tier" to "premium"),
            enableDebug = false
        )
        println("Flag Key: ${result.flagKey}")
        println("Variant Key: ${result.variantKey}")
        println("Flag ID: ${result.flagID}")
        println("Variant ID: ${result.variantID}")
        println()

        println("Example 2: Batch Evaluation")
        println("------------------------------------------------------------")
        val batchResults = manager.evaluateBatch(
            flagKeys = listOf("my_feature_flag", "sample_flag_2"),
            entities = listOf(
                EvaluationEntity(entityID = "user1", entityType = "user"),
                EvaluationEntity(entityID = "user2", entityType = "user")
            ),
            enableDebug = false
        )
        println("Total Results: ${batchResults.size}")
        batchResults.forEachIndexed { i, r ->
            println("Result ${i + 1}: flagKey=${r.flagKey}, variantKey=${r.variantKey}, entityID=${r.evalContext?.entityID}")
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        System.err.println("Ensure Flagent backend is running at $baseUrl")
        kotlin.system.exitProcess(1)
    }
}
