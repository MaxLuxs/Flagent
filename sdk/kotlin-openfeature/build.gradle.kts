import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    `maven-publish`
}

group = "com.flagent"
// version inherited from root (VERSION file)

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvm()
    jvmToolchain(21)
    androidLibrary {
        namespace = "com.flagent.openfeature"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    linuxX64()
    mingwX64()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kotlin-client"))
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
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

publishing {
    publications {
        withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            pom {
                name.set("Flagent Kotlin OpenFeature adapter")
                description.set("OpenFeature-like Kotlin multiplatform client built on top of Flagent Kotlin client")
                url.set("https://github.com/MaxLuxs/Flagent")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("flagent")
                        name.set("Flagent Team")
                    }
                }
            }
        }
    }
}

