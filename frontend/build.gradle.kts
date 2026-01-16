plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
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
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }

        val jsMain by getting {
            dependencies {
                // Shared models
                implementation(project(":shared"))

                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(compose.html.svg)

                // Ktor Client for API
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                // Kotlinx Serialization
                implementation(libs.kotlinx.serialization.json)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
