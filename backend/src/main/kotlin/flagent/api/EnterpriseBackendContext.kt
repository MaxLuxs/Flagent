package flagent.api

/**
 * Context passed from backend to enterprise module for configuration.
 * Allows enterprise to run migrations and repository queries without depending on backend.
 * Moved from shared to backend so shared jvmMain does not depend on Ktor.
 */
interface EnterpriseBackendContext {
    /**
     * Run a block inside a database transaction (used for enterprise table migrations).
     */
    fun runMigrations(block: () -> Unit)

    /**
     * Run a block inside a synchronous database transaction (used for enterprise repository operations).
     */
    fun <T> runTransaction(block: () -> T): T

    /**
     * Run a block inside a suspend database transaction (used for enterprise repository operations from coroutines).
     */
    suspend fun <T> runTransactionSuspend(block: suspend () -> T): T

    /**
     * Create a dedicated schema for a tenant (e.g. tenant_123). Used by TenantProvisioningService.
     */
    suspend fun createTenantSchema(schemaName: String)

    /**
     * Run core Flagent table migrations inside the given tenant schema.
     */
    suspend fun runTenantSchemaMigrations(schemaName: String)

    /**
     * Drop tenant schema (e.g. on tenant deletion).
     */
    suspend fun dropTenantSchema(schemaName: String)

    /**
     * Optional core dependencies (SegmentService, FlagRepository, EvalCache, etc.) for enterprise features
     * that need them (e.g. SmartRolloutService, AnomalyDetectionService). Null when not provided.
     */
    fun getCoreDependencies(): CoreDependencies?
}

/**
 * Provider for request-scoped environment ID (used for evaluation filtering).
 * Enterprise sets this to extract environmentId from TenantContext.
 * Accepts Any (ApplicationCall) to avoid Ktor dependency in api package.
 */
object EvalEnvironmentProvider {
    @Volatile
    var getEnvironmentId: (Any) -> Long? = { null }
}

/**
 * Core backend dependencies passed to enterprise for features that need them (smart rollout, anomaly detection).
 * CoreSegmentService and CoreFlagRepository are from shared (flagent.api).
 */
interface CoreDependencies {
    fun getSegmentService(): CoreSegmentService?
    fun getFlagRepository(): CoreFlagRepository?
    fun getEvalCache(): Any?
    fun getSlackNotificationService(): Any?
}
