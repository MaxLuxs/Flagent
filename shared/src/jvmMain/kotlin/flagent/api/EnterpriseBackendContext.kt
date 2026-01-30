package flagent.api

/**
 * Context passed from backend to enterprise module for configuration.
 * Allows enterprise to run migrations and repository queries without depending on backend.
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
 * Core backend dependencies passed to enterprise for features that need them (smart rollout, anomaly detection).
 */
interface CoreDependencies {
    fun getSegmentService(): CoreSegmentService?
    fun getFlagRepository(): CoreFlagRepository?
    fun getEvalCache(): Any?
    fun getSlackNotificationService(): Any?
}
