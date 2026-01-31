pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
        mavenCentral()
        google()
    }
}

rootProject.name = "Flagent Kotlin Sample"

// Include SDK modules (use same project names as main monorepo for kotlin-enhanced's findProject)
include(":kotlin-client")
project(":kotlin-client").projectDir = file("../../sdk/kotlin")

include(":kotlin-enhanced")
project(":kotlin-enhanced").projectDir = file("../../sdk/kotlin-enhanced")
