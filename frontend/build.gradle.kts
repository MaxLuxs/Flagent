import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    js(IR) {
        compilerOptions {
            freeCompilerArgs.add("-Xir-minimized-member-names=false")
        }
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

// Ensure design tokens CSS is copied to resources before frontend build/run
tasks.named("jsProcessResources").configure {
    dependsOn(":flagent-design-tokens:copyCssToFrontend")
}
tasks.named("jsBrowserDevelopmentWebpack").configure {
    dependsOn(":flagent-design-tokens:copyCssToFrontend")
}
tasks.named("jsBrowserProductionWebpack").configure {
    dependsOn(":flagent-design-tokens:copyCssToFrontend")
}

tasks.register("run") {
    group = "application"
    description = "Run frontend dev server (webpack dev server at http://localhost:8080)"
    dependsOn(tasks.named("jsBrowserDevelopmentRun"))
}

// Copy production index (loads only frontend.js; dev index loads kotlin/ modules, SPA fallback returns HTML for missing .js)
tasks.named("jsBrowserProductionWebpack").configure {
    doLast {
        val outDir = layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable").get().asFile
        if (outDir.exists()) {
            val indexProd = file("$projectDir/src/jsMain/resources/index.production.html")
            if (indexProd.exists()) {
                indexProd.copyTo(file("$outDir/index.html"), overwrite = true)
            }
        }
    }
}

tasks.named<Sync>("jsBrowserDistribution").configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Development bundle works (production has CoroutineContext.Element.minusKey "this" undefined bug). Copy index for backend serving.
tasks.named("jsBrowserDevelopmentWebpack").configure {
    doLast {
        val outDir = layout.buildDirectory.dir("kotlin-webpack/js/developmentExecutable").get().asFile
        if (outDir.exists()) {
            val indexProd = file("$projectDir/src/jsMain/resources/index.production.html")
            if (indexProd.exists()) {
                indexProd.copyTo(file("$outDir/index.html"), overwrite = true)
            }
        }
    }
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
    dependsOn(tasks.named("compileKotlinJs"))
}

// E2E tests (Playwright) - run from frontend/e2e with: npm run test
// Requires backend + frontend running (./gradlew run from root) or CI=true for auto-start
tasks.register<Exec>("e2e") {
    group = "verification"
    description = "Run Playwright E2E tests. Start servers first: ./gradlew run"
    workingDir = file("e2e")
    commandLine("npm", "run", "test")
    isIgnoreExitValue = false
}
