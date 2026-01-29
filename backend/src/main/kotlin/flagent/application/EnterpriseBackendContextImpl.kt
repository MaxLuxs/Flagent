package flagent.application

import flagent.api.CoreDependencies
import flagent.api.EnterpriseBackendContext
import flagent.repository.Database

/**
 * Backend implementation of EnterpriseBackendContext for enterprise module migrations and repos.
 */
class EnterpriseBackendContextImpl(
    private val coreDependencies: CoreDependencies? = null
) : EnterpriseBackendContext {
    override fun runMigrations(block: () -> Unit) {
        Database.runBlockingMigrations(block)
    }

    override fun <T> runTransaction(block: () -> T): T {
        return Database.runBlockingTransaction(block)
    }

    override suspend fun <T> runTransactionSuspend(block: suspend () -> T): T {
        return Database.transaction(block)
    }

    override suspend fun createTenantSchema(schemaName: String) {
        Database.createTenantSchema(schemaName)
    }

    override suspend fun runTenantSchemaMigrations(schemaName: String) {
        Database.runTenantSchemaMigrations(schemaName)
    }

    override suspend fun dropTenantSchema(schemaName: String) {
        Database.dropTenantSchema(schemaName)
    }

    override fun getCoreDependencies(): CoreDependencies? = coreDependencies
}
