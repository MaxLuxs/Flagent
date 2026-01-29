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
include(":kotlin-enhanced")
include(":kotlin-debug-ui")

// Android sample
include(":android-sample")
include(":android-sample:app")

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
project(":kotlin-enhanced").projectDir = file("sdk/kotlin-enhanced")
project(":kotlin-debug-ui").projectDir = file("sdk/kotlin-debug-ui")

project(":android-sample").projectDir = file("sdk/android-sample")
project(":android-sample:app").projectDir = file("sdk/android-sample/app")
