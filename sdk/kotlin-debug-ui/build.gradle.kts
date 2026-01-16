import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.flagent"
version = "1.1.19"

repositories {
    mavenCentral()
}

dependencies {
    // Enhanced SDK - use local project if available, otherwise Maven
    if (findProject(":kotlin-enhanced") != null) {
        implementation(project(":kotlin-enhanced"))
    } else {
        implementation("com.flagent:flagent-kotlin-enhanced-client:1.1.19")
    }
    
    // Compose (for future UI implementation)
    // Note: Compose UI will be added in next iteration
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            
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