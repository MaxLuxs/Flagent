package flagent.application

import flagent.api.EnterpriseBackendContext
import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DefaultEnterpriseConfiguratorTest {

    @Test
    fun `configure does not throw`() {
        val configurator = DefaultEnterpriseConfigurator()
        val app = mockk<Application>(relaxed = true)
        val ctx = mockk<EnterpriseBackendContext>(relaxed = true)

        configurator.configure(app, ctx)
        assertTrue(true, "configure completes without exception")
    }

    @Test
    fun `configureRoutes does not throw`() {
        val configurator = DefaultEnterpriseConfigurator()
        val routing = mockk<Routing>(relaxed = true)
        val ctx = mockk<EnterpriseBackendContext>(relaxed = true)

        configurator.configureRoutes(routing, ctx)
        assertTrue(true, "configureRoutes completes without exception")
    }
}
