package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationEntity
import kotlinx.coroutines.runBlocking

/**
 * Blocking wrapper for [FlagentClient] for use from Java or other non-coroutine callers.
 * Each method blocks the current thread until the result is available.
 *
 * Create via [Flagent.builder].buildBlocking() on JVM.
 *
 * @see Flagent.Builder.buildBlocking
 */
class FlagentClientBlocking(
    private val client: FlagentClient
) {
    /**
     * For offline mode: loads snapshot (bootstrap). No-op for server mode.
     */
    fun initialize(forceRefresh: Boolean = false) {
        runBlocking { client.initialize(forceRefresh) }
    }

    /**
     * Evaluates a single flag for the given entity (blocking).
     */
    fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ): EvalResult = runBlocking {
        client.evaluate(
            flagKey = flagKey,
            flagID = flagID,
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            enableDebug = enableDebug
        )
    }

    /**
     * Returns true if the flag evaluates to an enabled variant (blocking).
     */
    fun isEnabled(
        flagKey: String,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null
    ): Boolean = runBlocking {
        client.isEnabled(
            flagKey = flagKey,
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext
        )
    }

    /**
     * Batch evaluation: multiple flags × entities (blocking).
     */
    fun evaluateBatch(
        flagKeys: List<String>? = null,
        flagIDs: List<Int>? = null,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean = false
    ): List<EvalResult> = runBlocking {
        client.evaluateBatch(
            flagKeys = flagKeys,
            flagIDs = flagIDs,
            entities = entities,
            enableDebug = enableDebug
        )
    }
}

/**
 * Builds a [FlagentClientBlocking] for use from Java or other non-coroutine callers.
 * Use this when you cannot use Kotlin coroutines (e.g. plain Java code).
 *
 * Example from Java:
 * ```java
 * FlagentClientBlocking client = Flagent.INSTANCE.builder()
 *     .baseUrl("https://api.example.com/api/v1")
 *     .cache(true, 300_000L)
 *     .buildBlocking();
 * EvalResult result = client.evaluate("new_feature", null, "user123", null, null, false);
 * boolean on = client.isEnabled("new_feature", "user123", null, null);
 * ```
 */
fun Flagent.Builder.buildBlocking(): FlagentClientBlocking =
    FlagentClientBlocking(build())
