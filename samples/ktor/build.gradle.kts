import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server (use same version as ktor-flagent / libs.versions.toml to avoid NoSuchMethodError)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.status.pages)

    // Flagent Ktor plugin (local)
    implementation(project(":ktor-flagent"))

    // Logging
    implementation(libs.logback.classic)
}

application {
    mainClass.set("flagent.sample.ApplicationKt")
}

// Disable default "run" so root "run" (frontend only) does not trigger this sample. Use :sample-ktor:runSample to run.
tasks.named<JavaExec>("run").configure { enabled = false }
tasks.register<JavaExec>("runSample") {
    group = "application"
    description = "Run Ktor sample server on port 8080 (requires backend at http://localhost:18000)"
    mainClass.set(application.mainClass.get())
    classpath = sourceSets.main.get().runtimeClasspath
}

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
