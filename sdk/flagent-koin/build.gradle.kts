import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    `maven-publish`
}

group = "com.flagent"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvm()
    jvmToolchain(21)
    androidLibrary {
        namespace = "com.flagent.koin"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
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
                implementation(libs.koin.core)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                implementation(libs.mockk)
                implementation(libs.koin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        if (name.contains("Jvm", ignoreCase = true)) {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
