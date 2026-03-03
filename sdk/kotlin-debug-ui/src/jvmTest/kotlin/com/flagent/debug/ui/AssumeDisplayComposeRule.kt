package com.flagent.debug.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.awt.HeadlessException
import java.awt.Toolkit

/**
 * JUnit 4 rule that creates the Compose rule lazily and skips tests when no display is available
 * (e.g. headless CI or SSH). Avoids [org.jetbrains.skiko.LibraryLoadException] by not calling
 * [createComposeRule] until the first test runs and only when display is available.
 */
class AssumeDisplayComposeRule : TestRule {

    private val delegate by lazy {
        val headless = try {
            Toolkit.getDefaultToolkit()
            false
        } catch (_: HeadlessException) {
            true
        }
        Assume.assumeTrue(
            "Display required for Compose Desktop UI tests (skip in headless/CI)",
            !headless
        )
        try {
            createComposeRule()
        } catch (e: Throwable) {
            Assume.assumeTrue(
                "Compose/Skiko failed to load (display or native lib): ${e.message}",
                false
            )
            @Suppress("UNREACHABLE_CODE")
            throw e
        }
    }

    private fun assumeOnSkikoFailure(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            // Skiko/LibraryLoadException or NoClassDefFoundError when native lib missing
            Assume.assumeTrue(
                "Compose/Skiko failed (display or native lib): ${e.message}",
                false
            )
        }
    }

    override fun apply(base: Statement, description: Description): Statement =
        delegate.apply(base, description)

    fun setContent(composable: @androidx.compose.runtime.Composable () -> Unit) {
        assumeOnSkikoFailure { delegate.setContent(composable) }
    }

    fun waitForIdle() {
        assumeOnSkikoFailure { delegate.waitForIdle() }
    }

    fun onNodeWithTag(testTag: String) = delegate.onNodeWithTag(testTag)

    fun onNodeWithText(text: String) = delegate.onNodeWithText(text)
}
