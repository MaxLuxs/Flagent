@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.flagent.debug.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

class FlagentDebugUITest {

    @Test
    fun `DebugScreen composes and shows title`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("debug-title").assertIsDisplayed()
        onNodeWithText("Flagent Debug").assertIsDisplayed()
    }

    @Test
    fun `DebugScreen shows Evaluate Clear cache and Evict expired controls`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("evaluate-button").assertIsDisplayed()
        onNodeWithTag("clear-cache-button").assertIsDisplayed()
        onNodeWithTag("evict-expired-button").assertIsDisplayed()
    }

    @Test
    fun `clicking Clear cache calls manager clearCache`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery { manager.clearCache() } returns Unit

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("clear-cache-button").performClick()
        waitForIdle()

        coVerify(exactly = 1) { manager.clearCache() }
    }

    @Test
    fun `clicking Evict expired calls manager evictExpired`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery { manager.evictExpired() } returns Unit

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("evict-expired-button").performClick()
        waitForIdle()

        coVerify(exactly = 1) { manager.evictExpired() }
    }

    @Test
    fun `evaluate success shows result card with variantKey`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery {
            manager.evaluate(
                flagKey = any(),
                flagID = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any()
            )
        } returns EvalResult(flagKey = "my_flag", variantKey = "control")

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("evaluate-button").performClick()
        waitForIdle()

        onNodeWithTag("result-card").assertIsDisplayed()
        onNodeWithText("variantKey: control").assertIsDisplayed()
    }

    @Test
    fun `evaluate failure shows error message`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery {
            manager.evaluate(
                flagKey = any(),
                flagID = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any()
            )
        } throws RuntimeException("Connection refused")

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        onNodeWithTag("evaluate-button").performClick()
        waitForIdle()

        onNodeWithText("Connection refused").assertIsDisplayed()
    }
}
