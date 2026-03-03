import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.flagent.verify-flags")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":kotlin-client"))
    implementation(project(":kotlin-enhanced"))
    compileOnly(project(":flagent-annotations"))
    implementation(libs.kotlinx.coroutines.core)
}

flagent {
    baseUrl = "http://localhost:18000"
    apiKey = ""
    flagsFile = file("flags.yaml")
    failOnUnknown = true
    flagKeysOutputDir = layout.buildDirectory.dir("generated/sources/flagent/flagKeys")
    flagKeysFormat = "object"
    flagKeysPackage = "flagent.sample.strict.generated"
    allowOnlyGeneratedOrAnnotated = true
}

kotlin {
    jvmToolchain(17)
    sourceSets["main"].kotlin.srcDirs(layout.buildDirectory.dir("generated/sources/flagent/flagKeys"))
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}
