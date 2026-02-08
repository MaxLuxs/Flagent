import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.plugin.compose)
    `maven-publish`
}

group = "com.flagent"

repositories {
    mavenCentral()
    google()
}

dependencies {
    if (findProject(":kotlin-client") != null) {
        implementation(project(":kotlin-client"))
    } else {
        implementation("com.flagent:kotlin-client:0.1.5")
    }
    if (findProject(":kotlin-enhanced") != null) {
        implementation(project(":kotlin-enhanced"))
    } else {
        implementation("com.flagent:kotlin-enhanced:0.1.5")
    }
    implementation(libs.kotlinx.coroutines.core)
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)

    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.test.ExperimentalTestApi")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
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