@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.flagent.debug.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test

class FeatureFlagTest {

    @Test
    fun `FlagentProvider provides manager to children`() = runComposeUiTest {
        val manager = mockk<FlagentManager>()
        var receivedManager: FlagentManager? = null

        setContent {
            FlagentProvider(manager = manager) {
                receivedManager = LocalFlagentManager.current
                Text("child", modifier = Modifier.testTag("child"))
            }
        }

        onNodeWithTag("child").assertTextEquals("child")
        assert(receivedManager === manager)
    }

    @Test
    fun `FeatureFlag shows content when flag is enabled`() = runComposeUiTest {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "control")

        setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        waitForIdle()
        onNodeWithTag("content").assertTextEquals("Enabled")
    }

    @Test
    fun `FeatureFlag shows fallback when flag is disabled`() = runComposeUiTest {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "disabled")

        setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        waitForIdle()
        onNodeWithTag("fallback").assertTextEquals("Disabled")
    }

    @Test
    fun `FeatureFlag shows fallback on evaluation exception`() = runComposeUiTest {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } throws RuntimeException("network error")

        setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        waitForIdle()
        onNodeWithTag("fallback").assertTextEquals("Disabled")
    }

    @Test
    fun `FeatureFlag shows content when variantKey is not disabled`() = runComposeUiTest {
        val manager = mockk<FlagentManager>()
        coEvery {
            manager.evaluate(
                flagKey = any(),
                entityID = any(),
                entityType = any(),
                entityContext = any()
            )
        } returns EvalResult(flagKey = "test_flag", variantKey = "variant_a")

        setContent {
            FeatureFlag(
                key = "test_flag",
                manager = manager,
                content = { Text("Enabled", modifier = Modifier.testTag("content")) },
                fallback = { Text("Disabled", modifier = Modifier.testTag("fallback")) }
            )
        }

        waitForIdle()
        onNodeWithTag("content").assertTextEquals("Enabled")
    }
}
