import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "com.flagent"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

android {
    compileSdk = 35
    namespace = "com.flagent.debug.ui"
}

kotlin {
    jvm()
    jvmToolchain(21)
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kotlin-client"))
                implementation(project(":kotlin-enhanced"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.animation)
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
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
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
