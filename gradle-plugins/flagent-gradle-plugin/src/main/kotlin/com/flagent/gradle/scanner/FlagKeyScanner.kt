package com.flagent.gradle.scanner

import java.io.File

/**
 * Scans Kotlin/Java source files for flag key references.
 * Matches: @FlagKey("key"), isEnabled("key"), evaluate("key")
 */
object FlagKeyScanner {

    private val FLAG_KEY_ANNOTATION = Regex("""@FlagKey\s*\(\s*"([^"]+)"\s*\)""")
    private val STRING_IN_QUOTES = Regex(""""([a-zA-Z][a-zA-Z0-9_]*(_[a-zA-Z0-9]+)*)"(?!\s*\))""")
    private val IS_ENABLED_OR_EVALUATE = Regex("""(?:isEnabled|evaluate)\s*\(\s*"([^"]+)"\s*[\),]""")

    fun scanSourceFiles(files: Collection<File>): Set<String> {
        val keys = mutableSetOf<String>()
        for (file in files) {
            if (!file.name.endsWith(".kt") && !file.name.endsWith(".java")) continue
            val content = file.readText()
            keys.addAll(FLAG_KEY_ANNOTATION.findAll(content).map { it.groupValues[1] })
            keys.addAll(IS_ENABLED_OR_EVALUATE.findAll(content).map { it.groupValues[1] })
        }
        return keys.filter { isValidFlagKey(it) }.toSet()
    }

    private fun isValidFlagKey(key: String): Boolean {
        if (key.length < 2) return false
        return key.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*(_[a-zA-Z0-9]+)*"))
    }
}
