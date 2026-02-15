@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.flagent.debug.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
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

    @Test
    fun `with flagsProvider shows Flags section and Refresh button`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            delay(10)
            listOf(FlagRow("feature_a", 1L, true, listOf("control", "treatment")))
        }

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        waitForIdle()

        onNodeWithTag("flags-card").assertIsDisplayed()
        onNodeWithTag("refresh-flags-button").assertIsDisplayed()
        onNodeWithText("Flags").assertIsDisplayed()
        onNodeWithText("feature_a").assertIsDisplayed()
    }

    @Test
    fun `clicking Refresh calls flagsProvider again`() = runComposeUiTest {
        var callCount = 0
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            callCount++
            delay(5)
            listOf(FlagRow("f1", 1L, true, listOf("control")))
        }

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        waitForIdle()
        val afterLoad = callCount

        onNodeWithTag("refresh-flags-button").performClick()
        waitForIdle()

        assert(callCount > afterLoad) { "flagsProvider should be called again after Refresh" }
    }

    @Test
    fun `evaluate with override returns result without calling manager`() = runComposeUiTest {
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
        } returns EvalResult(flagKey = "server_flag", variantKey = "control")

        val flagsProvider: suspend () -> List<FlagRow> = {
            delay(10)
            listOf(FlagRow("my_flag", 1L, true, listOf("control", "treatment")))
        }

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        waitForIdle()

        onNodeWithTag("override-my_flag").performClick()
        waitForIdle()
        onNodeWithText("disabled").performClick()
        waitForIdle()

        onNodeWithTag("eval-flag-key").performTextInput("my_flag")
        onNodeWithTag("evaluate-button").performClick()
        waitForIdle()

        onNodeWithTag("result-card").assertIsDisplayed()
        onNodeWithText("variantKey: disabled").assertIsDisplayed()

        coVerify(exactly = 0) {
            manager.evaluate(
                flagKey = any(),
                flagID = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any()
            )
        }
    }

    @Test
    fun `Clear override removes override and shows Clear button when override set`() = runComposeUiTest {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            delay(10)
            listOf(FlagRow("f1", 1L, true, listOf("control")))
        }

        setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        waitForIdle()

        onNodeWithTag("override-f1").performClick()
        waitForIdle()
        onNodeWithText("control").performClick()
        waitForIdle()

        onNodeWithTag("clear-override-f1").assertIsDisplayed()
        onNodeWithTag("clear-override-f1").performClick()
        waitForIdle()

        onNodeWithTag("override-f1").assertIsDisplayed()
    }
}
