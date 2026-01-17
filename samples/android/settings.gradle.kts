pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Flagent Android Sample"

// Include SDK modules first
include(":kotlin-client")
project(":kotlin-client").projectDir = file("../../sdk/kotlin")

include(":kotlin-enhanced")
project(":kotlin-enhanced").projectDir = file("../../sdk/kotlin-enhanced")

include(":kotlin-debug-ui")
project(":kotlin-debug-ui").projectDir = file("../../sdk/kotlin-debug-ui")

// Include app
include(":app")
