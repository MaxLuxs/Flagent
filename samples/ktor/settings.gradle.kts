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
    }
}

rootProject.name = "Flagent Ktor Sample"

// Include ktor-flagent plugin
include(":ktor-flagent")
project(":ktor-flagent").projectDir = file("../../ktor-flagent")
