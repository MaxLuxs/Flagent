package com.flagent.gradle

import com.flagent.gradle.tasks.VerifyFlagsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class FlagentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("flagent", FlagentExtension::class.java, project)
        project.tasks.register("verifyFlags", VerifyFlagsTask::class.java) { t: VerifyFlagsTask ->
            t.extension.set(ext)
        }
    }
}
