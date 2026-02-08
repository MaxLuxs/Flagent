package com.flagent.koin

import com.flagent.enhanced.config.FlagentConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class FlagentManagerProviderTest {

    @Test
    fun `getManager returns same instance for same baseUrl and config`() {
        val provider = FlagentManagerProvider()
        val manager1 = provider.getManager("http://localhost:18000")
        val manager2 = provider.getManager("http://localhost:18000")

        assertSame(manager1, manager2)
    }

    @Test
    fun `getManager returns same instance when baseUrl has trailing slash`() {
        val provider = FlagentManagerProvider()
        val manager1 = provider.getManager("http://localhost:18000/")
        val manager2 = provider.getManager("http://localhost:18000")

        assertSame(manager1, manager2)
    }

    @Test
    fun `getManager normalizes URL - adds api v1 if missing`() {
        val provider = FlagentManagerProvider()
        val manager1 = provider.getManager("http://localhost:18000")
        val manager2 = provider.getManager("http://localhost:18000/api/v1")

        assertSame(manager1, manager2)
    }

    @Test
    fun `getManager returns different instances for different baseUrls`() {
        val provider = FlagentManagerProvider()
        val manager1 = provider.getManager("http://localhost:18000")
        val manager2 = provider.getManager("http://localhost:18001")

        assertNotSame(manager1, manager2)
    }

    @Test
    fun `getManager returns different instances for different configs`() {
        val provider = FlagentManagerProvider()
        val config1 = FlagentConfig(enableCache = true, cacheTtlMs = 60000)
        val config2 = FlagentConfig(enableCache = false, cacheTtlMs = 30000)

        val manager1 = provider.getManager("http://localhost:18000", config1)
        val manager2 = provider.getManager("http://localhost:18000", config2)

        assertNotSame(manager1, manager2)
    }

    @Test
    fun `getEvaluationApi returns same instance for same baseUrl`() {
        val provider = FlagentManagerProvider()
        val api1 = provider.getEvaluationApi("http://localhost:18000")
        val api2 = provider.getEvaluationApi("http://localhost:18000")

        assertSame(api1, api2)
    }

    @Test
    fun `getEvaluationApi returns different instances for different baseUrls`() {
        val provider = FlagentManagerProvider()
        val api1 = provider.getEvaluationApi("http://localhost:18000")
        val api2 = provider.getEvaluationApi("http://localhost:18001")

        assertNotSame(api1, api2)
    }
}
