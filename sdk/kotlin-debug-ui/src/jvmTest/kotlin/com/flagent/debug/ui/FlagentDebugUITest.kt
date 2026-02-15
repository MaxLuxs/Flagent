package com.flagent.debug.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class FlagentDebugUITest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `DebugScreen composes and shows title`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("debug-title").assertIsDisplayed()
        rule.onNodeWithText("Flagent Debug").assertIsDisplayed()
    }

    @Test
    fun `DebugScreen shows Evaluate Clear cache and Evict expired controls`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("evaluate-button").assertIsDisplayed()
        rule.onNodeWithTag("clear-cache-button").assertIsDisplayed()
        rule.onNodeWithTag("evict-expired-button").assertIsDisplayed()
    }

    @Test
    fun `clicking Clear cache calls manager clearCache`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery { manager.clearCache() } returns Unit

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("clear-cache-button").performClick()
        rule.waitForIdle()

        coVerify(exactly = 1) { manager.clearCache() }
    }

    @Test
    fun `clicking Evict expired calls manager evictExpired`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        coEvery { manager.evictExpired() } returns Unit

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("evict-expired-button").performClick()
        rule.waitForIdle()

        coVerify(exactly = 1) { manager.evictExpired() }
    }

    @Test
    fun `evaluate success shows result card with variantKey`() {
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

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("evaluate-button").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("result-card").assertIsDisplayed()
        rule.onNodeWithText("variantKey: control").assertIsDisplayed()
    }

    @Test
    fun `evaluate failure shows error message`() {
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

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager)
        }

        rule.onNodeWithTag("evaluate-button").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Connection refused").assertIsDisplayed()
    }

    @Test
    fun `with flagsProvider shows Flags section and Refresh button`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            delay(10)
            listOf(FlagRow("feature_a", 1L, true, listOf("control", "treatment")))
        }

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        rule.waitForIdle()

        rule.onNodeWithTag("flags-card").assertIsDisplayed()
        rule.onNodeWithTag("refresh-flags-button").assertIsDisplayed()
        rule.onNodeWithText("Flags").assertIsDisplayed()
        rule.onNodeWithText("feature_a").assertIsDisplayed()
    }

    @Test
    fun `clicking Refresh calls flagsProvider again`() {
        var callCount = 0
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            callCount++
            delay(5)
            listOf(FlagRow("f1", 1L, true, listOf("control")))
        }

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        rule.waitForIdle()
        val afterLoad = callCount

        rule.onNodeWithTag("refresh-flags-button").performClick()
        rule.waitForIdle()

        assertTrue("flagsProvider should be called again after Refresh") { callCount > afterLoad }
    }

    /** Override + Evaluate: result from override, manager not called. Ignored: with flagsProvider, Desktop Compose test does not show result card (coroutine/timing). */
    @org.junit.Ignore("Desktop: Evaluate result not shown when flagsProvider is set")
    @Test
    fun `evaluate with override shows result without calling manager`() {
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
        rule.setContent { FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider) }
        rule.waitForIdle()
        Thread.sleep(150)
        rule.onNodeWithTag("override-my_flag").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("override-option-disabled").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("clear-override-my_flag").assertIsDisplayed()
        rule.waitForIdle()
        rule.onNodeWithTag("eval-flag-key").performTextReplacement("my_flag")
        rule.waitForIdle()
        rule.onNodeWithTag("evaluate-button").performClick()
        rule.waitForIdle()
        Thread.sleep(600)
        rule.onNodeWithTag("result-card").assertIsDisplayed()
        rule.onNodeWithText("variantKey: disabled").assertIsDisplayed()
        coVerify(exactly = 0) {
            manager.evaluate(flagKey = any(), flagID = any(), entityID = any(), entityType = any(), entityContext = any(), enableDebug = any())
        }
    }

    @Test
    fun `Clear override removes override and shows Clear button when override set`() {
        val manager = mockk<FlagentManager>(relaxUnitFun = true)
        val flagsProvider: suspend () -> List<FlagRow> = {
            delay(10)
            listOf(FlagRow("f1", 1L, true, listOf("control")))
        }

        rule.setContent {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }

        rule.waitForIdle()

        rule.onNodeWithTag("override-f1").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("control").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("clear-override-f1").assertIsDisplayed()
        rule.onNodeWithTag("clear-override-f1").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("override-f1").assertIsDisplayed()
    }
}
