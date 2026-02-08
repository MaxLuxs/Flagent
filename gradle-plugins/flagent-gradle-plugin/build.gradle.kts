plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.flagent"
version = "0.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    testImplementation(kotlin("test"))
}

gradlePlugin {
    website.set("https://github.com/MaxLuxs/Flagent")
    vcsUrl.set("https://github.com/MaxLuxs/Flagent")
    plugins {
        create("flagentVerifyFlags") {
            id = "com.flagent.verify-flags"
            implementationClass = "com.flagent.gradle.FlagentPlugin"
            displayName = "Flagent Verify Flags"
            description = "Verify feature flag keys in code exist in Flagent"
            tags.set(listOf("feature-flags", "verification", "flagent"))
        }
    }
}
