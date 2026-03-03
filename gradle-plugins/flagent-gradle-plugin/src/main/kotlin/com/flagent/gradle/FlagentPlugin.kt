package com.flagent.gradle

import com.flagent.gradle.tasks.GenerateFlagKeysTask
import com.flagent.gradle.tasks.VerifyFlagsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class FlagentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("flagent", FlagentExtension::class.java, project)

        project.tasks.register("verifyFlags", VerifyFlagsTask::class.java) { t: VerifyFlagsTask ->
            t.extension.set(ext)
        }

        val generateFlagKeys = project.tasks.register("generateFlagKeys", GenerateFlagKeysTask::class.java) { t: GenerateFlagKeysTask ->
            t.extension.set(ext)
            t.outputDir.set(ext.flagKeysOutputDir)
        }

        // Run generateFlagKeys before any Kotlin compile. User must add output dir to source sets.
        project.afterEvaluate {
            project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
                it.dependsOn(generateFlagKeys)
            }
        }
    }
}
