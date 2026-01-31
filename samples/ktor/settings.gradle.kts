pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Flagent Ktor Sample"

// Include shared (required by ktor-flagent)
include(":shared")
project(":shared").projectDir = file("../../shared")

// Include ktor-flagent plugin
include(":ktor-flagent")
project(":ktor-flagent").projectDir = file("../../ktor-flagent")
