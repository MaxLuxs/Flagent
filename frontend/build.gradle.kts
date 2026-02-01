plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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

tasks.register("run") {
    group = "application"
    description = "Run frontend dev server (webpack dev server at http://localhost:8080)"
    dependsOn(tasks.named("jsBrowserDevelopmentRun"))
}

// Run backend with dev mode (X-API-Key optional) - use with :frontend:run for full dev
tasks.register("runBackendDev") {
    group = "application"
    description = "Run backend with FLAGENT_DEV_MODE+FLAGENT_DEV_SKIP_TENANT_AUTH (X-API-Key optional)"
    dependsOn(":backend:runDev")
}

// Ensure JS test compilation runs after main JS compilation (avoids friendPaths ordering issues
// when building from root with Kotlin MPP + Compose). jsBrowserTest stays in check.
tasks.named("compileTestKotlinJs").configure {
    mustRunAfter(tasks.named("compileKotlinJs"))
}
