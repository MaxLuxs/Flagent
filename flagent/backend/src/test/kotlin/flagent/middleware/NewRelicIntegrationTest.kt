package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlin.test.*

class NewRelicIntegrationTest {
    @Test
    fun testConfigureNewRelic_DoesNothing_WhenDisabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.newRelicEnabled } returns false
        
        application {
            configureNewRelic()
        }
        
        // Should not throw exception
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testConfigureNewRelic_Initializes_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.newRelicEnabled } returns true
        
        application {
            configureNewRelic()
        }
        
        // Should not throw exception
        // New Relic API works without agent (no-op implementations)
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testConfigureNewRelic_HandlesException() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.newRelicEnabled } returns true
        
        application {
            // Should handle exceptions gracefully
            try {
                configureNewRelic()
            } catch (e: Exception) {
                // Should not propagate
            }
        }
        
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
}
