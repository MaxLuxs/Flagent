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

    // Replace org.lz4:lz4-java with at.yawk.lz4 fork to fix CVE-2025-12183, CVE-2025-66566 (transitive from kafka-clients)
    configurations.all {
        resolutionStrategy {
            dependencySubstitution {
                substitute(module("org.lz4:lz4-java")).using(module("at.yawk.lz4:lz4-java:1.10.2"))
            }
        }
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

// Root "run" = frontend only. Stops Gradle from running run in ALL subprojects (sample-kotlin, sample-ktor, backend, frontend).
tasks.register("run") {
    group = "application"
    description = "Run frontend dev server at http://localhost:8080. For full dev: run :backend:run in another terminal first."
    dependsOn(":frontend:run")
}
