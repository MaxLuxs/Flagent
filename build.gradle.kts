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

// Backend (runDev = FLAGENT_DEV_MODE + FLAGENT_DEV_SKIP_TENANT_AUTH, no X-API-Key) + frontend.
// Both run in parallel (different subprojects). Use: ./gradlew run --parallel (or org.gradle.parallel=true in gradle.properties).
// Ctrl+C stops both.
tasks.register("run") {
    group = "application"
    description = "Backend (dev, no token) + frontend at http://localhost:8080. Requires org.gradle.parallel=true (see gradle.properties)."
    dependsOn(":backend:runDev", ":frontend:jsBrowserDevelopmentRun")
}
