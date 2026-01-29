package flagent.application

import flagent.api.EnterpriseBackendContext
import flagent.api.EnterpriseConfigurator
import io.ktor.server.application.Application
import io.ktor.server.routing.Routing

/**
 * No-op implementation of EnterpriseConfigurator used when flagent-enterprise module is not present.
 */
class DefaultEnterpriseConfigurator : EnterpriseConfigurator {
    override fun configure(application: Application, context: EnterpriseBackendContext) {
        // No-op: enterprise module not loaded
    }

    override fun configureRoutes(routing: Routing, context: EnterpriseBackendContext) {
        // No-op: enterprise module not loaded
    }
}
