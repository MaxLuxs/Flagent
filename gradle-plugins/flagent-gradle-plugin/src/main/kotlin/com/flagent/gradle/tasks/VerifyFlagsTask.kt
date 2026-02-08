package com.flagent.gradle.tasks

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.flagent.gradle.FlagentExtension
import com.flagent.gradle.scanner.FlagKeyScanner
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class VerifyFlagsTask : DefaultTask() {

    @get:org.gradle.api.tasks.Internal
    abstract val extension: Property<FlagentExtension>

    @TaskAction
    fun verify() {
        val ext = extension.get()
        val files = project.fileTree(mapOf("dir" to "src", "include" to listOf("**/*.kt", "**/*.java"))).files
        val codeKeys = FlagKeyScanner.scanSourceFiles(files)
        if (codeKeys.isEmpty()) {
            logger.lifecycle("verifyFlags: no flag keys found in source code")
            return
        }
        val knownKeys = when {
            ext.flagsFile.orNull?.asFile?.exists() == true -> loadFromFile(ext.flagsFile.get().asFile)
            else -> fetchFromApi(ext.baseUrl.get(), ext.apiKey.get())
        }
        val unknown = codeKeys - knownKeys
        if (unknown.isNotEmpty() && ext.failOnUnknown.get()) {
            throw org.gradle.api.GradleException(
                "verifyFlags: unknown flags in code (not in Flagent): ${unknown.sorted().joinToString(", ")}"
            )
        }
        if (unknown.isNotEmpty()) {
            logger.warn("verifyFlags: unknown flags (not failing): ${unknown.sorted().joinToString(", ")}")
        } else {
            logger.lifecycle("verifyFlags: all ${codeKeys.size} flag(s) verified")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadFromFile(file: File): Set<String> {
        val content = file.readText()
        val mapper = when {
            file.extension.lowercase() in listOf("yaml", "yml") ->
                ObjectMapper(YAMLFactory()).findAndRegisterModules()
            else -> jacksonObjectMapper()
        }
        val tree = mapper.readTree(content)
        val flags = when {
            tree.isObject && tree.has("flags") -> tree.get("flags")
            tree.isArray -> tree
            else -> mapper.createArrayNode()
        }
        return (0 until flags.size()).mapNotNull { flags.get(it).get("key")?.asText()?.takeIf { k -> k.isNotBlank() } }.toSet()
    }

    private fun fetchFromApi(baseUrl: String, apiKey: String): Set<String> {
        val url = baseUrl.trimEnd('/').let {
            if (it.endsWith("/api/v1")) it else "$it/api/v1"
        } + "/flags?limit=1000"
        val client = HttpClient.newBuilder().build()
        val reqBuilder = HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/json")
        if (apiKey.isNotBlank()) reqBuilder.header("X-API-Key", apiKey)
        val res = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() != 200) {
            logger.warn("verifyFlags: API returned ${res.statusCode()}, assuming all flags exist")
            return emptySet()
        }
        val mapper = jacksonObjectMapper()
        @Suppress("UNCHECKED_CAST")
        val flags = mapper.readValue<List<Map<String, Any>>>(res.body())
        return flags.mapNotNull { it["key"]?.toString() }.toSet()
    }
}
