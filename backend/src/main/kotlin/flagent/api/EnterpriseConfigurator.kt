package flagent.api

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing

/**
 * Contract for enterprise module configuration.
 * Implemented by flagent-enterprise when present; otherwise backend uses a no-op implementation.
 * Moved from shared to backend so shared jvmMain does not depend on Ktor.
 */
interface EnterpriseConfigurator {
    /**
     * Configure enterprise middleware and migrations (tables, tenant schema helpers).
     * @param application Ktor application
     * @param context Backend context (database migrations, etc.)
     */
    fun configure(application: Application, context: EnterpriseBackendContext)

    /**
     * Register enterprise routes (tenant, billing, SSO). Called from backend routing block.
     * @param routing Current routing instance (same as backend's route block)
     * @param context Backend context for DB and core dependencies
     */
    fun configureRoutes(routing: Routing, context: EnterpriseBackendContext)
}
