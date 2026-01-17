pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "Flagent Kotlin Sample"

// Include SDK modules
include(":sdk:kotlin")
project(":sdk:kotlin").projectDir = file("../../sdk/kotlin")

include(":sdk:kotlin-enhanced")
project(":sdk:kotlin-enhanced").projectDir = file("../../sdk/kotlin-enhanced")
