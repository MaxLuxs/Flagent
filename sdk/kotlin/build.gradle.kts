import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.android.library)
    jacoco
    `maven-publish`
}

group = "com.flagent"
// version inherited from root (VERSION file)

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = 34
    namespace = "com.flagent.client"
}

kotlin {
    jvm()
    jvmToolchain(21)
    androidTarget()
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
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.mock)
                implementation("io.kotest:kotest-runner-junit5:5.8.0")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val linuxX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:${libs.versions.ktor.get()}")
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:${libs.versions.ktor.get()}")
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val macosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:${libs.versions.ktor.get()}")
                implementation(libs.ktor.serialization.kotlinx.json)
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

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// JaCoCo report for jvmTest (KMP has jvmTest, not test)
val jacocoReport = tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generate JaCoCo coverage report for JVM tests"
    dependsOn(tasks.named("jvmTest"))
    val jvmTest = tasks.named<Test>("jvmTest").get()
    executionData.from(fileTree(layout.buildDirectory) { include("jacoco/${jvmTest.name}.exec") })
    additionalSourceDirs.from(
        file("$projectDir/src/commonMain/kotlin"),
        file("$projectDir/src/jvmMain/kotlin")
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main")) {
            exclude("**/BuildConfig.class", "**/R.class")
        }
    )
}
tasks.named<Test>("jvmTest") { finalizedBy(jacocoReport) }

// Enforce minimum coverage (line coverage ≥ 85%)
tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "verification"
    description = "Verify JVM test line coverage meets minimum"
    dependsOn(jacocoReport)
    val jvmTest = tasks.named<Test>("jvmTest").get()
    executionData.from(fileTree(layout.buildDirectory) { include("jacoco/${jvmTest.name}.exec") })
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main")) {
            exclude("**/BuildConfig.class", "**/R.class")
        }
    )
    violationRules {
        rule { limit { minimum = "0.85".toBigDecimal() } }
    }
}

publishing {
    publications {
        withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            pom {
                name.set("Flagent Kotlin Client")
                description.set("Kotlin multiplatform client library for Flagent API")
                url.set("https://github.com/MaxLuxs/flagent")
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
