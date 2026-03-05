import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    `maven-publish`
}

group = "com.flagent"
// Fallback when not building from source: com.flagent:kotlin-enhanced:0.1.7

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm()
    jvmToolchain(21)
    androidLibrary {
        namespace = "com.flagent.debug.ui"
        compileSdk = 35
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kotlin-client"))
                implementation(project(":kotlin-enhanced"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3.mp)
                implementation(libs.compose.ui.mp)
                implementation(libs.compose.animation)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting { }
        val jvmTest by getting {
            dependencies {
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.compose.desktop.current.os)
                implementation(libs.compose.desktop.ui.test.junit4)
                // Explicit Skiko AWT runtime for current OS/arch so native lib is on classpath (helps when display/xvfb available)
                val os = System.getProperty("os.name").lowercase()
                val arch = System.getProperty("os.arch").lowercase()
                val skikoClassifier = when {
                    os.contains("mac") && (arch == "aarch64" || arch == "arm64") -> "macos-arm64"
                    os.contains("mac") -> "macos-x64"
                    os.contains("linux") && (arch == "aarch64" || arch == "arm64") -> "linux-arm64"
                    os.contains("linux") -> "linux-x64"
                    os.contains("win") && (arch == "aarch64" || arch == "arm64") -> "windows-arm64"
                    os.contains("win") -> "windows-x64"
                    else -> null
                }
                if (skikoClassifier != null) {
                    implementation("org.jetbrains.skiko:skiko-awt-runtime-$skikoClassifier:0.9.37.3")
                }
            }
        }
        val androidMain by getting { }
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        if (name.contains("Jvm", ignoreCase = true)) {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnit()
    // Skiko/Compose Desktop UI tests require a display. In CI they are either skipped (unit_test job)
    // or run in the kotlin-debug-ui job with xvfb-run (FLAGENT_RUN_DEBUG_UI_TESTS=true).
    onlyIf {
        System.getenv("CI") != "true" || System.getenv("FLAGENT_RUN_DEBUG_UI_TESTS") == "true"
    }
}

publishing {
    publications {
        withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            pom {
                name.set("Flagent Kotlin Debug UI")
                description.set("Debug UI library for Flagent Enhanced SDK using Compose Multiplatform")
                url.set("https://github.com/MaxLuxs/Flagent")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }
    }
}
