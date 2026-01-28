package flagent.frontend.config

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppConfigTest {
    
    @Test
    fun testApiBaseUrlIsSet() {
        assertNotNull(AppConfig.apiBaseUrl)
    }
    
    @Test
    fun testDebugModeIsBoolean() {
        assertTrue(AppConfig.debugMode is Boolean)
    }
    
    @Test
    fun testApiTimeoutIsPositive() {
        assertTrue(AppConfig.apiTimeout > 0)
    }
    
    @Test
    fun testDefaultLanguageIsSet() {
        assertNotNull(AppConfig.defaultLanguage)
        assertTrue(AppConfig.defaultLanguage.isNotEmpty())
    }
    
    @Test
    fun testFeaturesAreAccessible() {
        assertTrue(AppConfig.Features.enableAuth is Boolean)
        assertTrue(AppConfig.Features.enableMultiTenancy is Boolean)
        assertTrue(AppConfig.Features.enableMetrics is Boolean)
        assertTrue(AppConfig.Features.enableRealtime is Boolean)
    }
}
