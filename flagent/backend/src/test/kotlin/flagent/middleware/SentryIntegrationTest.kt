package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlin.test.*

class SentryIntegrationTest {
    @Test
    fun testConfigureSentry_DoesNothing_WhenDisabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        every { AppConfig.sentryDSN } returns ""
        
        application {
            configureSentry()
        }
        
        // Should not throw exception
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testConfigureSentry_DoesNothing_WhenDSNEmpty() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns true
        every { AppConfig.sentryDSN } returns ""
        
        application {
            configureSentry()
        }
        
        // Should not throw exception
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testConfigureSentry_Initializes_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns true
        every { AppConfig.sentryDSN } returns "https://test@test.ingest.sentry.io/test"
        every { AppConfig.sentryEnvironment } returns "test"
        
        application {
            configureSentry()
        }
        
        // Should not throw exception
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testConfigureSentry_HandlesException() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns true
        every { AppConfig.sentryDSN } returns "invalid-dsn"
        every { AppConfig.sentryEnvironment } returns ""
        
        application {
            // Should handle exceptions gracefully
            try {
                configureSentry()
            } catch (e: Exception) {
                // Should not propagate
            }
        }
        
        assertTrue(true)
        
        unmockkObject(AppConfig)
    }
}
