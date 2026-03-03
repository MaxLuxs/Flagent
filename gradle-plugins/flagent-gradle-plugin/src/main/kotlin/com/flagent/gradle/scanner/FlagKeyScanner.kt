package com.flagent.gradle.scanner

import java.io.File

/**
 * Scans Kotlin/Java source files for flag key references.
 * Matches: @FlagKey("key"), isEnabled("key"), evaluate("key"), evaluateBatch(flagKeys = listOf("key"), ...)
 */
object FlagKeyScanner {

    private val FLAG_KEY_ANNOTATION = Regex("""@FlagKey\s*\(\s*"([^"]+)"\s*\)""")
    private val IS_ENABLED_OR_EVALUATE = Regex("""(?:isEnabled|evaluate)\s*\(\s*"([^"]+)"\s*[\),]""")
    /** Match "key" inside listOf("key1", "key2") in evaluateBatch */
    private val STRING_LITERAL_IN_QUOTES = Regex(""""([^"]+)"\s*(?:,\s*|\s*\)|\s*\])""")

    /**
     * All flag keys found in source: from @FlagKey, isEnabled("..."), evaluate("..."), evaluateBatch listOf("...").
     */
    fun scanSourceFiles(files: Collection<File>): Set<String> {
        val keys = mutableSetOf<String>()
        for (file in files) {
            if (!file.name.endsWith(".kt") && !file.name.endsWith(".java")) continue
            val content = file.readText()
            keys.addAll(FLAG_KEY_ANNOTATION.findAll(content).map { it.groupValues[1] })
            keys.addAll(IS_ENABLED_OR_EVALUATE.findAll(content).map { it.groupValues[1] })
            keys.addAll(scanEvaluateBatchRawStrings(content))
        }
        return keys.filter { isValidFlagKey(it) }.toSet()
    }

    /**
     * Keys only from @FlagKey("...") annotations.
     */
    fun scanKeysFromAnnotations(files: Collection<File>): Set<String> {
        val keys = mutableSetOf<String>()
        for (file in files) {
            if (!file.name.endsWith(".kt") && !file.name.endsWith(".java")) continue
            keys.addAll(FLAG_KEY_ANNOTATION.findAll(file.readText()).map { it.groupValues[1] })
        }
        return keys.filter { isValidFlagKey(it) }.toSet()
    }

    /**
     * Keys only from raw string literals in isEnabled("..."), evaluate("..."), and evaluateBatch(..., listOf("..."), ...).
     * Used when allowOnlyGeneratedOrAnnotated is true to fail the build if any raw strings are used.
     */
    fun scanKeysFromRawStrings(files: Collection<File>): Set<String> {
        val keys = mutableSetOf<String>()
        for (file in files) {
            if (!file.name.endsWith(".kt") && !file.name.endsWith(".java")) continue
            val content = file.readText()
            keys.addAll(IS_ENABLED_OR_EVALUATE.findAll(content).map { it.groupValues[1] })
            keys.addAll(scanEvaluateBatchRawStrings(content))
        }
        return keys.filter { isValidFlagKey(it) }.toSet()
    }

    private fun scanEvaluateBatchRawStrings(content: String): Set<String> {
        val keys = mutableSetOf<String>()
        // Match evaluateBatch(..., flagKeys = listOf("a", "b"), ...) or evaluateBatch(listOf("a","b"), ...)
        val listOfBlocks = Regex("""(?:evaluateBatch|evaluateBatchAsync)\s*\([^)]*listOf\s*\([^)]+\)[^)]*\)""").findAll(content)
        for (block in listOfBlocks) {
            STRING_LITERAL_IN_QUOTES.findAll(block.value).forEach { keys.add(it.groupValues[1].trim()) }
        }
        return keys
    }

    /**
     * Reads key values from a generated FlagKeys.kt file (object with const val X = "key" or enum X("key")).
     */
    fun loadKeysFromGeneratedFile(file: File): Set<String> {
        if (!file.exists() || !file.name.endsWith(".kt")) return emptySet()
        val content = file.readText()
        val keys = mutableSetOf<String>()
        // const val NAME = "key" or enum NAME("key")
        val valuePattern = Regex("""(?:=\s*|\(\s*)"([a-zA-Z][a-zA-Z0-9_]*(_[a-zA-Z0-9]+)*)"\s*[\),;]""")
        valuePattern.findAll(content).forEach { keys.add(it.groupValues[1]) }
        return keys
    }

    private fun isValidFlagKey(key: String): Boolean {
        if (key.length < 2) return false
        return key.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*(_[a-zA-Z0-9]+)*"))
    }
}
