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
project(":kotlin-client").projectDir = file("../kotlin")

include(":kotlin-enhanced")
project(":kotlin-enhanced").projectDir = file("../kotlin-enhanced")

include(":kotlin-debug-ui")
project(":kotlin-debug-ui").projectDir = file("../kotlin-debug-ui")

// Include app
include(":app")
