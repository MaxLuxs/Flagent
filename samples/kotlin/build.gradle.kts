import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Flagent Kotlin SDK (local)
    implementation(project(":kotlin-client"))
    implementation(project(":kotlin-enhanced"))
    
    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    
    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
}

application {
    mainClass.set("flagent.sample.MainKt")
}

// Disable default "run" so root "run" (frontend only) does not trigger this sample. Use :sample-kotlin:runSample to run.
tasks.named<JavaExec>("run").configure { enabled = false }
tasks.register<JavaExec>("runSample") {
    group = "application"
    description = "Run Kotlin SDK sample (requires backend at http://localhost:18000)"
    mainClass.set(application.mainClass.get())
    classpath = sourceSets.main.get().runtimeClasspath
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
