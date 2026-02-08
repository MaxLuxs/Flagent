package com.flagent.koin

import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.inject
import kotlin.test.assertNotNull

class FlagentModuleTest : KoinTest {

    @Test
    fun `flagentClientModule loads and provides FlagentManager`() {
        stopKoin()
        startKoin {
            modules(
                flagentClientModule(
                    baseUrl = "http://localhost:18000",
                    config = FlagentConfig(enableCache = false)
                )
            )
        }
        try {
            val manager: FlagentManager by inject()
            assertNotNull(manager)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `flagentManagerProviderModule loads and provides FlagentManagerProvider`() {
        stopKoin()
        startKoin {
            modules(flagentManagerProviderModule())
        }
        try {
            val provider: FlagentManagerProvider by inject()
            assertNotNull(provider)
            val manager = provider.getManager("http://localhost:18000")
            assertNotNull(manager)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `flagentClientModule passes module check`() {
        stopKoin()
        checkModules {
            flagentClientModule(
                baseUrl = "http://localhost:18000",
                config = FlagentConfig(enableCache = false)
            )
        }
    }

    @Test
    fun `flagentManagerProviderModule passes module check`() {
        stopKoin()
        checkModules {
            flagentManagerProviderModule()
        }
    }
}
