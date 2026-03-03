package com.flagent.gradle

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File

/**
 * Extension for Flagent verify-flags plugin.
 */
abstract class FlagentExtension constructor(@org.gradle.api.tasks.Internal val project: Project) {

    /**
     * Flagent API base URL. Used to fetch flags via GET /api/v1/flags.
     */
    abstract val baseUrl: Property<String>

    /**
     * API key for authentication (X-API-Key header).
     */
    abstract val apiKey: Property<String>

    /**
     * Local flags file (YAML or JSON) instead of API. Format: list of objects with "key" field.
     */
    abstract val flagsFile: RegularFileProperty

    /**
     * If true, build fails when code references a flag not found in Flagent.
     */
    abstract val failOnUnknown: Property<Boolean>

    // --- generateFlagKeys ---

    /**
     * Output directory for generated FlagKeys.kt. Default: build/generated/sources/flagent/flagKeys
     */
    abstract val flagKeysOutputDir: DirectoryProperty

    /**
     * Format of generated file: "object" (const vals) or "enum". Default: "object"
     */
    abstract val flagKeysFormat: Property<String>

    /**
     * Package name for the generated class. Default: "flagent.generated" or project group + ".generated"
     */
    abstract val flagKeysPackage: Property<String>

    /**
     * When true, build fails if code uses raw string in isEnabled("...")/evaluate("...");
     * only keys from generated FlagKeys or @FlagKey are allowed.
     */
    abstract val allowOnlyGeneratedOrAnnotated: Property<Boolean>

    init {
        baseUrl.convention("http://localhost:18000")
        apiKey.convention("")
        failOnUnknown.convention(true)
        flagKeysOutputDir.convention(project.layout.buildDirectory.dir("generated/sources/flagent/flagKeys"))
        flagKeysFormat.convention("object")
        flagKeysPackage.convention("flagent.generated")
        allowOnlyGeneratedOrAnnotated.convention(false)
    }
}
