import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
}

val projectVersion: String = run {
    val versionFile = rootProject.layout.projectDirectory.file("VERSION")
    if (versionFile.asFile.exists()) versionFile.asFile.readText().trim()
    else project.findProperty("version")?.toString() ?: "0.1.0"
}

allprojects {
    group = "com.flagent"
    version = projectVersion

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

subprojects {
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/MaxLuxs/Flagent")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR").orEmpty()
                        password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN").orEmpty()
                    }
                }
            }
        }
    }
}
