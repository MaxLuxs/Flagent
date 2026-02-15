package com.flagent.debug.ui

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertSame

class FeatureFlagTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `FlagentProvider provides manager to children`() {
        val manager = mockk<FlagentManager>()
        var receivedManager: FlagentManager? = null

        rule.setContent {
            FlagentProvider(manager = manager) {
                receivedManager = LocalFlagentManager.current
                Text("child", modifier = Modifier.testTag("child"))
            }
        }

        rule.onNodeWithTag("child").assertTextEquals("child")
        assertSame(manager, receivedManager)
    }

    @Test
    fun `FeatureFlag shows content when flag is enabled`() {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "control")

        rule.setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        rule.waitForIdle()
        rule.onNodeWithTag("content").assertTextEquals("Enabled")
    }

    @Test
    fun `FeatureFlag shows fallback when flag is disabled`() {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "disabled")

        rule.setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        rule.waitForIdle()
        rule.onNodeWithTag("fallback").assertTextEquals("Disabled")
    }

    @Test
    fun `FeatureFlag shows fallback on evaluation exception`() {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } throws RuntimeException("network error")

        rule.setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        rule.waitForIdle()
        rule.onNodeWithTag("fallback").assertTextEquals("Disabled")
    }

    @Test
    fun `FeatureFlag shows content when variantKey is not disabled`() {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "variant_a")

        rule.setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        rule.waitForIdle()
        rule.onNodeWithTag("content").assertTextEquals("Enabled")
    }
}
