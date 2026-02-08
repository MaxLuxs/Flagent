package com.flagent.gradle.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class FlagKeyScannerTest {

    @Test
    fun extractsKeysFromFlagKeyAnnotation() {
        val dir = createTempFile("test", ".kt").toFile().parentFile
        val f = File(dir, "Test.kt").apply {
            writeText("""
                @FlagKey("new_checkout")
                fun foo() {}
                @FlagKey("dark_mode")
                val x = 1
            """.trimIndent())
        }
        val keys = FlagKeyScanner.scanSourceFiles(listOf(f))
        assertEquals(setOf("new_checkout", "dark_mode"), keys)
    }

    @Test
    fun extractsKeysFromIsEnabledAndEvaluateCalls() {
        val dir = createTempFile("test", ".kt").toFile().parentFile
        val f = File(dir, "Test.kt").apply {
            writeText("""
                if (client.isEnabled("new_payment")) {}
                manager.evaluate("exp_checkout", entityID)
            """.trimIndent())
        }
        val keys = FlagKeyScanner.scanSourceFiles(listOf(f))
        assertEquals(setOf("new_payment", "exp_checkout"), keys)
    }

    @Test
    fun ignoresInvalidKeys() {
        val dir = createTempFile("test", ".kt").toFile().parentFile
        val f = File(dir, "Test.kt").apply {
            writeText("""
                client.isEnabled("ab")
                client.isEnabled("")
            """.trimIndent())
        }
        val keys = FlagKeyScanner.scanSourceFiles(listOf(f))
        assertEquals(setOf("ab"), keys)
    }
}
