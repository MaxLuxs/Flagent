package flagent.api

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for EnterpriseConfigurator API contract.
 * Verifies that configure and configureRoutes are called with Application and context.
 */
class EnterpriseConfiguratorTest {

    @Test
    fun `configure is called with application and context`() {
        var configured = false
        val configurator = object : EnterpriseConfigurator {
            override fun configure(application: Application, context: EnterpriseBackendContext) {
                configured = true
            }
            override fun configureRoutes(routing: Routing, context: EnterpriseBackendContext) {}
        }
        val app = mockk<Application>(relaxed = true)
        val ctx = mockk<EnterpriseBackendContext>(relaxed = true)

        configurator.configure(app, ctx)

        assertTrue(configured)
    }

    @Test
    fun `configureRoutes is called with routing and context`() {
        var routesConfigured = false
        val configurator = object : EnterpriseConfigurator {
            override fun configure(application: Application, context: EnterpriseBackendContext) {}
            override fun configureRoutes(routing: Routing, context: EnterpriseBackendContext) {
                routesConfigured = true
            }
        }
        val routing = mockk<Routing>(relaxed = true)
        val ctx = mockk<EnterpriseBackendContext>(relaxed = true)

        configurator.configureRoutes(routing, ctx)

        assertTrue(routesConfigured)
    }

    @Test
    fun `configurator receives same context in configure and configureRoutes`() {
        var contextFromConfigure: EnterpriseBackendContext? = null
        var contextFromRoutes: EnterpriseBackendContext? = null
        val configurator = object : EnterpriseConfigurator {
            override fun configure(application: Application, context: EnterpriseBackendContext) {
                contextFromConfigure = context
            }
            override fun configureRoutes(routing: Routing, context: EnterpriseBackendContext) {
                contextFromRoutes = context
            }
        }
        val app = mockk<Application>(relaxed = true)
        val routing = mockk<Routing>(relaxed = true)
        val ctx = mockk<EnterpriseBackendContext>(relaxed = true)

        configurator.configure(app, ctx)
        configurator.configureRoutes(routing, ctx)

        assertSame(ctx, contextFromConfigure)
        assertSame(ctx, contextFromRoutes)
    }
}
