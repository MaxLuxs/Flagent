import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.flagent"
// version inherited from root (VERSION file)

repositories {
    mavenCentral()
}

dependencies {
    // Enhanced SDK - use local project if available, otherwise Maven
    if (findProject(":kotlin-enhanced") != null) {
        implementation(project(":kotlin-enhanced"))
    } else {
        implementation("com.flagent:flagent-kotlin-enhanced-client:0.1.4")
    }
    
    // Compose for UI - using Compose Multiplatform
    // Note: These dependencies will be used when Compose UI is fully implemented
    // Currently commented out to avoid build errors until Compose Multiplatform is properly configured
    // implementation("org.jetbrains.compose.ui:ui:1.6.1")
    // implementation("org.jetbrains.compose.foundation:foundation:1.6.1")
    // implementation("org.jetbrains.compose.material:material:1.6.1")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
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