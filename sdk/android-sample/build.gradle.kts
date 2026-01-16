// Top-level build file for Android Sample App
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
