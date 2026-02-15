pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "flagent"

include(":shared")
include(":backend")
include(":frontend")
include(":ktor-flagent")

// SDK projects
include(":kotlin-client")
include(":java-client")
include(":flagent-spring-boot-starter")
include(":kotlin-enhanced")
include(":kotlin-debug-ui")
include(":flagent-koin")

// Android sample
include(":android-sample")
include(":android-sample:app")

// Standalone samples (Ktor, Kotlin, Spring Boot)
include(":sample-ktor")
include(":sample-kotlin")
include(":sample-spring-boot")

// Enterprise module (optional submodule)
if (file("internal/flagent-enterprise/build.gradle.kts").exists()) {
    include(":flagent-enterprise")
    project(":flagent-enterprise").projectDir = file("internal/flagent-enterprise")
}

project(":shared").projectDir = file("shared")
project(":backend").projectDir = file("backend")
project(":frontend").projectDir = file("frontend")
project(":ktor-flagent").projectDir = file("ktor-flagent")

project(":kotlin-client").projectDir = file("sdk/kotlin")
project(":java-client").projectDir = file("sdk/java")
project(":flagent-spring-boot-starter").projectDir = file("sdk/spring-boot-starter")
project(":kotlin-enhanced").projectDir = file("sdk/kotlin-enhanced")
project(":kotlin-debug-ui").projectDir = file("sdk/kotlin-debug-ui")
project(":flagent-koin").projectDir = file("sdk/flagent-koin")

project(":android-sample").projectDir = file("samples/android")
project(":android-sample:app").projectDir = file("samples/android/app")

project(":sample-ktor").projectDir = file("samples/ktor")
project(":sample-kotlin").projectDir = file("samples/kotlin")
project(":sample-spring-boot").projectDir = file("samples/spring-boot")

// Gradle plugin (use via includeBuild)
includeBuild("gradle-plugins/flagent-gradle-plugin")
