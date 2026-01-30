plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.compose.html.svg)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Exclude JS tests from global build to avoid compileTestKotlinJs/friendPaths ordering issue (Kotlin MPP + Compose).
// Run :frontend:jsBrowserTest manually when needed.
tasks.named("jsBrowserTest") {
    enabled = false
}
tasks.named("compileTestKotlinJs") {
    enabled = false
}
